package com.super2k.tiledspriteengine;

import com.nucleus.sprite.Sprite;
import com.nucleus.sprite.Sprite.Logic;
import com.nucleus.vecmath.VecMath;
import com.nucleus.vecmath.Vector2D;

/**
 * Logic for the ÅF sprite - do not store any local data (fields) in this class, this is to avoid having to instantiate
 * one logic for each visible sprite. Instead the same logic instance will be used for all sprites with the ÅF logic.
 * 
 * @author Richard Sahlin
 *
 */
public class AFSprite implements Logic {

    public final static int ELASTICITY = Sprite.SPRITE_FLOAT_COUNT;
    public final static int ROTATE_SPEED = Sprite.SPRITE_FLOAT_COUNT + 1;

    private final static float TWOPI = 3.1415926f * 2;
    public final static float GRAVITY = 5;

    @Override
    public void process(Sprite sprite, float deltaTime) {

        float[] floatData = sprite.floatData;
        Vector2D moveVector = sprite.moveVector;

        floatData[Sprite.ROTATION] += deltaTime * floatData[ROTATE_SPEED];
        if (floatData[Sprite.ROTATION] > TWOPI) {
            floatData[Sprite.ROTATION] -= TWOPI;
        }
        sprite.accelerate(0, GRAVITY, deltaTime);
        sprite.move(deltaTime);
        if (floatData[Sprite.Y_POS] > SuperSprites.worldLimit[3]) {
            floatData[Sprite.MOVE_VECTOR_Y] = -floatData[Sprite.MOVE_VECTOR_Y]
                    * floatData[ELASTICITY];
            floatData[Sprite.Y_POS] = SuperSprites.worldLimit[3]
                    - (floatData[Sprite.Y_POS] - SuperSprites.worldLimit[3]);
        }
        if (floatData[Sprite.X_POS] > SuperSprites.worldLimit[2]) {
            floatData[Sprite.X_POS] = SuperSprites.worldLimit[2]
                    - (floatData[Sprite.X_POS] - SuperSprites.worldLimit[2]);
            moveVector.vector[VecMath.X] = -moveVector.vector[VecMath.X] * floatData[ELASTICITY];
            floatData[ROTATE_SPEED] = -floatData[ROTATE_SPEED] * floatData[ELASTICITY];
        } else if (floatData[Sprite.X_POS] < SuperSprites.worldLimit[0]) {
            floatData[Sprite.X_POS] = -floatData[Sprite.X_POS];
            moveVector.vector[VecMath.X] = -moveVector.vector[VecMath.X] * floatData[ELASTICITY];
            floatData[ROTATE_SPEED] = -floatData[ROTATE_SPEED] * floatData[ELASTICITY];
        }

    }

}
