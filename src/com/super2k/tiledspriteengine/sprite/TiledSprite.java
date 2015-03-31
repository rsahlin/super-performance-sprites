package com.super2k.tiledspriteengine.sprite;

import com.super2k.tiledspriteengine.TiledSpriteProgram;


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
    public final static int PER_VERTEX_DATA = 5;

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
        prepareUV();
    }

    /**
     * Set the UV indexes in attribute data, do this at setup so that the tex fraction size can be multiplied by FRAME at each
     * vertice to get the correct UV coordinate.
     * This method is chosen to move as much processing as possible to the GPU - the UV of each sprite could be calculated at runtime
     * but that would give a higher CPU impact when a large number of sprites are animated.
     */
    protected void prepareUV() {
    	int index = offset;
        data[index + TiledSpriteProgram.ATTRIBUTE_SPRITE_U_INDEX] = 0;
        data[index + TiledSpriteProgram.ATTRIBUTE_SPRITE_V_INDEX] = 0;
        index += PER_VERTEX_DATA;
        data[index + TiledSpriteProgram.ATTRIBUTE_SPRITE_U_INDEX] = 1;
        data[index + TiledSpriteProgram.ATTRIBUTE_SPRITE_V_INDEX] = 0;
        index += PER_VERTEX_DATA;
        data[index + TiledSpriteProgram.ATTRIBUTE_SPRITE_U_INDEX] = 1;
        data[index + TiledSpriteProgram.ATTRIBUTE_SPRITE_V_INDEX] = 1;
        index += PER_VERTEX_DATA;
        data[index + TiledSpriteProgram.ATTRIBUTE_SPRITE_U_INDEX] = 0;
        data[index + TiledSpriteProgram.ATTRIBUTE_SPRITE_V_INDEX] = 1;
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
        int frameIndex = (int) floatData[FRAME];
        
        for (int i = 0; i < VERTICES_PER_SPRITE; i++) {
            data[index + TiledSpriteProgram.ATTRIBUTE_SPRITE_X_INDEX] = xpos;
            data[index + TiledSpriteProgram.ATTRIBUTE_SPRITE_Y_INDEX] = ypos;
            data[index + TiledSpriteProgram.ATTRIBUTE_SPRITE_FRAME_INDEX] = frameIndex;
            index += PER_VERTEX_DATA;
        }
        
    }

}
