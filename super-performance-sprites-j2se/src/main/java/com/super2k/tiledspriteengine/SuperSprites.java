package com.super2k.tiledspriteengine;

import java.io.IOException;

import com.graphicsengine.component.SpriteComponent;
import com.graphicsengine.io.GSONGraphicsEngineFactory;
import com.graphicsengine.scene.GraphicsEngineNodeType;
import com.graphicsengine.scene.SceneSerializerFactory;
import com.nucleus.CoreApp;
import com.nucleus.CoreApp.ClientApplication;
import com.nucleus.SimpleLogger;
import com.nucleus.actor.ComponentNode;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.geometry.Mesh;
import com.nucleus.io.SceneSerializer;
import com.nucleus.mmi.MMIEventListener;
import com.nucleus.mmi.MMIPointerEvent;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.Layer;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;
import com.nucleus.renderer.Window;
import com.nucleus.scene.Node;
import com.nucleus.scene.NodeException;
import com.nucleus.scene.RootNode;
import com.nucleus.scene.ViewNode;
import com.nucleus.system.ComponentHandler;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TiledTexture2D;
import com.nucleus.vecmath.VecMath;
import com.nucleus.vecmath.Vector2D;

public class SuperSprites implements MMIEventListener, RenderContextListener, ClientApplication {

    protected final static String TILED_SPRITE_RENDERER_TAG = "TiledSpiteRenderer";
    /**
     * Must be set from scenetree
     */
    private int spritecount;

    Window window;
    CoreApp coreApp;
    RootNode root;
    NucleusRenderer renderer;
    private int spriteFrames;
    private ComponentNode component;
    private float[] pointerScale = new float[2];
    private ViewNode viewNode;

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
        this.coreApp = coreApp;
        renderer = coreApp.getRenderer();
        coreApp.getInputProcessor().addMMIListener(this);
        coreApp.getRenderer().addContextListener(this);
        coreApp.getInputProcessor().getPointerScale(pointerScale);
    }

    @Override
    public void inputEvent(MMIPointerEvent event) {

        switch (event.getAction()) {
        case INACTIVE:
            process = false;
            break;
        case MOVE:
            float[] move = event.getPointerData().getDelta(1);
            if (move != null && component != null) {
                component.getTransform().translate(move[0], move[1]);
            }
        case ACTIVE:
            fetchSprites();
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
            viewNode.getView().scale(zoom);
            float[] scale = viewNode.getView().getScale();
            SimpleLogger.d(SuperSprites.class, "Scale: " + scale[VecMath.X] + " zoom " + zoom);
            worldLimit[0] = (orthoLeft) / scale[VecMath.X];
            worldLimit[1] = (orthoTop) / scale[VecMath.Y];
            worldLimit[2] = (-orthoLeft) / scale[VecMath.X];
            worldLimit[3] = (-orthoTop) / scale[VecMath.Y];
        }
    }

    private void releaseSprite(float[] pos, float[] delta) {
        float[] scale = root.getViewNode(Layer.SCENE).getView().getScale();
        float x = (pos[0] / scale[VecMath.X]);
        float y = (pos[1] / scale[VecMath.Y]);
        /*
         * Sprite s = spriteNode.getSprites()[currentSprite];
         * // s.setPosition(x, y, 0);
         * s.floatData[Sprite.X_POS] = x;
         * s.floatData[Sprite.Y_POS] = y;
         * s.setMoveVector(0, 0, 0);
         * s.floatData[BounceSprite.ELASTICITY] = 1f - (random.nextFloat() / 5);
         * if (delta != null) {
         * s.moveVector.setNormalized((delta[0] * 30) / scale[0], 0);
         * } else {
         * s.moveVector.setNormalized(0, 0);
         * }
         * s.floatData[BounceSprite.ROTATE_SPEED] = s.moveVector.vector[VecMath.X];
         * s.setFrame(random.nextInt(spriteFrames));
         * s.setScale(0.8f + random.nextFloat() * 0.5f, 0.8f + random.nextFloat() * 0.5f);
         * 
         * currentSprite++;
         * if (currentSprite > spritecount - 1) {
         * currentSprite = 0;
         * }
         */
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
        component = (ComponentNode) root.getScene()
                .getNodeByType(GraphicsEngineNodeType.spriteComponentNode.name());
        float xpos = worldLimit[0];
        float ypos = worldLimit[1];
        if (component != null) {
            SpriteComponent c = (SpriteComponent) component.getComponentById("spritecomponent");
            if (c == null) {
                throw new IllegalArgumentException("Could not find component");
            }
            Mesh mesh = component.getMeshes().get(0);
            // TODO A method to query the mesh how many frames it supports?
            // Maybe a way to fetch the texture from the resources?
            TiledTexture2D tiledTexture = (TiledTexture2D) mesh.getTexture(Texture2D.TEXTURE_0);
            spriteFrames = tiledTexture.getTileWidth() * tiledTexture.getTileHeight();
            spritecount = c.getCount();
            System.out.println("Spritecount: " + spritecount + ", Spriteframes: " + spriteFrames);
            int spriteCount = c.getCount();
            int height = (int) Math.sqrt(spriteCount);
            int width = (height);
            float deltay = ((worldLimit[1] - worldLimit[3]) / height) * 1f;
            float deltax = ((worldLimit[2] - worldLimit[0]) / width) * 1f;
            initSprites(c, xpos, ypos, deltax, deltay, width);
        }
    }

    private void initSprites(SpriteComponent spriteComponent, float xpos, float ypos, float deltax, float deltay,
            int width) {
        int x = 0;
        float startX = xpos;
        int frame = 0;
        int spriteCount = spriteComponent.getCount();
        for (int i = 0; i < spriteCount; i++) {
            spriteComponent.setPosition(i, xpos, ypos, 0);
            spriteComponent.setScale(i, 1, 1);
            spriteComponent.setFrame(i, frame++);
            spriteComponent.setRotateSpeed(i, 0.5f);
            spriteComponent.setElasticity(i, 1);
            if (frame > spriteFrames) {
                frame = 0;
            }
            xpos += deltax;
            x++;
            if (x >= width) {
                x = 0;
                xpos = startX;
                ypos -= deltay;
            }
        }
    }

    @Override
    public void contextCreated(int width, int height) {
        SimpleLogger.d(getClass(), "contextCreated()");
        window = Window.getInstance();
        // Todo - should have a method indicating that context is lost
        if (root == null) {
            try {
                SimpleLogger.d(getClass(), "Loading scene");
                SceneSerializer sf = SceneSerializerFactory.getSerializer(GSONGraphicsEngineFactory.class.getName(),
                        renderer, GSONGraphicsEngineFactory.getNodeFactory(),
                        GSONGraphicsEngineFactory.getMeshFactory());
                root = sf.importScene("assets/scene.json");
                coreApp.setRootNode(root);
                coreApp.addPointerInput(root);
                /**
                 * TODO - this should be handled in a more generic way.
                 */
                Node scene = root.getScene();
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

                renderer.getRenderSettings().setClearFunction(GLES20.GL_COLOR_BUFFER_BIT);
                renderer.getRenderSettings().setDepthFunc(GLES20.GL_NONE);
                renderer.getRenderSettings().setCullFace(GLES20.GL_NONE);
                ComponentHandler.getInstance().initSystems(root, renderer);
                fetchSprites();
                try {
                    sf.exportScene(System.out, root);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (NodeException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
