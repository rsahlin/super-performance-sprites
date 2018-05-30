package com.super2k.supersprites.system;

import java.util.Random;

import com.graphicsengine.component.ActorComponent;
import com.graphicsengine.component.ActorComponent.EntityMapper;
import com.graphicsengine.component.SpriteAttributeComponent;
import com.nucleus.bounds.RectangularBounds;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.common.Constants;
import com.nucleus.component.CPUComponentBuffer;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.Node;
import com.nucleus.scene.Node.NodeTypes;
import com.nucleus.scene.RootNode;
import com.nucleus.system.System;
import com.nucleus.vecmath.Rectangle;

/**
 * The system for controlling the sprites defined by {@linkplain SpriteAttributeComponent}
 * This is the collected functionality for the sprites, use this setup so that data is shared in such a way
 * that the logic can be accelerated by APIs such as OpenCL.
 * 
 * @author Richard Sahlin
 *
 */
public class SuperSpriteSystem extends System<SpriteAttributeComponent> {

    public final static float GRAVITY = -5000;

    RootNode root;
    private Node scene;
    private RectangularBounds screenBounds = new RectangularBounds(new Rectangle(0, 0, 0, 0));
    public float[] scaledRect = new float[4];
    public float[] viewport = new float[ViewFrustum.PROJECTION_SIZE];
    protected ViewFrustum viewFrustum;
    private boolean initialized = false;
    private SpriteAttributeComponent sprites;
    float[] entityData;
    EntityMapper mapper;
    private int spriteCount;
    private int currentSprite = 0;
    private Random random = new Random(java.lang.System.currentTimeMillis());
    /**
     * Source
     */
    private CPUComponentBuffer entityBuffer;

    public SuperSpriteSystem() {
    }

    @Override
    public void process(SpriteAttributeComponent component, float deltaTime) {
        if (!initialized) {
            throw new IllegalStateException("initSystem() must be called before calling process()");
        }
        updateNodeScale();
        int quadIndex = 0;
        float[] entityData = entityBuffer.getData();
        float yMin = scaledRect[1] - scaledRect[3];
        float xMin = scaledRect[0] - scaledRect[2];
        float xMax = scaledRect[0] + scaledRect[2];
        float[] pos = new float[3];
        for (int sprite = 0; sprite < spriteCount; sprite++) {
            // Update gravity
            entityData[mapper.moveVectorOffset + 1 + quadIndex] += GRAVITY * deltaTime;
            entityData[mapper.rotate + quadIndex] += deltaTime * entityData[mapper.rotateSpeedOffset + quadIndex];
            if (entityData[mapper.rotate + quadIndex] > Constants.TWOPI) {
                entityData[mapper.rotate + quadIndex] -= Constants.TWOPI;
            }
            float xpos = entityData[mapper.translate + quadIndex];
            float ypos = entityData[mapper.translate + 1 + quadIndex];

            xpos += deltaTime * entityData[mapper.moveVectorOffset + quadIndex];
            ypos += deltaTime * entityData[mapper.moveVectorOffset + 1 + quadIndex];
            if (ypos < yMin) {
                entityData[mapper.moveVectorOffset + 1
                        + quadIndex] = -entityData[mapper.moveVectorOffset + 1 + quadIndex]
                                * entityData[mapper.elasticityOffset + quadIndex];
                ypos = yMin - (ypos - yMin);
            }
            if (xpos > xMax) {
                xpos = xMax - (xpos - xMax);
                entityData[mapper.moveVectorOffset + quadIndex] = -entityData[mapper.moveVectorOffset + quadIndex]
                        * entityData[mapper.elasticityOffset + quadIndex];
                entityData[mapper.rotateSpeedOffset] = entityData[mapper.rotateSpeedOffset + quadIndex]
                        * entityData[mapper.elasticityOffset + quadIndex];
            } else if (xpos < xMin) {
                xpos = xMin - (xpos - xMin);
                entityData[mapper.moveVectorOffset + 1
                        + quadIndex] = -entityData[mapper.moveVectorOffset + 1 + quadIndex]
                                * entityData[mapper.elasticityOffset + quadIndex];
            }
            pos[0] = xpos;
            pos[1] = ypos;
            sprites.setEntity(sprite, 0, pos, 0, 2);
            quadIndex += entityBuffer.getSizePerEntity();
        }
    }

    @Override
    public void initSystem(NucleusRenderer renderer, RootNode root, SpriteAttributeComponent component) {
        initialized = true;
        this.root = root;
        scene = root.getNodeById("scene");
        // Get the view frustum and create rectangle bounds
        viewFrustum = root.getNodeByType(NodeTypes.layernode.name()).getViewFrustum();
        viewFrustum.getValues(viewport);
        initSprites(component);
    }

    private void initSprites(SpriteAttributeComponent sprites) {
        this.sprites = sprites;
        this.entityBuffer = (CPUComponentBuffer) sprites.getEntityBuffer();
        int spriteFrames = sprites.getFrameCount();
        spriteCount = sprites.getCount();
        int frame = 0;
        float rotation = 0;
        mapper = sprites.getMapper();
        entityData = new float[entityBuffer.getSizePerEntity()];
        float[] scale = scene.getTransform().getScale();
        float sceneWidth = viewFrustum.getWidth() / scale[0];
        float sceneHeight = viewFrustum.getHeight() / scale[1];
        for (int currentSprite = 0; currentSprite < sprites.getCount(); currentSprite++) {
            ActorComponent.getRandomSprite(entityData, rotation, frame++, sceneWidth, sceneHeight, mapper, random);
            ActorComponent.getRandomEntityData(entityData, mapper, random);
            sprites.setEntity(currentSprite, 0, entityData, 0, entityData.length);
            rotation += 0.01f;
            if (frame >= spriteFrames) {
                frame = 0;
            }
        }
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

    /**
     * 
     * @param pos
     */
    public void releaseSprite(float[] pos) {
        if (entityData != null) {
            float[] scale = scene.getTransform().getScale();
            entityData[mapper.translate] = pos[0] / scale[0];
            entityData[mapper.translate + 1] = pos[1] / scale[1];
            // Only update position
            sprites.setEntity(currentSprite, 0, entityData, 0, 2);
            // Reset / new entity data.
            ActorComponent.getRandomEntityData(entityData, mapper, random);
            sprites.setEntity(currentSprite, mapper.attributesPerVertex, entityData, mapper.attributesPerVertex,
                    mapper.attributesPerEntity - mapper.attributesPerVertex);
            currentSprite++;
            if (currentSprite > spriteCount - 1) {
                currentSprite = 0;
            }
        }
    }

}
