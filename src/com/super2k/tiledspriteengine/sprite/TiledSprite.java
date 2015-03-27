package com.super2k.tiledspriteengine.sprite;

/**
 * A tiled sprite object, this is a type of sprite that uses one Mesh (drawcall) to draw all sprites.
 * It is created by the TiledSpriteController.
 * 
 * @author Richard Sahlin
 *
 */
public class TiledSprite extends Sprite {

    public final static int VERTICES_PER_SPRITE = 4;
    /**
     * Number of float data per vertex
     */
    public final static int PER_VERTEX_DATA = 4;

    /**
     * Ref to sprite data, use with offset.
     */
    float[] data;
    int offset;

    /**
     * Creates a new TiledSprite, using attribute data at the specified offset.
     * This constructor shall not be called directly, use TiledSpriteController to create sprites.
     * 
     * @param data Shared attribute data for positions
     * @param offset Offset into array where data for this sprite is.
     */
    TiledSprite(float[] data, int offset) {
        this.data = data;
        this.offset = offset;
    }

    /**
     * Internal method.
     * Stores the position and data of this sprite into the attribute array (in the Mesh) used when rendering this
     * sprite. This must be called before this sprite is updated on screen.
     * 
     * @param xpos
     * @param ypos
     */
    void prepare() {
        float xpos = floatData[X_POS];
        float ypos = floatData[Y_POS];
        int index = offset;
        for (int i = 0; i < VERTICES_PER_SPRITE; i++) {
            data[index++] = xpos;
            data[index++] = ypos;
            data[index++] = floatData[ROTATION];
            data[index++] = floatData[FRAME];
        }
    }

}
