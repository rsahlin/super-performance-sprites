package com.super2k.tiledspriteengine;

import java.util.Random;

import com.nucleus.mmi.MMIEventListener;
import com.nucleus.mmi.MMIPointerEvent;
import com.nucleus.mmi.PointerInputProcessor;
import com.nucleus.opengl.GLES20Wrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.BaseRenderer;
import com.nucleus.renderer.BaseRenderer.FrameListener;
import com.nucleus.renderer.BaseRenderer.RenderContextListener;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.scene.Node;
import com.nucleus.sprite.Sprite;
import com.nucleus.texturing.Image;
import com.nucleus.texturing.Texture2D;
import com.nucleus.tiledsprite.TiledSpriteController;
import com.nucleus.tiledsprite.TiledSpriteProgram;
import com.nucleus.vecmath.VecMath;
import com.nucleus.vecmath.Vector2D;

public class SuperSprites implements MMIEventListener, RenderContextListener, FrameListener {

    protected final static String TILED_SPRITE_RENDERER_TAG = "TiledSpiteRenderer";
    private final static String TEXTURE_NAME = "assets/af.png";
    public final static int SPRITECOUNT = 1200;
    public final static int SPRITE_FRAMES_X = 5;
    public final static int SPRITE_FRAMES_Y = 1;
    public final static int START_XPOS = -1;
    public final static int START_YPOS = 0;
    public final static float SPRITE_WIDTH = 0.05f;
    public final static float SPRITE_HEIGHT = 0.05f;
    public final static float ORTHO_LEFT = -0.5f;
    public final static float ORTHO_RIGHT = 0.5f;
    public final static float ORTHO_BOTTOM = 0.5f;
    public final static float ORTHO_TOP = -0.5f;
    public final static float ORTHO_NEAR = 0;
    public final static float ORTHO_FAR = 10;
    public final static float MIN_SCALE = 0.02f;
    public final static float MAX_SCALE = 4f;
    public final static float ZOOM_FACTOR = 0.5f;

    BaseRenderer baseRenderer;
    private TiledSpriteController spriteController;

    private int currentSprite = 0;
    private Random random = new Random();

    private TiledSpriteProgram tiledSpriteProgram = new TiledSpriteProgram();
    private Image[] textureImg;
    private Texture2D texture;
    private int textureID;
    /**
     * The node containing sprites is the only node in the scene.
     */
    private Node scene;

    public final static int DEFAULT_MAX_X = 1;
    public final static int DEFAULT_MAX_Y = 1;
    public final static float[] worldLimit = new float[] { ORTHO_LEFT, ORTHO_TOP, DEFAULT_MAX_X + ORTHO_LEFT,
            DEFAULT_MAX_Y + ORTHO_TOP };

    public SuperSprites(BaseRenderer baseRenderer, PointerInputProcessor inputProcessor) {
        inputProcessor.addMMIListener(this);
        this.baseRenderer = baseRenderer;
        baseRenderer.addContextListener(this);
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
            float z = ((zoom.vector[Vector2D.MAGNITUDE] * zoom.vector[VecMath.X]) / baseRenderer.getWidth())
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
        float[] scale = scene.getTransform().getScale();
        float x = ((pos[0] / baseRenderer.getWidth() + ORTHO_LEFT) / scale[VecMath.X]);
        float y = ((pos[1] / baseRenderer.getHeight() + ORTHO_TOP) / scale[VecMath.Y]);
        Sprite s = spriteController.getSprites()[currentSprite];
        s.setPosition(x, y);
        s.setMoveVector(0, 0, 0);
        s.floatData[AFSprite.ELASTICITY] = 0.95f - (random.nextFloat() / 10);
        s.moveVector.setNormalized((pos[0] - start[0]) / baseRenderer.getWidth(), 0);
        s.floatData[AFSprite.ROTATE_SPEED] = s.moveVector.vector[VecMath.X];
        currentSprite++;
        if (currentSprite > SPRITECOUNT - 1) {
            currentSprite = 0;
        }
    }

    @Override
    public void contextCreated(int width, int height) {

        baseRenderer.createProgram(tiledSpriteProgram);

        texture = baseRenderer.createTexture(TEXTURE_NAME, 3, RESOLUTION.HD);
        texture.setValues(GLES20.GL_LINEAR, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_CLAMP_TO_EDGE);

        if (spriteController == null) {
            spriteController = new TiledSpriteController(SPRITECOUNT);
            spriteController.createMesh(tiledSpriteProgram, texture, SPRITE_WIDTH, SPRITE_HEIGHT, SPRITE_FRAMES_X,
                    SPRITE_FRAMES_Y);
            int frame = 0;
            AFSprite logic = new AFSprite();
            int maxFrames = SPRITE_FRAMES_X * SPRITE_FRAMES_Y - 1;
            for (Sprite sprite : spriteController.getSprites()) {
                sprite.setFrame(frame++);
                sprite.logic = logic;
                if (frame > maxFrames) {
                    frame = 0;
                }
                sprite.setPosition(START_XPOS, START_YPOS);
            }
            scene = new Node(spriteController.getMesh());
        } else {
            spriteController.createMesh(tiledSpriteProgram, texture, SPRITE_WIDTH, SPRITE_HEIGHT, SPRITE_FRAMES_X,
                    SPRITE_FRAMES_Y);
            scene.setMesh(spriteController.getMesh());
        }
        baseRenderer.getViewFrustum().setOrthoProjection(ORTHO_LEFT, ORTHO_RIGHT, ORTHO_BOTTOM, ORTHO_TOP, ORTHO_NEAR,
                ORTHO_FAR);

    }

    @Override
    public void processFrame(float deltaTime) {
        baseRenderer.render();
        for (Sprite sprite : spriteController.getSprites()) {
            sprite.logic.process(sprite, deltaTime);
        }
        try {
            baseRenderer.render(scene);
        } catch (GLException e) {
            throw new RuntimeException(e);
        }
    }
}
