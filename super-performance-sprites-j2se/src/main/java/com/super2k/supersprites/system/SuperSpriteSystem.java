package com.super2k.supersprites.system;

import java.util.Random;

import com.graphicsengine.component.SpriteComponent;
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

    /**
     * This is the data defined for each sprite
     * 
     * @author Richard Sahlin
     *
     */
    public enum EntityData {
        MOVE_VECTOR_X(0),
        MOVE_VECTOR_Y(1),
        MOVE_VECTOR_Z(2),
        ELASTICITY(3),
        ROTATE_SPEED(4),
        RESISTANCE(5);
        public final int index;

        EntityData(int index) {
            this.index = index;
        }

        /**
         * Returns the size in floats of the data store for each sprite
         * 
         * @return The size in float for each sprite datastore.
         */
        public static int getSize() {
            EntityData[] values = values();
            return values[values.length - 1].index + 1;
        }
    }

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
        int spriteIndex = 0;
        int readLength = spriteComponent.getSpritedataSize();
        float[] entityData = new float[EntityData.getSize()];
        float[] v = moveVector.vector;
        for (int sprite = 0; sprite < spriteCount; sprite++) {
            spriteIndex = sprite * readLength;
            spriteComponent.get(sprite, 1, entityData);
            spriteData[mapper.rotateOffset + spriteIndex] += deltaTime
                    * entityData[EntityData.ROTATE_SPEED.index];
            if (spriteData[mapper.rotateOffset + spriteIndex] > TWOPI) {
                spriteData[mapper.rotateOffset + spriteIndex] -= TWOPI;
            }
            // Update gravity
            entityData[EntityData.MOVE_VECTOR_Y.index] += GRAVITY * deltaTime;

            float xpos = spriteData[mapper.translateOffset + spriteIndex];
            float ypos = spriteData[mapper.translateOffset + 1 + spriteIndex];

            xpos += deltaTime * entityData[EntityData.MOVE_VECTOR_X.index];
            ypos += deltaTime * entityData[EntityData.MOVE_VECTOR_Y.index];
            if (ypos < worldLimit[3]) {
                spriteData[EntityData.MOVE_VECTOR_Y.index] = -spriteData[EntityData.MOVE_VECTOR_Y.index]
                        * spriteData[EntityData.ELASTICITY.index];
                ypos = worldLimit[3] - (ypos - worldLimit[3]);
            }
            if (xpos > worldLimit[2]) {
                xpos = worldLimit[2] - (xpos - worldLimit[2]);
                entityData[EntityData.MOVE_VECTOR_X.index] = -entityData[EntityData.MOVE_VECTOR_X.index]
                        * entityData[EntityData.ELASTICITY.index];
                entityData[EntityData.ROTATE_SPEED.index] = -entityData[EntityData.ROTATE_SPEED.index]
                        * entityData[EntityData.ELASTICITY.index];
            } else if (xpos < worldLimit[0]) {
                xpos = worldLimit[0] - (xpos - worldLimit[0]);
                entityData[EntityData.MOVE_VECTOR_Y.index] = -entityData[EntityData.MOVE_VECTOR_Y.index]
                        * entityData[EntityData.ELASTICITY.index];
            }
            spriteData[mapper.translateOffset + spriteIndex] = xpos;
            spriteData[mapper.translateOffset + 1 + spriteIndex] = ypos;
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
        float rotation = 0;
        for (int currentSprite = 0; currentSprite < sprites.getCount(); currentSprite++) {
            int index = currentSprite * sprites.getSpritedataSize() + sprites.getMapper().attributesPerVertex;
            float[] scale = root.getViewNode(Layer.SCENE).getTransform().getScale();
            float x = (((random.nextFloat() * 1.67f) - 0.8889f) / scale[VecMath.X]);
            float y = ((random.nextFloat() - 0.5f) / scale[VecMath.Y]);
            sprites.setSprite(currentSprite, x, y, 1f, 1 + random.nextFloat(), 1 + random.nextFloat(),
                    1f, rotation, frame++);
            rotation += 0.01f;
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
        return EntityData.getSize();
    }

}
