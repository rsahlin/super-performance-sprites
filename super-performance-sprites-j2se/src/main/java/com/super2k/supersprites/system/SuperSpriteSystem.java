package com.super2k.supersprites.system;

import java.util.Random;

import com.graphicsengine.component.SpriteComponent;
import com.graphicsengine.component.SpriteComponent.EntityData;
import com.graphicsengine.component.SpriteComponent.SpriteData;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.component.Component;
import com.nucleus.geometry.AttributeUpdater.PropertyMapper;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.Layer;
import com.nucleus.scene.LayerNode;
import com.nucleus.scene.Node;
import com.nucleus.scene.RootNode;
import com.nucleus.system.System;
import com.nucleus.vecmath.VecMath;
import com.nucleus.vecmath.Vector2D;

/**
 * The system for controlling the sprites defined by {@linkplain SpriteComponent}
 * This is the collected functionality for the sprites, use this setup so that data is shared in such a way
 * that the logic can be accelerated by APIs such as OpenCL.
 * 
 * @author Richard Sahlin
 *
 */
public class SuperSpriteSystem extends System {

    private final static float TWOPI = 3.1415926f * 2;
    public final static float GRAVITY = -5;

    RootNode root;
    private LayerNode viewNode;
    public static float[] worldLimit = new float[4];
    private float orthoLeft;
    private float orthoTop;
    private boolean initialized = false;
    private Vector2D moveVector = new Vector2D();

    public SuperSpriteSystem() {
    }

    @Override
    public void process(Component component, float deltaTime) {
        if (!initialized) {
            throw new IllegalStateException("initSystem() must be called before calling process()");
        }
        updateNodeScale();
        SpriteComponent spriteComponent = (SpriteComponent) component;
        int spriteCount = spriteComponent.getCount();
        float[] spriteData = spriteComponent.getSpriteData();
        PropertyMapper mapper = spriteComponent.getMapper();
        int readIndex = 0;
        int readLength = SpriteComponent.EntityData.getSize();
        for (int sprite = 0; sprite < spriteCount; sprite++) {
            spriteComponent.getMoveVector(sprite, moveVector);
            spriteData[SpriteData.ROTATE.index + readIndex] += deltaTime
                    * spriteData[EntityData.ROTATE_SPEED.index + readIndex];
            if (spriteData[SpriteData.ROTATE.index + readIndex] > TWOPI) {
                spriteData[SpriteData.ROTATE.index + readIndex] -= TWOPI;
            }
            // Update gravity
            spriteData[EntityData.MOVE_VECTOR_Y.index
                    + readIndex] += GRAVITY * deltaTime;

            float xpos = spriteData[SpriteData.TRANSLATE_X.index + readIndex];
            float ypos = spriteData[SpriteData.TRANSLATE_Y.index + readIndex];

            xpos += deltaTime * moveVector.vector[VecMath.X] * moveVector.vector[Vector2D.MAGNITUDE]
                    + spriteData[EntityData.MOVE_VECTOR_X.index + readIndex] * deltaTime;
            ypos += deltaTime * moveVector.vector[VecMath.Y] * moveVector.vector[Vector2D.MAGNITUDE]
                    + spriteData[EntityData.MOVE_VECTOR_Y.index + readIndex] * deltaTime;
            if (ypos < worldLimit[3]) {
                spriteData[EntityData.MOVE_VECTOR_Y.index
                        + readIndex] = -spriteData[EntityData.MOVE_VECTOR_Y.index + readIndex]
                                * spriteData[EntityData.ELASTICITY.index + readIndex];
                ypos = worldLimit[3] - (ypos - worldLimit[3]);
            }
            if (xpos > worldLimit[2]) {
                xpos = worldLimit[2] - (xpos - worldLimit[2]);
                moveVector.vector[VecMath.X] = -moveVector.vector[VecMath.X]
                        * spriteData[EntityData.ELASTICITY.index + readIndex];
                spriteData[EntityData.ROTATE_SPEED.index
                        + readIndex] = -spriteData[EntityData.ROTATE_SPEED.index + readIndex]
                                * spriteData[EntityData.ELASTICITY.index];
            } else if (xpos < worldLimit[0]) {
                xpos = worldLimit[0] - (xpos - worldLimit[0]);
                moveVector.vector[VecMath.X] = -moveVector.vector[VecMath.X]
                        * spriteData[EntityData.ELASTICITY.index + readIndex];
                spriteData[EntityData.ROTATE_SPEED.index
                        + readIndex] = -spriteData[EntityData.ROTATE_SPEED.index + readIndex]
                                * spriteData[EntityData.ELASTICITY.index + readIndex];
            }

            float rotate = spriteData[SpriteData.ROTATE.index + readIndex];
            spriteData[SpriteData.TRANSLATE_X.index + readIndex] = xpos;
            spriteData[SpriteData.TRANSLATE_Y.index + readIndex] = ypos;
            readIndex += readLength;
        }
    }

    @Override
    public void initSystem(NucleusRenderer renderer, RootNode root, Component component) {
        initialized = true;
        this.root = root;

        Node scene = root.getNodeById("root");
        ViewFrustum vf = scene.getViewFrustum();
        float[] values = vf.getValues();
        orthoLeft = values[ViewFrustum.LEFT_INDEX];
        orthoTop = values[ViewFrustum.TOP_INDEX];
        viewNode = root.getViewNode(Layer.SCENE);
        initSprites((SpriteComponent) component);
    }

    private void initSprites(SpriteComponent sprites) {
        int spriteFrames = sprites.getFrameCount();
        int frame = 0;
        Random random = new Random();
        float[] spriteData = sprites.getSpriteData();
        for (int currentSprite = 0; currentSprite < sprites.getCount(); currentSprite++) {
            int index = currentSprite * SpriteComponent.EntityData.getSize();
            float[] scale = root.getViewNode(Layer.SCENE).getTransform().getScale();
            float x = (((random.nextFloat() * 1.67f) - 0.8889f) / scale[VecMath.X]);
            float y = ((random.nextFloat() - 0.5f) / scale[VecMath.Y]);
            sprites.setPosition(currentSprite, x, y, 1);
            sprites.setRotation(currentSprite, 0);
            sprites.setScale(currentSprite, 1 + random.nextFloat(), 1 + random.nextFloat());
            sprites.setFrame(currentSprite, frame++);
            if (frame >= spriteFrames) {
                frame = 0;
            }
            spriteData[index + EntityData.MOVE_VECTOR_X.index] = 0;
            spriteData[index + EntityData.MOVE_VECTOR_Y.index] = 0;
            spriteData[index + EntityData.ELASTICITY.index] = 0.5f + random.nextFloat() * 0.5f;
            spriteData[index + EntityData.RESISTANCE.index] = random.nextFloat() * 0.03f;
            currentSprite++;
            // if (currentSprite > spritecount - 1) {
            // currentSprite = 0;
            // }

        }
    }

    private void updateNodeScale() {
        if (viewNode != null) {
            float[] scale = viewNode.getTransform().getScale();
            worldLimit[0] = (orthoLeft) / scale[VecMath.X];
            worldLimit[1] = (orthoTop) / scale[VecMath.Y];
            worldLimit[2] = (-orthoLeft) / scale[VecMath.X];
            worldLimit[3] = (-orthoTop) / scale[VecMath.Y];
        }
    }

    @Override
    public int getEntityDataSize() {
        // TODO Auto-generated method stub
        return 0;
    }

}
