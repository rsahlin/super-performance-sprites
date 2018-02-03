package com.super2k.supersprites;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import com.graphicsengine.component.SpriteComponent;
import com.graphicsengine.io.GSONGraphicsEngineFactory;
import com.graphicsengine.scene.GraphicsEngineNodeType;
import com.nucleus.CoreApp;
import com.nucleus.CoreApp.ClientApplication;
import com.nucleus.SimpleLogger;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.common.Type;
import com.nucleus.component.ComponentNode;
import com.nucleus.geometry.Mesh;
import com.nucleus.io.SceneSerializer;
import com.nucleus.mmi.MMIEventListener;
import com.nucleus.mmi.MMIPointerEvent;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.Layer;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;
import com.nucleus.renderer.Window;
import com.nucleus.scene.LayerNode;
import com.nucleus.scene.Node;
import com.nucleus.scene.Node.MeshType;
import com.nucleus.scene.NodeException;
import com.nucleus.scene.RootNode;
import com.nucleus.system.ComponentHandler;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TiledTexture2D;
import com.nucleus.vecmath.VecMath;
import com.nucleus.vecmath.Vector2D;
import com.super2k.supersprites.system.SuperSpriteSystem;
import com.super2k.supersprites.system.SuperSpriteSystem.EntityData;

public class SuperSprites implements MMIEventListener, RenderContextListener, ClientApplication {

    /**
     * The types that can be used to represent classes when importing/exporting
     * This is used as a means to decouple serialized name from implementing class.
     * 
     */
    public enum SuperSpritesClasses implements Type<Object> {
        superspritesystem(SuperSpriteSystem.class);

        private final Class<?> theClass;

        private SuperSpritesClasses(Class<?> theClass) {
            this.theClass = theClass;
        }

        @Override
        public Class<Object> getTypeClass() {
            return (Class<Object>) theClass;
        }

        @Override
        public String getName() {
            return name();
        }
    }

    protected final static String TILED_SPRITE_RENDERER_TAG = "TiledSpiteRenderer";
    /**
     * Must be set from scenetree
     */
    private int spritecount;
    private int currentSprite = 0;
    private Random random = new Random(System.currentTimeMillis());

    Window window;
    CoreApp coreApp;
    RootNode root;
    NucleusRenderer renderer;
    private int spriteFrames;
    private ComponentNode component;
    private SpriteComponent spriteComponent;
    private float[] pointerScale = new float[2];
    private LayerNode viewNode;

    public static float[] worldLimit;
    private float orthoLeft;
    private float orthoTop;
    // Ugly fix just to enable stopping sprites
    public static boolean process = false;

    public SuperSprites() {
        super();
    }

    @Override
    public void init(CoreApp coreApp) {
        if (!coreApp.getRenderer().isInitialized()) {
            throw new IllegalArgumentException("Renderer is not initialized!");
        }
        this.coreApp = coreApp;
        renderer = coreApp.getRenderer();
        coreApp.getInputProcessor().addMMIListener(this);
        coreApp.getRenderer().addContextListener(this);
        coreApp.getInputProcessor().getPointerScale(pointerScale);
    }

    @Override
    public void onInputEvent(MMIPointerEvent event) {

        switch (event.getAction()) {
            case INACTIVE:
                process = false;
                break;
            case MOVE:
                float[] move = event.getPointerData().getDelta(1);
                if (move != null && component != null) {
                    // component.getTransform().translate(move[0], move[1]);
                }
                releaseSprite(event.getPointerData().getCurrentPosition(), move);
                break;
            case ACTIVE:
                fetchSprites();
                releaseSprite(event.getPointerData().getCurrentPosition(), null);
                process = true;
                break;
            case ZOOM:
                Vector2D zoom = event.getZoom();
                float z = ((zoom.vector[Vector2D.MAGNITUDE] * zoom.vector[VecMath.X]))
                        * pointerScale[1];
                updateNodeScale(z);
                break;
            default:

        }
    }

    private void updateNodeScale(float zoom) {
        if (viewNode != null) {
            viewNode.getTransform().scale(zoom);
            float[] scale = viewNode.getTransform().getScale();
            SimpleLogger.d(SuperSprites.class, "Scale: " + scale[VecMath.X] + " zoom " + zoom);
            worldLimit[0] = (orthoLeft) / scale[VecMath.X];
            worldLimit[1] = (orthoTop) / scale[VecMath.Y];
            worldLimit[2] = (-orthoLeft) / scale[VecMath.X];
            worldLimit[3] = (-orthoTop) / scale[VecMath.Y];
        }
    }

