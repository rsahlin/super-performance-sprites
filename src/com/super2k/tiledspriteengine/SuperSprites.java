package com.super2k.tiledspriteengine;

import java.io.IOException;
import java.util.Random;

import com.gameengine.jsongameserializer.JSONSceneFactory;
import com.graphicsengine.scene.SceneSerializer;
import com.graphicsengine.scene.SceneSerializerFactory;
import com.graphicsengine.sprite.Sprite;
import com.graphicsengine.sprite.SpriteControllerFactory;
import com.graphicsengine.tiledsprite.TiledSpriteController;
import com.nucleus.mmi.MMIEventListener;
import com.nucleus.mmi.MMIPointerEvent;
import com.nucleus.mmi.PointerInputProcessor;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.FrameListener;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;
import com.nucleus.renderer.Window;
import com.nucleus.scene.Node;
import com.nucleus.vecmath.VecMath;
import com.nucleus.vecmath.Vector2D;

public class SuperSprites implements MMIEventListener, RenderContextListener, FrameListener {

    protected final static String TILED_SPRITE_RENDERER_TAG = "TiledSpiteRenderer";
    private final static int SPRITECOUNT = 1200;
    private final static float ORTHO_LEFT = -0.5f;
    private final static float ORTHO_RIGHT = 0.5f;
    private final static float ORTHO_BOTTOM = 0.5f;
    private final static float ORTHO_TOP = -0.5f;
    private final static float ORTHO_NEAR = 0;
    private final static float ORTHO_FAR = 10;
    private final static float MIN_SCALE = 0.2f;
    private final static float MAX_SCALE = 2f;
    private final static float ZOOM_FACTOR = 0.5f;

    NucleusRenderer renderer;
    Window window;
    private TiledSpriteController spriteController;

    private int currentSprite = 0;
    private Random random = new Random();

    /**
     * The node containing sprites and chars, is root node of the scene.
     */
    private Node scene;

    public final static int DEFAULT_MAX_X = 1;
    public final static int DEFAULT_MAX_Y = 1;
    public final static float[] worldLimit = new float[] { ORTHO_LEFT, ORTHO_TOP, DEFAULT_MAX_X + ORTHO_LEFT,
            DEFAULT_MAX_Y + ORTHO_TOP };

    public SuperSprites(NucleusRenderer renderer, PointerInputProcessor inputProcessor) {
        // TODO remove constructor and use dependency injection.
        inputProcessor.addMMIListener(this);
        this.renderer = renderer;
        renderer.addContextListener(this);
        SpriteControllerFactory.setLogicResolver(new SuperSpriteResolver());
    }

    @Override
    public void inputEvent(MMIPointerEvent event) {
        float[] delta = event.getPointerData().getDelta(1);

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
            float[] scale = scene.getTransform().getScale();
            scale[VecMath.X] += (z * scale[VecMath.X]);
            scale[VecMath.Y] += (z * scale[VecMath.Y]);
            Vector2D.min(scale, MIN_SCALE);
            Vector2D.max(scale, MAX_SCALE);
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
        float[] scale = scene.getTransform().getScale();
        float x = ((pos[0] / window.getWidth() + ORTHO_LEFT) / scale[VecMath.X]);
        float y = ((pos[1] / window.getHeight() + ORTHO_TOP) / scale[VecMath.Y]);
        Sprite s = spriteController.getSprites()[currentSprite];
        s.setPosition(x, y);
        s.setMoveVector(0, 0, 0);
        s.floatData[AFSprite.ELASTICITY] = 0.95f - (random.nextFloat() / 10);
        s.moveVector.setNormalized((pos[0] - start[0]) / window.getWidth(), 0);
        s.floatData[AFSprite.ROTATE_SPEED] = s.moveVector.vector[VecMath.X];
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
                SceneSerializer sf = SceneSerializerFactory.getSerializer(JSONSceneFactory.class.getName());
                sf.setRenderer(renderer);
                scene = sf.importScene("assets/scene.json", "scene");
                Node sprites = scene.getNodeById("tiledsprites");
                if (sprites != null && sprites instanceof TiledSpriteController) {
                    spriteController = (TiledSpriteController) sprites;
                }
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
        renderer.getViewFrustum().setOrthoProjection(ORTHO_LEFT, ORTHO_RIGHT, ORTHO_BOTTOM, ORTHO_TOP, ORTHO_NEAR,
                ORTHO_FAR);

    }

    @Override
    public void processFrame(float deltaTime) {
        for (Sprite sprite : spriteController.getSprites()) {
            sprite.logic.process(sprite, deltaTime);
        }
        try {
            renderer.render(scene);
        } catch (GLException e) {
            throw new RuntimeException(e);
        }
    }
}
