package com.super2k.tiledspriteengine;

import java.io.IOException;
import java.util.Random;

import com.nucleus.android.AndroidGLUtils;
import com.nucleus.common.TimeKeeper;
import com.nucleus.geometry.VertexBuffer;
import com.nucleus.matrix.MatrixEngine;
import com.nucleus.mmi.MMIEventListener;
import com.nucleus.mmi.MMIPointerEvent;
import com.nucleus.mmi.PointerInputProcessor;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLES20Wrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.renderer.BaseRenderer;
import com.nucleus.resource.ResourceBias;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.texturing.Image;
import com.nucleus.texturing.ImageFactory;
import com.nucleus.texturing.Texture2D;
import com.nucleus.transform.Vector2D;
import com.super2k.tiledspriteengine.sprite.Sprite;
import com.super2k.tiledspriteengine.sprite.TiledSprite;
import com.super2k.tiledspriteengine.sprite.TiledSpriteController;

public class TiledSpriteRenderer extends BaseRenderer implements MMIEventListener {

    protected final static String TILED_SPRITE_RENDERER_TAG = "TiledSpiteRenderer";
    public final static int SPRITECOUNT = 1200;
    public final static int SPRITE_FRAMES_X = 5;
    public final static int SPRITE_FRAMES_Y = 1;
    public final static int START_XPOS = -1;
    public final static int START_YPOS = 0;
    public final static float SPRITE_WIDTH = 0.05f;
    public final static float SPRITE_HEIGHT = 0.05f;

    private PointerInputProcessor inputProcessor;
    private ShaderProgram tiledSpriteProgram;
    private Image[] textureImg;
    private Texture2D texture;
    private int textureID;
    private TiledSpriteController spriteController;

    private int currentSprite = 0;
    private TimeKeeper timeKeeper = new TimeKeeper(30);
    private Random random = new Random();

    public TiledSpriteRenderer(GLES20Wrapper gles, ImageFactory imageFactory, MatrixEngine matrixEngine,
            PointerInputProcessor inputProcessor) {
        super(gles, imageFactory, matrixEngine);
        this.inputProcessor = inputProcessor;
        inputProcessor.addMMIListener(this);
    }

    /**
     * Create the programs for the fractal renderer.
     * 
     */
    private void createPrograms() {
        ClassLoader classLoader = getClass().getClassLoader();
        tiledSpriteProgram = new TiledSpriteProgram();
        try {
            tiledSpriteProgram.prepareProgram(gles,
                    classLoader.getResourceAsStream(TiledSpriteProgram.VERTEX_SHADER_NAME),
                    classLoader.getResourceAsStream(TiledSpriteProgram.FRAGMENT_SHADER_NAME));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GLException e) {
            throw new RuntimeException(e.toString());
        }
    }

    @Override
    public void beginFrame() {
        super.beginFrame();
        /**
         * For now handling of logic is done before rendering
         * TODO: Add logic handler separated from rendering.
         */

        float deltaTime = timeKeeper.update();
        if (timeKeeper.getSampleDuration() > 3) {
            System.out.println(TILED_SPRITE_RENDERER_TAG + ": Average FPS: " + timeKeeper.sampleFPS());
        }
        for (TiledSprite sprite : spriteController.getSprites()) {
            sprite.process(deltaTime);
        }

    }