    private void releaseSprite(float[] pos, float[] delta) {
        float[] scale = root.getViewNode(Layer.SCENE).getTransform().getScale();
        float x = (pos[0] / scale[VecMath.X]);
        float y = (pos[1] / scale[VecMath.Y]);
        spriteComponent.setSprite(currentSprite, x, y, 1, 1, 1, 1, 0, random.nextInt(spriteFrames));
        int index = currentSprite * spriteComponent.getSpritedataSize()
                + spriteComponent.getMapper().attributesPerVertex;

        spriteData[index + EntityData.MOVE_VECTOR_X.index] = 0;
        spriteData[index + EntityData.MOVE_VECTOR_Y.index] = 0;
        spriteData[index + EntityData.ELASTICITY.index] = 0.5f + random.nextFloat() * 0.5f;
        if (delta != null) {
            spriteData[index + EntityData.ROTATE_SPEED.index] = delta[0];
        }
        // s.setScale(0.8f + random.nextFloat() * 0.5f, 0.8f + random.nextFloat() * 0.5f);
        currentSprite++;
        if (currentSprite > spritecount - 1) {
            currentSprite = 0;
        }
    }

    /**
     * Fetches the framecount and number of sprites
     */
    private void fetchSprites() {
        if (component != null) {
            return;
        }
        if (viewNode == null) {
            viewNode = root.getViewNode(Layer.SCENE);
            updateNodeScale(0);
        }
        component = (ComponentNode) root.getNodeById("root")
                .getNodeByType(GraphicsEngineNodeType.spriteComponentNode.name());
        float xpos = worldLimit[0];
        float ypos = worldLimit[1];
        if (component != null) {
            spriteComponent = (SpriteComponent) component.getComponentById("spritecomponent");
            if (spriteComponent == null) {
                throw new IllegalArgumentException("Could not find component");
            }
            Mesh mesh = component.getMesh(MeshType.MAIN);
            // TODO A method to query the mesh how many frames it supports?
            // Maybe a way to fetch the texture from the resources?
            TiledTexture2D tiledTexture = (TiledTexture2D) mesh.getTexture(Texture2D.TEXTURE_0);
            spriteFrames = tiledTexture.getTileWidth() * tiledTexture.getTileHeight();
            spritecount = spriteComponent.getCount();
            System.out.println("Spritecount: " + spritecount + ", Spriteframes: " + spriteFrames);
            int spriteCount = spriteComponent.getCount();
            int height = (int) Math.sqrt(spriteCount);
            int width = (height);
            float deltay = ((worldLimit[1] - worldLimit[3]) / height) * 1f;
            float deltax = ((worldLimit[2] - worldLimit[0]) / width) * 1f;
        }
    }

    @Override
    public void contextCreated(int width, int height) {
        SimpleLogger.d(getClass(), "contextCreated()");
        window = Window.getInstance();
        if (root == null) {
            try {
                SimpleLogger.d(getClass(), "Loading scene");
                SceneSerializer sf = new GSONGraphicsEngineFactory(renderer, GSONGraphicsEngineFactory.getNodeFactory(),
                        GSONGraphicsEngineFactory.getMeshFactory(renderer),
                        Arrays.asList((Type<?>[]) SuperSpritesClasses.values()));
                root = sf.importScene("assets/scene.json");
                coreApp.setRootNode(root);
                coreApp.addPointerInput(root);
                /**
                 * TODO - this should be handled in a more generic way.
                 */
                Node scene = root.getNodeById("root");
                ViewFrustum vf = scene.getViewFrustum();
                if (vf != null) {
                    float[] values = vf.getValues();
                    float w = Math.abs(values[ViewFrustum.LEFT_INDEX] - values[ViewFrustum.RIGHT_INDEX]);
                    float h = Math.abs(values[ViewFrustum.TOP_INDEX] - values[ViewFrustum.BOTTOM_INDEX]);
                    // If y is going down then reverse y so that 0 is at bottom which is the same as OpenGL
                    coreApp.getInputProcessor().setPointerTransform(w / width, h / -height,
                            values[ViewFrustum.LEFT_INDEX],
                            values[ViewFrustum.TOP_INDEX]);
                    orthoLeft = values[ViewFrustum.LEFT_INDEX];
                    orthoTop = values[ViewFrustum.TOP_INDEX];
                    worldLimit = new float[] { orthoLeft, orthoTop, -orthoLeft, -orthoTop };
                } else {
                    // If y is going down then reverse y so that 0 is at bottom which is the same as OpenGL
                    coreApp.getInputProcessor().setPointerTransform((float) 1 / width, (float) 1 / -height, -0.5f,
                            0.5f);
                    throw new IllegalArgumentException();
                }
                // renderer.getRenderSettings().setClearFunction(GLES20.GL_COLOR_BUFFER_BIT);
                // renderer.getRenderSettings().setDepthFunc(GLES20.GL_NONE);
                // renderer.getRenderSettings().setCullFace(GLES20.GL_NONE);
                ComponentHandler.getInstance().initSystems(root, renderer);
                fetchSprites();
                try {
                    sf.exportScene(System.out, root);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (NodeException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void drawFrame() {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceLost() {
        // TODO Auto-generated method stub

    }
}
