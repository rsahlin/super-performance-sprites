package com.super2k.supersprites.system;

import java.util.Random;

import com.graphicsengine.component.SpriteComponent;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.common.Constants;
import com.nucleus.component.CPUComponentBuffer;
import com.nucleus.component.CPUQuadExpander;
import com.nucleus.component.Component;
import com.nucleus.geometry.AttributeUpdater.PropertyMapper;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.Node;
import com.nucleus.scene.RootNode;
import com.nucleus.system.System;

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
    private Node scene;
    public float[] scaledLimit = new float[ViewFrustum.PROJECTION_SIZE];
    public float[] viewport = new float[ViewFrustum.PROJECTION_SIZE];
    protected ViewFrustum viewFrustum;
    private boolean initialized = false;
    private SpriteComponent sprites;
    private int currentSprite = 0;
    private Random random = new Random(java.lang.System.currentTimeMillis());
    /**
     * Source
     */
    private CPUComponentBuffer entityBuffer;
    private CPUQuadExpander quadExpander;

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
        int quadIndex = 0;
        int entityIndex = mapper.attributesPerVertex;
        float[] entityData = entityBuffer.getData();
        for (int sprite = 0; sprite < spriteCount; sprite++) {
            // Do processing
            entityData[mapper.rotateOffset + quadIndex] += deltaTime
                    * entityData[EntityData.ROTATE_SPEED.index + entityIndex];
            if (entityData[mapper.rotateOffset + quadIndex] > Constants.TWOPI) {
                entityData[mapper.rotateOffset + quadIndex] -= Constants.TWOPI;
            }
            // Update gravity
            entityData[EntityData.MOVE_VECTOR_Y.index + entityIndex] += GRAVITY * deltaTime;

            float xpos = entityData[mapper.translateOffset + quadIndex];
            float ypos = entityData[mapper.translateOffset + 1 + quadIndex];

            xpos += deltaTime * entityData[EntityData.MOVE_VECTOR_X.index + entityIndex];
            ypos += deltaTime * entityData[EntityData.MOVE_VECTOR_Y.index + entityIndex];
            if (ypos < scaledLimit[ViewFrustum.BOTTOM_INDEX]) {
                entityData[EntityData.MOVE_VECTOR_Y.index
                        + entityIndex] = -entityData[EntityData.MOVE_VECTOR_Y.index + entityIndex]
                                * entityData[EntityData.ELASTICITY.index + entityIndex];
                ypos = scaledLimit[ViewFrustum.BOTTOM_INDEX] - (ypos - scaledLimit[ViewFrustum.BOTTOM_INDEX]);
            }
            if (xpos > scaledLimit[ViewFrustum.RIGHT_INDEX]) {
                xpos = scaledLimit[ViewFrustum.RIGHT_INDEX] - (xpos - scaledLimit[ViewFrustum.RIGHT_INDEX]);
                entityData[EntityData.MOVE_VECTOR_X.index
                        + entityIndex] = -entityData[EntityData.MOVE_VECTOR_X.index + entityIndex]
                                * entityData[EntityData.ELASTICITY.index];
                entityData[EntityData.ROTATE_SPEED.index
                        + entityIndex] = -entityData[EntityData.ROTATE_SPEED.index + entityIndex]
                                * entityData[EntityData.ELASTICITY.index + entityIndex];
            } else if (xpos < scaledLimit[ViewFrustum.LEFT_INDEX]) {
                xpos = scaledLimit[ViewFrustum.LEFT_INDEX] - (xpos - scaledLimit[ViewFrustum.LEFT_INDEX]);
                entityData[EntityData.MOVE_VECTOR_Y.index
                        + entityIndex] = -entityData[EntityData.MOVE_VECTOR_Y.index + entityIndex]
                                * entityData[EntityData.ELASTICITY.index + entityIndex];
            }
            quadExpander.setPosition(sprite, xpos, ypos);
            quadIndex += entityBuffer.getSizePerEntity();
            entityIndex += entityBuffer.getSizePerEntity();
        }
    }

    @Override
    public void initSystem(NucleusRenderer renderer, RootNode root, Component component) {
        initialized = true;
        this.root = root;
        scene = root.getNodeById("scene");
        // Get the view frustum and create rectangle bounds
        viewFrustum = scene.getViewFrustum();
        viewFrustum.getValues(viewport);
        initSprites((SpriteComponent) component);
    }

    private void initSprites(SpriteComponent sprites) {
        this.sprites = sprites;
        this.entityBuffer = (CPUComponentBuffer) sprites.getEntityBuffer();
        this.quadExpander = sprites.getQuadExpander();
        int spriteFrames = sprites.getFrameCount();
        spritecount = sprites.getCount();
        int frame = 0;
        Random random = new Random();
        float rotation = 0;
        PropertyMapper mapper = sprites.getMapper();
        float[] entityData = entityBuffer.getData();
        int index = 0;
        int entityIndex = mapper.attributesPerVertex;
        float[] transform = new float[9];
        float sceneWidth = scene.getViewFrustum().getWidth();
        float sceneHeight = scene.getViewFrustum().getHeight();
        for (int currentSprite = 0; currentSprite < sprites.getCount(); currentSprite++) {
            transform[0] = ((random.nextFloat() * sceneWidth) - sceneWidth / 2);
            transform[1] = ((random.nextFloat() * sceneHeight) - sceneHeight / 2);
            transform[2] = 1;
            transform[3] = 0;
            transform[4] = 0;
            transform[5] = rotation;
            // transform[6] = random.nextFloat() + 1;
            // transform[7] = random.nextFloat() + 1;
            transform[6] = 1;
            transform[7] = 1;
            transform[8] = 1;
            sprites.setTransform(currentSprite, transform);

            entityData[index + mapper.frameOffset] = frame++;

            rotation += 0.01f;
            if (frame >= spriteFrames) {
                frame = 0;
            }
            entityData[EntityData.MOVE_VECTOR_X.index + entityIndex] = 0;
            entityData[EntityData.MOVE_VECTOR_Y.index + entityIndex] = 0;
            entityData[EntityData.ELASTICITY.index + entityIndex] = 0.2f + random.nextFloat() * 0.5f;
            entityData[EntityData.RESISTANCE.index + entityIndex] = random.nextFloat() * 0.03f;
            quadExpander.expandQuadData(currentSprite);
            index += entityBuffer.getSizePerEntity();
            entityIndex += entityBuffer.getSizePerEntity();
        }
    }

    private void updateNodeScale() {
        // Scale the bounds according to node scale - this only works while sprites are attached
        float[] scale = scene.getTransform().getScale();
        scaledLimit[ViewFrustum.LEFT_INDEX] = viewport[ViewFrustum.LEFT_INDEX] / scale[0];
        scaledLimit[ViewFrustum.RIGHT_INDEX] = viewport[ViewFrustum.RIGHT_INDEX] / scale[0];
        scaledLimit[ViewFrustum.TOP_INDEX] = viewport[ViewFrustum.TOP_INDEX] / scale[1];
        scaledLimit[ViewFrustum.BOTTOM_INDEX] = viewport[ViewFrustum.BOTTOM_INDEX] / scale[1];
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
        sprites.setTransform(currentSprite, new float[] { pos[0], pos[1], 0, 0, 0, 0, 1, 1, 1 });
        currentSprite++;
        if (currentSprite > spritecount - 1) {
            currentSprite = 0;
        }
    }

}
