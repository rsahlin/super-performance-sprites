package com.super2k.supersprites.system;

import java.util.Random;

import com.graphicsengine.component.SpriteComponent;
import com.nucleus.bounds.RectangularBounds;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.common.Constants;
import com.nucleus.component.CPUComponentBuffer;
import com.nucleus.component.CPUQuadExpander;
import com.nucleus.component.Component;
import com.nucleus.geometry.AttributeUpdater.PropertyMapper;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.Node;
import com.nucleus.scene.Node.NodeTypes;
import com.nucleus.scene.RootNode;
import com.nucleus.system.System;
import com.nucleus.vecmath.Rectangle;

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

    RootNode root;
    private Node scene;
    private RectangularBounds screenBounds = new RectangularBounds(new Rectangle(0, 0, 0, 0));
    public float[] scaledRect = new float[4];
    public float[] viewport = new float[ViewFrustum.PROJECTION_SIZE];
    protected ViewFrustum viewFrustum;
    private boolean initialized = false;
    private SpriteComponent sprites;
    float[] entityData;
    float[] spriteData;
    PropertyMapper mapper;
    private int spriteCount;
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
        PropertyMapper mapper = spriteComponent.getMapper();
        int quadIndex = 0;
        int entityIndex = mapper.attributesPerVertex;
        float[] entityData = entityBuffer.getData();
        float yMin = scaledRect[1] - scaledRect[3];
        float xMin = scaledRect[0] - scaledRect[2];
        float xMax = scaledRect[0] + scaledRect[2];
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
            if (ypos < yMin) {
                entityData[EntityData.MOVE_VECTOR_Y.index
                        + entityIndex] = -entityData[EntityData.MOVE_VECTOR_Y.index + entityIndex]
                                * entityData[EntityData.ELASTICITY.index + entityIndex];
                ypos = yMin - (ypos - yMin);
            }
            if (xpos > xMax) {
                xpos = xMax - (xpos - xMax);
                entityData[EntityData.MOVE_VECTOR_X.index
                        + entityIndex] = -entityData[EntityData.MOVE_VECTOR_X.index + entityIndex]
                                * entityData[EntityData.ELASTICITY.index];
                entityData[EntityData.ROTATE_SPEED.index
                        + entityIndex] = -entityData[EntityData.ROTATE_SPEED.index + entityIndex]
                                * entityData[EntityData.ELASTICITY.index + entityIndex];
            } else if (xpos < xMin) {
                xpos = xMin - (xpos - xMin);
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
        viewFrustum = root.getNodeByType(NodeTypes.layernode.name()).getViewFrustum();
        viewFrustum.getValues(viewport);
        initSprites((SpriteComponent) component);
    }

    private void initSprites(SpriteComponent sprites) {
        this.sprites = sprites;
        this.entityBuffer = (CPUComponentBuffer) sprites.getEntityBuffer();
        this.quadExpander = sprites.getQuadExpander();
        int spriteFrames = sprites.getFrameCount();
        spriteCount = sprites.getCount();
        int frame = 0;
        float rotation = 0;
        mapper = sprites.getMapper();
        entityData = new float[entityBuffer.getSizePerEntity() - mapper.attributesPerVertex];
        spriteData = new float[mapper.attributesPerVertex];
        float[] scale = scene.getTransform().getScale();
        float sceneWidth = viewFrustum.getWidth() / scale[0];
        float sceneHeight = viewFrustum.getHeight() / scale[1];
        for (int currentSprite = 0; currentSprite < sprites.getCount(); currentSprite++) {
            getRandomSprite(spriteData, rotation, frame++, sceneWidth, sceneHeight);
            sprites.setSprite(currentSprite, spriteData);
            sprites.setSprite(currentSprite, spriteData);
            rotation += 0.01f;
            if (frame >= spriteFrames) {
                frame = 0;
            }
            quadExpander.expandQuadData(currentSprite);
            getRandomEntityData(entityData);
            sprites.setEntityData(currentSprite, mapper.attributesPerVertex, entityData);
        }
    }

    protected void getRandomSprite(float[] spriteData, float rotate, int frame, float sceneWidth, float sceneHeight) {
        spriteData[mapper.translateOffset] = ((random.nextFloat() * sceneWidth) - sceneWidth / 2);
        spriteData[mapper.translateOffset + 1] = ((random.nextFloat() * sceneHeight) - sceneHeight / 2);
        spriteData[mapper.translateOffset + 2] = 1;
        spriteData[mapper.rotateOffset] = 0;
        spriteData[mapper.rotateOffset + 1] = 0;
        spriteData[mapper.rotateOffset + 2] = rotate;
        spriteData[mapper.scaleOffset] = random.nextFloat() + 1;
        spriteData[mapper.scaleOffset + 1] = random.nextFloat() + 1;
        spriteData[mapper.scaleOffset + 2] = 1;
        spriteData[mapper.frameOffset] = frame;
    }

    protected void getRandomEntityData(float[] entityData) {
        entityData[EntityData.MOVE_VECTOR_X.index] = 0;
        entityData[EntityData.MOVE_VECTOR_Y.index] = 0;
        entityData[EntityData.ELASTICITY.index] = 0.5f + random.nextFloat() * 0.5f;
        entityData[EntityData.RESISTANCE.index] = random.nextFloat() * 0.03f;
    }

    private void updateNodeScale() {
        // Scale the bounds according to node scale - this only works while sprites are attached
        float[] scale = scene.getTransform().getScale();
        scaledRect[0] = viewport[ViewFrustum.LEFT_INDEX] / scale[0];
        scaledRect[1] = viewport[ViewFrustum.TOP_INDEX] / scale[1];
        scaledRect[2] = viewFrustum.getWidth() / scale[0];
        scaledRect[3] = viewFrustum.getHeight() / scale[1];
        screenBounds.setBounds(scaledRect);
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
        if (entityData != null) {
            float[] scale = scene.getTransform().getScale();
            sprites.setTransform(currentSprite,
                    new float[] { pos[0] / scale[0], pos[1] / scale[1], 0, 0, 0, 0, 1, 1, 1 });
            getRandomEntityData(entityData);
            sprites.setEntityData(currentSprite, mapper.attributesPerVertex, entityData);
            currentSprite++;
            if (currentSprite > spriteCount - 1) {
                currentSprite = 0;
            }
        }
    }

}
