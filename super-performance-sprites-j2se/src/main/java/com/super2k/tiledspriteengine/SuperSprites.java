package com.super2k.tiledspriteengine;

import java.io.IOException;
import java.util.Random;

import com.graphicsengine.io.GSONGraphicsEngineFactory;
import com.graphicsengine.scene.GraphicsEngineNodeType;
import com.graphicsengine.scene.SceneSerializerFactory;
import com.graphicsengine.sprite.Sprite;
import com.graphicsengine.sprite.SpriteControllerFactory;
import com.graphicsengine.spritemesh.SpriteMeshController;
import com.nucleus.CoreApp;
import com.nucleus.CoreApp.ClientApplication;
import com.nucleus.actor.J2SELogicProcessor;
import com.nucleus.io.SceneSerializer;
import com.nucleus.mmi.MMIEventListener;
import com.nucleus.mmi.MMIPointerEvent;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.FrameListener;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;
import com.nucleus.renderer.Window;
import com.nucleus.scene.Node;
import com.nucleus.scene.RootNode;
import com.nucleus.scene.RootNode.Scenes;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TiledTexture2D;
import com.nucleus.vecmath.VecMath;
import com.nucleus.vecmath.Vector2D;

public class SuperSprites implements MMIEventListener, RenderContextListener, FrameListener, ClientApplication {

    protected final static String TILED_SPRITE_RENDERER_TAG = "TiledSpiteRenderer";
    private int SPRITECOUNT = 1200;
    // TODO How to find these values from the scene
    private final static float ORTHO_LEFT = -0.5f;
    private final static float ORTHO_RIGHT = 0.5f;
    private final static float ORTHO_BOTTOM = 0.5f;
    private final static float ORTHO_TOP = -0.5f;
    private final static float ORTHO_NEAR = 0;
    private final static float ORTHO_FAR = 10;
    private final static float MIN_SCALE = 0.2f;
    private final static float MAX_SCALE = 2f;
    private final static float ZOOM_FACTOR = 0.5f;

    Window window;
    CoreApp coreApp;
    NucleusRenderer renderer;
    private SpriteMeshController spriteController;
    private int spriteFrames;

    private int currentSprite = 0;
    private Random random = new Random();

    public final static int DEFAULT_MAX_X = 1;
    public final static int DEFAULT_MAX_Y = 1;
    public final static float[] worldLimit = new float[] { ORTHO_LEFT, ORTHO_TOP, DEFAULT_MAX_X + ORTHO_LEFT,
            DEFAULT_MAX_Y + ORTHO_TOP };

    public SuperSprites() {
        super();
    }

    @Override
    public void init(CoreApp coreApp) {
        this.coreApp = coreApp;
        renderer = coreApp.getRenderer();
        coreApp.getInputProcessor().addMMIListener(this);
        coreApp.getRenderer().addContextListener(this);
        coreApp.getRenderer().addFrameListener(this);
        SpriteControllerFactory.setActorResolver(new SuperSpriteResolver());
    }

    @Override
    public void inputEvent(MMIPointerEvent event) {

        switch (event.getAction()) {
        case INACTIVE:
            break;
        case MOVE:
        case ACTIVE:
            float[] pos = event.getPointerData().getCurrentPosition();
            float[] start = event.getPointerData().getFirstPosition();
            releaseSprite(start, pos);
            break;
        case ZOOM:
            Vector2D zoom = event.getZoom();
            float z = ((zoom.vector[Vector2D.MAGNITUDE] * zoom.vector[VecMath.X]) / window.getWidth())
                    * ZOOM_FACTOR;
            renderer.getScene().getTransform().scale(z);
            float[] scale = renderer.getScene().getTransform().getScale();
            System.out.println("scale: " + scale[VecMath.X]);
            worldLimit[0] = (ORTHO_LEFT) / scale[VecMath.X];
            worldLimit[1] = (ORTHO_TOP) / scale[VecMath.Y];
            worldLimit[2] = (DEFAULT_MAX_X + ORTHO_LEFT) / scale[VecMath.X];
            worldLimit[3] = (DEFAULT_MAX_Y + ORTHO_TOP) / scale[VecMath.Y];
            break;
        default:

        }
    }

    private void releaseSprite(float[] start, float[] pos) {
        if (spriteController == null) {
            return;
        }
        float[] scale = renderer.getScene().getTransform().getScale();
        float x = ((pos[0] / window.getWidth() + ORTHO_LEFT) / scale[VecMath.X]);
        float y = ((pos[1] / window.getHeight() + ORTHO_TOP) / scale[VecMath.Y]);
        Sprite s = spriteController.getSprites()[currentSprite];
        s.setPosition(x, y);
        s.setMoveVector(0, 0, 0);
        s.floatData[AFSprite.ELASTICITY] = 0.95f - (random.nextFloat() / 10);
        s.moveVector.setNormalized((pos[0] - start[0]) / window.getWidth(), 0);
        s.floatData[AFSprite.ROTATE_SPEED] = s.moveVector.vector[VecMath.X];
        s.floatData[Sprite.FRAME] = random.nextInt(spriteFrames);
        currentSprite++;
        if (currentSprite > SPRITECOUNT - 1) {
            currentSprite = 0;
        }
    }

    @Override
    public void contextCreated(int width, int height) {
        window = Window.getInstance();

        if (spriteController == null) {
            try {
                SceneSerializer sf = SceneSerializerFactory.getSerializer(GSONGraphicsEngineFactory.class.getName());
                sf.setRenderer(renderer);
                RootNode scene = sf.importScene("assets/scene.json");
                Node credit = scene.getScene(Scenes.credit);
                Node game = scene.getScene(Scenes.game);
                Node sprites = game.getNodeByType(GraphicsEngineNodeType.tiledSpriteController.name());
                renderer.setScene(game);
                renderer.getRenderSettings().setClearFunction(GLES20.GL_COLOR_BUFFER_BIT);
                renderer.getRenderSettings().setDepthFunc(GLES20.GL_NONE);
                renderer.getRenderSettings().setCullFace(GLES20.GL_NONE);

                if (sprites != null && sprites instanceof SpriteMeshController) {
                    spriteController = (SpriteMeshController) sprites;
                    TiledTexture2D tiledTexture = spriteController.getSpriteSheet()
                            .getTiledTexture(Texture2D.TEXTURE_0);
                    spriteFrames = tiledTexture.getTileWidth() * tiledTexture.getTileHeight();
                    SPRITECOUNT = spriteController.getCount();

                }
                coreApp.setLogicProcessor(new J2SELogicProcessor());
                try {
                    sf.exportScene(System.out, scene);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        } else {
            System.err.println("NOT IMPLEMENTED");
        }
    }

    @Override
    public void processFrame(float deltaTime) {
        // for (Sprite sprite : spriteController.getSprites()) {
        // sprite.logic.process(sprite, deltaTime);
        // }
    }

    @Override
    public void updateGLData() {
        for (Sprite sprite : spriteController.getSprites()) {
            sprite.prepare();
        }
    }

}
