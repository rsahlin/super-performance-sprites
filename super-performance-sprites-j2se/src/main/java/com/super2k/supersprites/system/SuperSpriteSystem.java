package com.super2k.supersprites.system;

import java.util.Random;

import com.graphicsengine.component.SpriteComponent;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.common.Constants;
import com.nucleus.component.Component;
import com.nucleus.geometry.AttributeUpdater.PropertyMapper;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.Layer;
import com.nucleus.scene.LayerNode;
import com.nucleus.scene.Node;
import com.nucleus.scene.RootNode;
import com.nucleus.system.System;
import com.nucleus.vecmath.VecMath;

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

    public final static float GRAVITY = -5;

    private int spritecount;
    RootNode root;
    private LayerNode viewNode;
    public static float[] worldLimit = new float[4];
    private float orthoLeft;
    private float orthoTop;
    private boolean initialized = false;
    private SpriteComponent sprites;
    private int currentSprite = 0;
    private Random random = new Random(java.lang.System.currentTimeMillis());

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
        PropertyMapper mapper = spriteComponent.getMapper();
        int spriteIndex = 0;
        float[] entityData = new float[EntityData.getSize()];
        float[] spriteData = new float[mapper.attributesPerVertex];
        for (int sprite = 0; sprite < spriteCount; sprite++) {
            // Fetch data from componentbuffer
            spriteComponent.get(sprite, 0, spriteData);
            spriteComponent.get(sprite, 1, entityData);

            // Do processing
            spriteData[mapper.rotateOffset + spriteIndex] += deltaTime
                    * entityData[EntityData.ROTATE_SPEED.index];
            if (spriteData[mapper.rotateOffset + spriteIndex] > Constants.TWOPI) {
                spriteData[mapper.rotateOffset + spriteIndex] -= Constants.TWOPI;
            }
            // Update gravity
            entityData[EntityData.MOVE_VECTOR_Y.index] += GRAVITY * deltaTime;

            float xpos = spriteData[mapper.translateOffset + spriteIndex];
            float ypos = spriteData[mapper.translateOffset + 1 + spriteIndex];

            xpos += deltaTime * entityData[EntityData.MOVE_VECTOR_X.index];
            ypos += deltaTime * entityData[EntityData.MOVE_VECTOR_Y.index];
            if (ypos < worldLimit[3]) {
                entityData[EntityData.MOVE_VECTOR_Y.index] = -entityData[EntityData.MOVE_VECTOR_Y.index]
                        * entityData[EntityData.ELASTICITY.index];
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

            // Put data back in componentbuffer
            spriteComponent.put(sprite, 0, spriteData);
            spriteComponent.put(sprite, 1, entityData);

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
        this.sprites = sprites;
        int spriteFrames = sprites.getFrameCount();
        spritecount = sprites.getCount();
        int frame = 0;
        Random random = new Random();
        float rotation = 0;
        PropertyMapper mapper = sprites.getMapper();
        float[] spriteData = new float[mapper.attributesPerVertex];
        float[] entityData = new float[EntityData.getSize()];
        for (int currentSprite = 0; currentSprite < sprites.getCount(); currentSprite++) {
            float[] scale = root.getViewNode(Layer.SCENE).getTransform().getScale();
            spriteData[mapper.translateOffset] = (((random.nextFloat() * 1.67f) - 0.8889f) / scale[VecMath.X]);
            spriteData[mapper.translateOffset + 1] = ((random.nextFloat() - 0.5f) / scale[VecMath.Y]);
            spriteData[mapper.translateOffset + 2] = 1;

            spriteData[mapper.scaleOffset] = random.nextFloat() + 1;
            spriteData[mapper.scaleOffset + 1] = random.nextFloat() + 1;
            spriteData[mapper.scaleOffset + 2] = 1;

            spriteData[mapper.rotateOffset] = 0;
            spriteData[mapper.rotateOffset + 1] = 0;
            spriteData[mapper.rotateOffset + 2] = rotation;

            spriteData[mapper.frameOffset] = frame++;

            rotation += 0.01f;
            if (frame >= spriteFrames) {
                frame = 0;
            }
            entityData[EntityData.MOVE_VECTOR_X.index] = 0;
            entityData[EntityData.MOVE_VECTOR_Y.index] = 0;
            entityData[EntityData.ELASTICITY.index] = 0.5f + random.nextFloat() * 0.5f;
            entityData[EntityData.RESISTANCE.index] = random.nextFloat() * 0.03f;
            sprites.put(currentSprite, 0, spriteData);
            sprites.put(currentSprite, 1, entityData);
            currentSprite++;
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

    /**
     * 
     * @param pos
     */
    public void releaseSprite(float[] pos) {
        float[] scale = root.getViewNode(Layer.SCENE).getTransform().getScale();
        float x = (pos[0] / scale[VecMath.X]);
        float y = (pos[1] / scale[VecMath.Y]);
        sprites.setTranslate(currentSprite, new float[] { x, y, 1 });
        sprites.setScale(currentSprite, new float[] { 1, 1, 1 });
        sprites.setRotate(currentSprite, new float[] { 0, 0, 1 });
        float[] entityData = new float[EntityData.getSize()];
        entityData[EntityData.MOVE_VECTOR_X.index] = 0;
        entityData[EntityData.MOVE_VECTOR_Y.index] = 0;
        entityData[EntityData.ELASTICITY.index] = 0.5f + random.nextFloat() * 0.5f;
        sprites.put(currentSprite, 1, entityData);
        currentSprite++;
        if (currentSprite > spritecount - 1) {
            currentSprite = 0;
        }
    }

}
