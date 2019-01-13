package com.super2k.supersprites;

import com.graphicsengine.component.SpriteAttributeComponent;
import com.graphicsengine.io.GSONGraphicsEngineFactory;
import com.graphicsengine.scene.GraphicsEngineNodeType;
import com.nucleus.CoreApp;
import com.nucleus.CoreApp.ClientApplication;
import com.nucleus.SimpleLogger;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.common.Type;
import com.nucleus.io.SceneSerializer;
import com.nucleus.mmi.MMIPointerInput;
import com.nucleus.mmi.MMIPointer;
import com.nucleus.mmi.core.CoreInput;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;
import com.nucleus.renderer.Window;
import com.nucleus.scene.ComponentNode;
import com.nucleus.scene.LayerNode;
import com.nucleus.scene.NodeException;
import com.nucleus.scene.RenderableNode;
import com.nucleus.scene.RootNode;
import com.nucleus.scene.RootNodeBuilder;
import com.nucleus.scene.RootNodeImpl;
import com.nucleus.system.ComponentHandler;
import com.nucleus.vecmath.AxisAngle;
import com.nucleus.vecmath.Matrix;
import com.nucleus.vecmath.Vec2;
import com.super2k.supersprites.system.SuperSpriteSystem;
import com.super2k.supersprites.system.SuperSpriteTestSystem;

public class SuperSprites implements MMIPointerInput, RenderContextListener, ClientApplication {

    public static final String NAME = "GLTF Render demo";
    public static final String VERSION = "0.2";
    public static final Renderers GL_VERSION = Renderers.GLES31;

    private float[] matrix = Matrix.setIdentity(Matrix.createMatrix(), 0);
    protected LayerNode scene;

    /**
     * The types that can be used to represent classes when importing/exporting
     * This is used as a means to decouple serialized name from implementing class.
     * 
     */
    public enum ClientClasses implements Type<Object> {
        /**
         * This is the main class implementing the ClientApplication
         */
        clientclass(SuperSprites.class),
        superspritesystem(SuperSpriteSystem.class),
        superspritetestsystem(SuperSpriteTestSystem.class);

        private final Class<?> theClass;

        private ClientClasses(Class<?> theClass) {
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

    Window window;
    CoreApp coreApp;
    RootNode root;
    ViewFrustum viewFrustum;
    NucleusRenderer renderer;
    private ComponentNode componentNode;
    private SpriteAttributeComponent spriteComponent;
    private SuperSpriteSystem system;

    public SuperSprites() {
        super();
    }

    @Override
    public void onInput(MMIPointer event) {

        float[] pos = event.getPointerData().getCurrentPosition();
        switch (event.getAction()) {
            case MOVE:
                float[] move = event.getPointerData().getDelta(1);
                if (move != null && componentNode != null) {
                    // component.getTransform().translate(move[0], move[1]);
                }
                releaseSprite(pos, move);
                // For testscene
                rotateTo(pos, "rotateobjects");
                break;
            case ACTIVE:
                fetchSprites();
                releaseSprite(pos, null);
                break;
            case ZOOM:
                Vec2 zoom = event.getZoom();
                float z = (zoom.vector[Vec2.MAGNITUDE] * zoom.vector[Vec2.X])
                        / CoreInput.getInstance().getPointerScaleY();
                root.getNodeById("scene", RenderableNode.class).getTransform().scale(z);
                break;
            default:

        }
    }

    protected void rotate(float[] move, String nodeId) {
        RenderableNode<?> node = root.getNodeById(nodeId, RenderableNode.class);
        if (node != null && node.getTransform() != null) {
            AxisAngle aa = node.getTransform().getAxisAngle();
            if (aa != null) {
                float[] values = aa.getValues();
                values[AxisAngle.ANGLE] += (move[1] / viewFrustum.getHeight() * 3.14);
            }
        }
    }

    protected void rotateTo(float[] pointerPos, String nodeId) {
        RenderableNode<?> node = root.getNodeById(nodeId, RenderableNode.class);
        if (node != null) {
            node.getTransform().setMatrixMode(true);
            Matrix.setIdentity(matrix, 0);
            Matrix.setRotateZTo2D(pointerPos, matrix);
            Matrix.translate(matrix, node.getTransform().getTranslate());
            node.getTransform().setMatrix(matrix);
        }

    }

    /**
     * 
     * @param pos
     */
    private void releaseSprite(float[] pos, float[] delta) {
        if (system != null) {
            system.releaseSprite(spriteComponent, pos);
        }
    }

    /**
     * Fetches the framecount and number of sprites
     */
    private void fetchSprites() {
        if (componentNode != null) {
            return;
        }
        componentNode = (ComponentNode) scene.getNodeByType(GraphicsEngineNodeType.spriteComponentNode.name(),
                GraphicsEngineNodeType.spriteComponentNode.theClass);
        if (componentNode != null) {
            spriteComponent = (SpriteAttributeComponent) componentNode.getComponentById("spritecomponent");
            if (spriteComponent == null) {
                throw new IllegalArgumentException("Could not find component");
            }
            system = (SuperSpriteSystem) ComponentHandler.getInstance().getSystem(spriteComponent);
        }
    }

    @Override
    public void contextCreated(int width, int height) {
        SimpleLogger.d(getClass(), "contextCreated()");
        window = Window.getInstance();
        if (root == null) {
            try {
                SimpleLogger.d(getClass(), "Loading scene");
                SceneSerializer<RootNode> serializer = GSONGraphicsEngineFactory.getInstance();
                if (!serializer.isInitialized()) {
                    serializer.init(renderer.getGLES(), ClientClasses.values());
                }
                // TODO Make a hook so that the name of the scene to load can be changed.
                root = serializer.importScene("assets/", "testscene.json", RootNodeBuilder.NUCLEUS_SCENE);
                // root = serializer.importScene("assets/", "scene.json");

                setup(width, height);

            } catch (NodeException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void initScene(RootNodeImpl root) {
        this.root = root;
    }

    protected void setup(int width, int height) {

        coreApp.setRootNode(root);
        coreApp.addPointerInput(root);
        scene = root.getNodeById("scene", LayerNode.class);
        viewFrustum = scene.getViewFrustum();
        float[] values = viewFrustum.getValues();
        // If y is going down then reverse y so that 0 is at bottom which is the same as OpenGL
        CoreInput.getInstance().setPointerTransform(viewFrustum.getWidth() / width,
                -viewFrustum.getHeight() / height, values[ViewFrustum.LEFT_INDEX],
                values[ViewFrustum.TOP_INDEX]);
        CoreInput.getInstance().setMaxPointers(20);
        fetchSprites();
    }

    @Override
    public void surfaceLost() {
        // TODO Auto-generated method stub

    }

    @Override
    public void init(CoreApp coreApp) {
        if (!coreApp.getRenderer().isInitialized()) {
            throw new IllegalArgumentException("Renderer is not initialized!");
        }
        this.coreApp = coreApp;
        renderer = coreApp.getRenderer();
        CoreInput.getInstance().addMMIListener(this);
        coreApp.getRenderer().addContextListener(this);
    }

    @Override
    public void beginFrame(float deltaTime) {
        // TODO Auto-generated method stub

    }

    @Override
    public void endFrame(float deltaTime) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getAppName() {
        return NAME;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

}
