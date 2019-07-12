package com.super2k.supersprites.system;

import com.graphicsengine.component.ActorComponent;
import com.graphicsengine.component.SpriteAttributeComponent;
import com.nucleus.component.CPUComponentBuffer;
import com.nucleus.shader.VariableIndexer.Property;

public class SuperSpriteTestSystem extends SuperSpriteSystem {

    @Override
    protected void initSprites(SpriteAttributeComponent component) {
        CPUComponentBuffer entityBuffer = (CPUComponentBuffer) component.getEntityBuffer();
        int spriteFrames = component.getFrameCount();
        component.get2DBounds(rectBounds);
        int frame = 0;
        float rotation = 0;
        if (entityData == null) {
            entityData = new float[entityBuffer.getSizePerEntity()];
        }
        float[] scale = scene.getTransform().getScale();
        float sceneWidth = viewFrustum.getWidth() / scale[0];
        float sceneHeight = viewFrustum.getHeight() / scale[1];
        float blue = 0.2f;
        int emissiveIndex = mapper.getOffset(Property.ALBEDO.getLocation());
        for (int currentSprite = 0; currentSprite < component.getCount(); currentSprite++) {
            entityData[emissiveIndex] = 0.2f;
            entityData[emissiveIndex + 1] = 0.3f;
            entityData[emissiveIndex + 2] = blue;
            entityData[emissiveIndex + 3] = 0.5f;
            blue += 0.01f;
            if (blue > 1f) {
                blue = 0;
            }
            ActorComponent.getRandomSprite(entityData, 0, frame++, 1, 1, sceneWidth, sceneHeight, random, mapper);
            ActorComponent.getRandomEntityData(entityData, rectBounds, 0.1f, random, mapper, sizePerVertex);
            component.setEntity(currentSprite, 0, entityData, 0, entityData.length);
            rotation += 0.01f;
            if (frame >= spriteFrames) {
                frame = 0;
            }
        }
    }

}
