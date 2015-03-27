package com.super2k.tiledspriteengine;

import java.io.IOException;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.util.Log;

import com.nucleus.android.AndroidGLUtils;
import com.nucleus.android.AndroidRenderer;
import com.nucleus.common.TimeKeeper;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.VertexBuffer;
import com.nucleus.mmi.MMIEventListener;
import com.nucleus.mmi.MMIPointerEvent;
import com.nucleus.mmi.PointerInputProcessor;
import com.nucleus.opengl.GLES20Wrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.texturing.Image;
import com.nucleus.transform.Vector2D;
import com.super2k.tiledspriteengine.sprite.Sprite;
import com.super2k.tiledspriteengine.sprite.TiledSprite;
import com.super2k.tiledspriteengine.sprite.TiledSpriteController;

public class TiledSpriteRenderer extends AndroidRenderer implements MMIEventListener {

    protected final static String TILED_SPRITE_RENDERER_TAG = "TiledSpiteRenderer";
    public final static int SPRITECOUNT = 1000;

    private PointerInputProcessor inputProcessor;
    private ShaderProgram tiledSpriteProgram;
    private Mesh mesh;
    private Image texture;
    private int textureID;
    private TiledSpriteController spriteController = new TiledSpriteController(SPRITECOUNT);

    private float[] uniformVector = new float[] { 0, 0, 0, 0, 0, 0, 0, 0 };
    private int currentSprite = 0;
    private TimeKeeper timeKeeper = new TimeKeeper(30);
    private Random random = new Random();

    public TiledSpriteRenderer(PointerInputProcessor inputProcessor) {
        super();
        this.inputProcessor = inputProcessor;
        inputProcessor.addMMIListener(this);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);
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

    public void beginFrame() {
        super.beginFrame();
        /**
         * For now handling of logic is done before rendering
         * TODO: Add logic handler separated from rendering.
         */

        float deltaTime = timeKeeper.update();
        if (timeKeeper.getSampleDuration() > 3) {
            Log.d(TILED_SPRITE_RENDERER_TAG, "Average FPS: " + timeKeeper.sampleFPS());
        }
        for (TiledSprite sprite : spriteController.getSprites()) {
            sprite.updateGravity(0, 10, deltaTime);
            sprite.move(deltaTime);
            if (sprite.floatData[Sprite.Y_POS] > 1.0f) {
                sprite.floatData[Sprite.GRAVITY_Y] = -sprite.floatData[Sprite.GRAVITY_Y]
                        * sprite.floatData[Sprite.ELASTICITY];
                sprite.floatData[Sprite.Y_POS] = 2 - (sprite.floatData[Sprite.Y_POS]);
            }
        }

    }

    /**
     * The main render method, all drawing shall take place here.
     */
    public void render() {
        super.render();
        try {
            GLUtils.handleError(gles, "Error");
            gles.glClearColor(0, 0f, 0f, 1.0f);
            gles.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            gles.glDisable(GLES20.GL_DEPTH_TEST);
            gles.glDisable(GLES20.GL_CULL_FACE);
            gles.glUseProgram(tiledSpriteProgram.getProgram());
            AndroidGLUtils.handleError("Program error");

            VertexBuffer positions = mesh.getVerticeBuffer(1);
            positions
                    .setArray(spriteController.getData(), 0, SPRITECOUNT * TiledSpriteController.SPRITE_ATTRIBUTE_DATA);

            renderMesh(mesh);
            GLUtils.handleError(gles, "Error");

        } catch (GLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void inputEvent(MMIPointerEvent event) {
        float[] delta = event.getPointerData().getDelta(1);

        switch (event.getAction()) {
        case MOVE:
            int index = 0;
            float[] pos = event.getPointerData().getCurrentPosition();
            // Normalize position
            pos[0] = pos[0] / width;
            pos[1] = pos[1] / height;
            Sprite s = spriteController.getSprites()[currentSprite];
            s.setPosition(pos[0], pos[1]);
            s.floatData[Sprite.GRAVITY_Y] = 0;
            s.floatData[Sprite.ELASTICITY] = 0.95f - (random.nextFloat() / 10);
            currentSprite++;
            if (currentSprite > SPRITECOUNT - 1) {
                currentSprite = 0;
            }
            break;
        case ZOOM:
            Vector2D zoom = event.getZoom();
            float z = ((zoom.vector[Vector2D.MAGNITUDE] * zoom.vector[Vector2D.X_AXIS]) / 1000);
            break;
        default:
            break;
        }
    }

    @Override
    public void GLContextCreated() {
        createPrograms();

        int[] textures = new int[1];
        gles.glGenTextures(1, textures, 0);

        textureID = textures[0];

        gles.glActiveTexture(GLES20.GL_TEXTURE0);
        gles.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
        try {
            texture = createImage("assets/texture.png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        gles.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, texture.getFormat().getFormat(), texture.getWidth(), texture
                .getHeight(), 0, texture.getFormat().getFormat(), GLES20.GL_UNSIGNED_BYTE, texture.getBuffer()
                .position(0));
        mesh = MeshBuilder.buildTileSpriteMesh(tiledSpriteProgram, spriteController.getCount(), 0.03f, 0.03f, 0,
                GLES20.GL_FLOAT);
        mesh.setUniformVectors(uniformVector);
    }
}
