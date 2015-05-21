package com.super2k.tiledspriteengine;

import java.io.IOException;
import java.util.Random;

import com.graphicsengine.charset.PlayfieldProgram;
import com.graphicsengine.json.JSONSceneFactory;
import com.graphicsengine.sprite.Sprite;
import com.graphicsengine.tiledsprite.TiledSpriteProgram;
import com.graphicsengine.tiledsprite.TiledSpriteSheet;
import com.nucleus.geometry.Mesh;
import com.nucleus.mmi.MMIEventListener;
import com.nucleus.mmi.MMIPointerEvent;
import com.nucleus.mmi.PointerInputProcessor;
import com.nucleus.opengl.GLES20Wrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.BaseRenderer;
import com.nucleus.renderer.BaseRenderer.FrameListener;
import com.nucleus.renderer.BaseRenderer.RenderContextListener;
import com.nucleus.renderer.Window;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.scene.Node;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TextureFactory;
import com.nucleus.texturing.TextureSetup;
import com.nucleus.vecmath.VecMath;
import com.nucleus.vecmath.Vector2D;

public class SuperSprites implements MMIEventListener, RenderContextListener, FrameListener {

    protected final static String TILED_SPRITE_RENDERER_TAG = "TiledSpiteRenderer";
    private final static String TEXTURE_NAME = "assets/af.png";
    private final static String CHARMAP_TEXTURE_NAME = "assets/charset.png";
    private final static int SPRITECOUNT = 1200;
    private final static int SPRITE_FRAMES_X = 5;
    private final static int SPRITE_FRAMES_Y = 1;
    private final static int START_XPOS = -1;
    private final static int START_YPOS = 0;
    private final static float SPRITE_ZPOS = 0;
    private final static float SPRITE_WIDTH = 0.05f;
    private final static float SPRITE_HEIGHT = 0.05f;
    private final static float ORTHO_LEFT = -0.5f;
    private final static float ORTHO_RIGHT = 0.5f;
    private final static float ORTHO_BOTTOM = 0.5f;
    private final static float ORTHO_TOP = -0.5f;
    private final static float ORTHO_NEAR = 0;
    private final static float ORTHO_FAR = 10;
    private final static float MIN_SCALE = 0.2f;
    private final static float MAX_SCALE = 2f;
    private final static float ZOOM_FACTOR = 0.5f;

    private final static float CHAR_WIDTH = 0.05f;
    private final static float CHAR_HEIGHT = 0.05f;
    private final static float CHAR_ZPOS = 0;
    private final static int CHAR_FRAMES_X = 32;
    private final static int CHAR_FRAMES_Y = 8;

    private final static int CHARMAP_WIDTH = 40;
    private final static int CHARMAP_HEIGHT = 40;
    private final static int CHARCOUNT = CHARMAP_WIDTH * CHARMAP_HEIGHT;

    BaseRenderer baseRenderer;
    Window window;
    private TiledSpriteSheet tiledSprites;

    private int currentSprite = 0;
    private Random random = new Random();

    private TiledSpriteProgram tiledSpriteProgram = new TiledSpriteProgram();
    private PlayfieldProgram charmapProgram = new PlayfieldProgram();
    private Texture2D texture;
    /**
     * The node containing sprites and chars, is root node of the scene.
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
        float[] scale = scene.getTransform().getScale();
        float x = ((pos[0] / window.getWidth() + ORTHO_LEFT) / scale[VecMath.X]);
        float y = ((pos[1] / window.getHeight() + ORTHO_TOP) / scale[VecMath.Y]);
        Sprite s = tiledSprites.getSpriteController().getSprites()[currentSprite];
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
        baseRenderer.createProgram(tiledSpriteProgram);
        baseRenderer.createProgram(charmapProgram);
        TextureSetup source = new TextureSetup(TEXTURE_NAME, RESOLUTION.HD, 3);
        texture = TextureFactory.createTexture(baseRenderer.getGLES(), baseRenderer.getImageFactory(), source);
        texture.getTexParams().setValues(GLES20.GL_LINEAR, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE,
                GLES20.GL_CLAMP_TO_EDGE);

        if (tiledSprites == null) {
            try {
                JSONSceneFactory sf = new JSONSceneFactory(baseRenderer);
                scene = sf.importScene("assets/scene.json", "scene");
                Node main = scene.getNodeById("main");
                main.addMesh(createSpriteController());
                try {
                    sf.exportScene(null, scene);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            tiledSprites.createMesh(tiledSpriteProgram, texture, SPRITE_WIDTH, SPRITE_HEIGHT, SPRITE_ZPOS,
                    SPRITE_FRAMES_X, SPRITE_FRAMES_Y);
            scene.addMesh(tiledSprites);
        }
        baseRenderer.getViewFrustum().setOrthoProjection(ORTHO_LEFT, ORTHO_RIGHT, ORTHO_BOTTOM, ORTHO_TOP, ORTHO_NEAR,
                ORTHO_FAR);

    }

    private Mesh createSpriteController() {
        tiledSprites = new TiledSpriteSheet(SPRITECOUNT);
        tiledSprites.createMesh(tiledSpriteProgram, texture, SPRITE_WIDTH, SPRITE_HEIGHT, SPRITE_ZPOS,
                SPRITE_FRAMES_X, SPRITE_FRAMES_Y);
        int frame = 0;
        AFSprite logic = new AFSprite();
        int maxFrames = SPRITE_FRAMES_X * SPRITE_FRAMES_Y - 1;
        for (Sprite sprite : tiledSprites.getSpriteController().getSprites()) {
            sprite.setFrame(frame++);
            sprite.logic = logic;
            if (frame > maxFrames) {
                frame = 0;
            }
            sprite.setPosition(START_XPOS, START_YPOS);
        }
        return tiledSprites;
    }

    @Override
    public void processFrame(float deltaTime) {
        for (Sprite sprite : tiledSprites.getSpriteController().getSprites()) {
            sprite.logic.process(sprite, deltaTime);
        }
        try {
            baseRenderer.render(scene);
        } catch (GLException e) {
            throw new RuntimeException(e);
        }
    }
}