    /**
     * The main render method, all drawing shall take place here.
     */
    @Override
    public void render() {
        super.render();
        try {
            GLUtils.handleError(gles, "Error");
            gles.glClearColor(0, 0f, 0.4f, 1.0f);
            gles.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            gles.glDisable(GLES20.GL_DEPTH_TEST);
            gles.glDisable(GLES20.GL_CULL_FACE);
            gles.glUseProgram(tiledSpriteProgram.getProgram());
            AndroidGLUtils.handleError("Program error");

            VertexBuffer positions = spriteController.getMesh().getVerticeBuffer(1);
            positions.setArray(spriteController.getData(), 0, 0, SPRITECOUNT
                    * TiledSpriteController.SPRITE_ATTRIBUTE_DATA);

            renderMesh(spriteController.getMesh());
            GLUtils.handleError(gles, "Error");

        } catch (GLException e) {
            throw new RuntimeException(e);
        }

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
            float z = ((zoom.vector[Vector2D.MAGNITUDE] * zoom.vector[Vector2D.X_AXIS]) / 1000);
            break;
        default:

        }
    }

    private void releaseSprite(float[] start, float[] pos) {
        pos[0] = pos[0] / width;
        pos[1] = pos[1] / height;
        Sprite s = spriteController.getSprites()[currentSprite];
        s.setPosition(pos[0], pos[1]);
        s.floatData[Sprite.GRAVITY_Y] = 0;
        s.floatData[Sprite.ELASTICITY] = 0.95f - (random.nextFloat() / 10);
        s.moveVector.setNormalized(pos[0] - start[0], 0);
        s.floatData[Sprite.ROTATE_SPEED] = s.moveVector.vector[Vector2D.X_AXIS];
        currentSprite++;
        if (currentSprite > SPRITECOUNT - 1) {
            currentSprite = 0;
        }
    }

    @Override
    public void GLContextCreated(int width, int height) {
        super.GLContextCreated(width, height);
        System.out.println(TILED_SPRITE_RENDERER_TAG + ": GLContextCreated: " + width + ", " + height);
        createPrograms();

        int[] textures = new int[1];
        gles.glGenTextures(1, textures, 0);

        textureID = textures[0];

        textureImg = loadTextureMIPMAP("assets/af.png", 1080, 3);

        try {
            uploadTextures(GLES20.GL_TEXTURE0, textureID, textureImg);
        } catch (GLException e) {
            throw new IllegalArgumentException(e);
        }
        texture = new Texture2D(textureID, textureImg[0].getWidth(), textureImg[0].getHeight());
        texture.setValues(GLES20.GL_LINEAR, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_CLAMP_TO_EDGE);
        spriteController = new TiledSpriteController(SPRITECOUNT);
        spriteController
                .createMesh(tiledSpriteProgram, texture, SPRITE_WIDTH, SPRITE_HEIGHT, 1f / SPRITE_FRAMES_X,
                        1f / SPRITE_FRAMES_Y);
        viewFrustum.setOrthoProjection(0, 1, 1, 0, 0, 10);
        int frame = 0;
        int maxFrames = SPRITE_FRAMES_X * SPRITE_FRAMES_Y - 1;
        for (TiledSprite sprite : spriteController.getSprites()) {
            sprite.setFrame(frame++);
            if (frame > maxFrames) {
                frame = 0;
            }
            sprite.setPosition(START_XPOS, START_YPOS);
        }

    }

    /**
     * Loads an image into several mip-map levels, the same image will be scaled to produce the
     * different mip-map levels.
     * TODO: Add method to ImageFactory to scale existing image - currently re-loads image and scales.
     * 
     * @param imageName Name of image to load
     * @param baseHeight Base height of assets, image will be scaled compared to
     * screen height. ie If base is 1080 and display height is 540 then the first mip-map level
     * will be 1/2 original size.
     * @param levels Number of mip-map levels
     * @return Array with an image for each mip-map level.
     */
    private Image[] loadTextureMIPMAP(String imageName, int baseHeight, int levels) {

        Image[] images = new Image[levels];
        try {
            float scale = ResourceBias.getScaleFactorLandscape(width, height, baseHeight);
            for (int i = 0; i < levels; i++) {
                images[i] = imageFactory.createImage(imageName, scale, scale);
                scale = scale * 0.5f;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return images;
    }

    /**
     * Sets the active texture, binds texName and calls glTexImage2D on the images in the array where
     * mip-map level will be same as the image index.
     * 
     * @param texture Texture unit number (active texture)
     * @param texName Name of texture object
     * @param textureImages Array with one or more images to send to GL. If more than
     * one image is specified then multiple mip-map levels will be set.
     * Level 0 shall be at index 0
     * @throws GLException If there is an error uploading the textures.
     */
    public void uploadTextures(int texture, int texName, Image[] textureImages) throws GLException {
        gles.glActiveTexture(texture);
        gles.glBindTexture(GLES20.GL_TEXTURE_2D, texName);
        int level = 0;
        for (Image textureImg : textureImages) {
            if (textureImg != null) {
                gles.glTexImage2D(GLES20.GL_TEXTURE_2D, level, textureImg.getFormat().getFormat(),
                        textureImg.getWidth(),
                        textureImg
                                .getHeight(), 0, textureImg.getFormat().getFormat(), GLES20.GL_UNSIGNED_BYTE,
                        textureImg
                                .getBuffer()
                                .position(0));
                GLUtils.handleError(gles, "texImage2D");
            }
            level++;
        }

    }

}
