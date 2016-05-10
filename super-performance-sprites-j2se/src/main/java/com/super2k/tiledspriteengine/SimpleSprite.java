package com.super2k.tiledspriteengine;

import com.graphicsengine.sprite.Sprite;
import com.nucleus.actor.ActorContainer;
import com.nucleus.actor.ActorItem;
import com.nucleus.vecmath.Vector2D;
import com.super2k.tiledspriteengine.SuperSpriteResolver.Logics;

public class SimpleSprite implements ActorItem {

    @Override
    public void reset(ActorContainer actor) {
        // TODO Auto-generated method stub

    }

    @Override
    public void init(ActorContainer actor) {
        // TODO Auto-generated method stub

    }

    @Override
    public void process(ActorContainer actor, float deltaTime) {
        float[] floatData = actor.floatData;
        Vector2D moveVector = actor.moveVector;

        floatData[Sprite.ROTATION] += deltaTime * 0.5f;
    }

    @Override
    public String getActorId() {
        return Logics.SPRITE.name();
    }

}
