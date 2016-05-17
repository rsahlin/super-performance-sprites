package com.super2k.tiledspriteengine;

import com.graphicsengine.sprite.Sprite;
import com.nucleus.actor.ActorContainer;
import com.nucleus.actor.ActorItem;
import com.nucleus.vecmath.VecMath;
import com.nucleus.vecmath.Vector2D;
import com.super2k.tiledspriteengine.SuperSpriteResolver.Logics;

/**
 * Logic for the AF sprite - do not store any local data (fields) in this class, this is to avoid having to instantiate
 * one logic for each visible sprite. Instead the same logic instance will be used for all sprites with the AF logic.
 * 
 * @author Richard Sahlin
 *
 */
public class BounceSprite implements ActorItem {

    public final static int ELASTICITY = Sprite.SPRITE_FLOAT_COUNT;
    public final static int ROTATE_SPEED = Sprite.SPRITE_FLOAT_COUNT + 1;

    private final static float TWOPI = 3.1415926f * 2;
    public final static float GRAVITY = -5;

    @Override
    public void process(ActorContainer spriteActor, float deltaTime) {
        if (!SuperSprites.process) {
            return;
        }
        float[] floatData = spriteActor.floatData;
        Vector2D moveVector = spriteActor.moveVector;

        floatData[Sprite.ROTATION] += deltaTime * floatData[ROTATE_SPEED];
        if (floatData[Sprite.ROTATION] > TWOPI) {
            floatData[Sprite.ROTATION] -= TWOPI;
        }
        spriteActor.accelerate(0, GRAVITY, deltaTime);
        spriteActor.move(deltaTime);
        if (floatData[Sprite.Y_POS] < SuperSprites.worldLimit[3]) {
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
            floatData[Sprite.X_POS] = SuperSprites.worldLimit[0]
                    - (floatData[Sprite.X_POS] - SuperSprites.worldLimit[0]);
            moveVector.vector[VecMath.X] = -moveVector.vector[VecMath.X] * floatData[ELASTICITY];
            floatData[ROTATE_SPEED] = -floatData[ROTATE_SPEED] * floatData[ELASTICITY];
        }
    }

    @Override
    public String getActorId() {
        return Logics.BOUNCESPRITE.name();
    }

    @Override
    public void reset(ActorContainer logic) {
        // TODO Auto-generated method stub

    }

    @Override
    public void init(ActorContainer logic) {
        logic.floatData[BounceSprite.ELASTICITY] = 0.9f;
    }

}
