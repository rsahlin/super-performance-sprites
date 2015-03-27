package com.super2k.tiledspriteengine.sprite;

/**
 * Controller for tiled sprites, this controller creates the tiled sprite objects.
 * A tiled sprite (quad) can be drawn in one draw call together with a large number of other sprites (they share the
 * same Mesh).
 * This is to allow a very large number of sprites in just 1 draw call to the underlying render API (OpenGLES).
 * Performance is increased, but all sprites must share the same texture atlas.
 * 
 * @author Richard Sahlin
 *
 */
public class TiledSpriteController {

    /**
     * Number of floats for each tiled sprite in the attribute data.
     */
    public final static int SPRITE_ATTRIBUTE_DATA = TiledSprite.PER_VERTEX_DATA * TiledSprite.VERTICES_PER_SPRITE;

    TiledSprite[] sprites;
    float[] data;
    int count;

    /**
     * Creates a TiledSpriteController with the specified number of sprites.
     * 
     * @param count
     */
    public TiledSpriteController(int count) {
        this.count = count;
        sprites = new TiledSprite[count];
        data = new float[count * SPRITE_ATTRIBUTE_DATA];
        for (int i = 0; i < count; i++) {
            sprites[i] = new TiledSprite(data, i * SPRITE_ATTRIBUTE_DATA);
        }
    }

    public float[] getData() {
        return data;
    }

    public int getCount() {
        return count;
    }

    public TiledSprite[] getSprites() {
        return sprites;
    }

}
