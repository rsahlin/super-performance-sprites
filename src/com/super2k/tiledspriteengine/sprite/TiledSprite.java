package com.super2k.tiledspriteengine.sprite;

import com.nucleus.transform.Vector2D;
import com.super2k.tiledspriteengine.TiledSpriteProgram;

/**
 * A tiled sprite object, this is a type of sprite that uses one Mesh (drawcall) to draw all sprites.
 * It is created by the TiledSpriteController.
 * 
 * @author Richard Sahlin
 *
 */
public class TiledSprite extends Sprite {

    private final static float TWOPI = 3.1415926f * 2;
    public final static float GRAVITY = 5;

    public final static int VERTICES_PER_SPRITE = 4;
    /**
     * Number of float data per vertex
     */
    public final static int PER_VERTEX_DATA = 8;

    /**
     * Ref to sprite data, use with offset.
     */
    float[] attributeData;
    int offset;

    /**
     * Creates a new TiledSprite, using attribute data at the specified offset.
     * This constructor shall not be called directly, use TiledSpriteController to create sprites.
     * 
     * @param data Shared attribute data for positions
     * @param offset Offset into array where data for this sprite is.
     */
    TiledSprite(float[] data, int offset) {
        this.attributeData = data;
        this.offset = offset;
        prepareUV();
    }

    /**
     * Set the UV indexes in attribute data, do this at setup so that the tex fraction size can be multiplied by FRAME
     * at each
     * vertice to get the correct UV coordinate.
     * This method is chosen to move as much processing as possible to the GPU - the UV of each sprite could be
     * calculated at runtime
     * but that would give a higher CPU impact when a large number of sprites are animated.
     */
    protected void prepareUV() {
        int index = offset;
        attributeData[index + TiledSpriteProgram.ATTRIBUTE_SPRITE_U_INDEX] = 0;
        attributeData[index + TiledSpriteProgram.ATTRIBUTE_SPRITE_V_INDEX] = 0;
        index += PER_VERTEX_DATA;
        attributeData[index + TiledSpriteProgram.ATTRIBUTE_SPRITE_U_INDEX] = 1;
        attributeData[index + TiledSpriteProgram.ATTRIBUTE_SPRITE_V_INDEX] = 0;
        index += PER_VERTEX_DATA;
        attributeData[index + TiledSpriteProgram.ATTRIBUTE_SPRITE_U_INDEX] = 1;
        attributeData[index + TiledSpriteProgram.ATTRIBUTE_SPRITE_V_INDEX] = 1;
        index += PER_VERTEX_DATA;
        attributeData[index + TiledSpriteProgram.ATTRIBUTE_SPRITE_U_INDEX] = 0;
        attributeData[index + TiledSpriteProgram.ATTRIBUTE_SPRITE_V_INDEX] = 1;
    }

    /**
     * Internal method.
     * Stores the position and data of this sprite into the attribute array (in the Mesh) used when rendering this
     * sprite. This must be called before this sprite is updated on screen.
     * 
     * @param xpos
     * @param ypos
     */
    @Override
    void prepare() {
        float xpos = floatData[X_POS];
        float ypos = floatData[Y_POS];
        int index = offset;
        int frameIndex = (int) floatData[FRAME];
        float rotation = floatData[ROTATION];

        for (int i = 0; i < VERTICES_PER_SPRITE; i++) {
            attributeData[index + TiledSpriteProgram.ATTRIBUTE_SPRITE_X_INDEX] = xpos;
            attributeData[index + TiledSpriteProgram.ATTRIBUTE_SPRITE_Y_INDEX] = ypos;
            attributeData[index + TiledSpriteProgram.ATTRIBUTE_SPRITE_FRAME_INDEX] = frameIndex;
            attributeData[index + TiledSpriteProgram.ATTRIBUTE_SPRITE_ROTATION_INDEX] = rotation;
            index += PER_VERTEX_DATA;
        }

    }

    /**
     * Update the movement of this sprite.
     * 
     * @param deltaTime
     */
    public void process(float deltaTime) {
        floatData[Sprite.ROTATION] += deltaTime * floatData[Sprite.ROTATE_SPEED];
        if (floatData[Sprite.ROTATION] > TWOPI) {
            floatData[Sprite.ROTATION] -= TWOPI;
        }
        updateGravity(0, GRAVITY, deltaTime);
        move(deltaTime);
        if (floatData[Sprite.Y_POS] > 1.0f) {
            floatData[Sprite.GRAVITY_Y] = -floatData[Sprite.GRAVITY_Y]
                    * floatData[Sprite.ELASTICITY];
            floatData[Sprite.Y_POS] = 2 - (floatData[Sprite.Y_POS]);
        }
        if (floatData[Sprite.X_POS] > 1.0f) {
            floatData[Sprite.X_POS] = 2 - floatData[Sprite.X_POS];
            moveVector.vector[Vector2D.X_AXIS] = -moveVector.vector[Vector2D.X_AXIS] * floatData[Sprite.ELASTICITY];
            floatData[Sprite.ROTATE_SPEED] = -floatData[Sprite.ROTATE_SPEED] * floatData[Sprite.ELASTICITY];
        } else if (floatData[Sprite.X_POS] < 0.0f) {
            floatData[Sprite.X_POS] = -floatData[Sprite.X_POS];
            moveVector.vector[Vector2D.X_AXIS] = -moveVector.vector[Vector2D.X_AXIS] * floatData[Sprite.ELASTICITY];
            floatData[Sprite.ROTATE_SPEED] = -floatData[Sprite.ROTATE_SPEED] * floatData[Sprite.ELASTICITY];
        }

    }

}
