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
import com.nucleus.mmi.MMIEventListener;
import com.nucleus.mmi.MMIPointerEvent;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;
import com.nucleus.renderer.Window;
import com.nucleus.scene.ComponentNode;
import com.nucleus.scene.Node.NodeTypes;
import com.nucleus.scene.NodeException;
import com.nucleus.scene.RootNode;
import com.nucleus.system.ComponentHandler;
import com.nucleus.vecmath.Vector2D;
import com.super2k.supersprites.system.SuperSpriteSystem;

public class SuperSprites implements MMIEventListener, RenderContextListener, ClientApplication {

    public static final Renderers GL_VERSION = Renderers.GLES20;

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
        superspritesystem(SuperSpriteSystem.class);

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
    private float[] pointerScale = new float[2];

    public SuperSprites() {
        super();
    }

    @Override
    public void onInputEvent(MMIPointerEvent event) {

        switch (event.getAction()) {
            case MOVE:
                float[] move = event.getPointerData().getDelta(1);
                if (move != null && componentNode != null) {
                    // component.getTransform().translate(move[0], move[1]);
                }
                releaseSprite(event.getPointerData().getCurrentPosition(), move);
                break;
            case ACTIVE:
                fetchSprites();
                releaseSprite(event.getPointerData().getCurrentPosition(), null);
                break;
            case ZOOM:
                Vector2D zoom = event.getZoom();
                float z = (zoom.vector[Vector2D.MAGNITUDE] * zoom.vector[Vector2D.X]) / viewFrustum.getHeight();
                root.getNodeById("scene").getTransform().scale(z);
                break;
            default:

        }
    }

    /**
     * 
     * @param pos
     */
    private void releaseSprite(float[] pos, float[] delta) {
        system.releaseSprite(spriteComponent, pos);
    }

    /**
     * Fetches the framecount and number of sprites
     */
    private void fetchSprites() {
        if (componentNode != null) {
            return;
        }
        componentNode = (ComponentNode) root.getNodeById("scene")
                .getNodeByType(GraphicsEngineNodeType.spriteComponentNode.name());
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
                SceneSerializer serializer = GSONGraphicsEngineFactory.getInstance();
                if (!serializer.isInitialized()) {
                    serializer.init(renderer, GSONGraphicsEngineFactory.getNodeFactory(),
                            GSONGraphicsEngineFactory.getMeshFactory(renderer), ClientClasses.values());
                }
                // root = serializer.importScene("assets/testscene.json");
                // TODO Make a hook so that the name of the scene to load can be changed.
                root = serializer.importScene("assets/scene.json");
                coreApp.setRootNode(root);
                coreApp.addPointerInput(root);
                viewFrustum = root.getNodeByType(NodeTypes.layernode.name()).getViewFrustum();
                float[] values = viewFrustum.getValues();
                // If y is going down then reverse y so that 0 is at bottom which is the same as OpenGL
                coreApp.getInputProcessor().setPointerTransform(viewFrustum.getWidth() / width,
                        -viewFrustum.getHeight() / height, values[ViewFrustum.LEFT_INDEX],
                        values[ViewFrustum.TOP_INDEX]);
                coreApp.getInputProcessor().setMaxPointers(20);
                fetchSprites();
            } catch (NodeException e) {
                throw new RuntimeException(e);
            }
        }
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
        coreApp.getInputProcessor().addMMIListener(this);
        coreApp.getRenderer().addContextListener(this);
        coreApp.getInputProcessor().getPointerScale(pointerScale);
    }

    @Override
    public void beginFrame(float deltaTime) {
        // TODO Auto-generated method stub

    }

    @Override
    public void endFrame(float deltaTime) {
        // TODO Auto-generated method stub

    }

}
