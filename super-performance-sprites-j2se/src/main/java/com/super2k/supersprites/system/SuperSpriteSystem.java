package com.super2k.supersprites.system;

import java.util.Random;

import com.graphicsengine.component.ActorComponent;
import com.graphicsengine.component.ActorComponent.ActorVariables;
import com.graphicsengine.component.SpriteAttributeComponent;
import com.nucleus.bounds.RectangularBounds;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.common.Constants;
import com.nucleus.component.CPUComponentBuffer;
import com.nucleus.geometry.AttributeUpdater.BufferIndex;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.LayerNode;
import com.nucleus.scene.RootNode;
import com.nucleus.shader.VariableIndexer;
import com.nucleus.shader.VariableIndexer.Property;
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

    public static final String GRAVITY_KEY = "gravity";
    public static final String DEFAULT_GRAVITY = "-500";

    public static float GRAVITY;

    RootNode root;
    protected LayerNode scene;
    protected RectangularBounds screenBounds = new RectangularBounds(new Rectangle(0, 0, 0, 0));
    public float[] scaledRect = new float[4];
    public float[] viewport = new float[ViewFrustum.PROJECTION_SIZE];
    protected ViewFrustum viewFrustum;
    float[] entityData;
    protected int currentSprite = 0;
    protected Random random = new Random(java.lang.System.currentTimeMillis());
    float[] rectBounds = new float[4];
    VariableIndexer mapper;
    private int translateOffset;
    int sizePerVertex;

    public SuperSpriteSystem() {
    }

    @Override
    public void process(SpriteAttributeComponent component, float deltaTime) {
        if (!component.isInitialized()) {
            throw new IllegalStateException("Component " + component.getId() + " is not initialized");
        }
        CPUComponentBuffer entityBuffer = (CPUComponentBuffer) component.getEntityBuffer();
        updateNodeScale();
        int quadIndex = 0;
        float[] entityData = entityBuffer.getData();
        float yMin = scaledRect[1] - scaledRect[3];
        float xMin = scaledRect[0] - scaledRect[2];
        float xMax = scaledRect[0] + scaledRect[2];
        float[] pos = new float[3];
        int spriteCount = component.getCount();
        float boundY;
        float elasticity;
        for (int sprite = 0; sprite < spriteCount; sprite++) {
            // Update gravity
            entityData[sizePerVertex + ActorVariables.MOVEVECTOR.offset + 1 + quadIndex] += GRAVITY * deltaTime;
            entityData[sizePerVertex + ActorVariables.ROTATESPEED.offset + quadIndex] += deltaTime
                    * entityData[sizePerVertex + ActorVariables.ROTATESPEED.offset + quadIndex];
            if (entityData[sizePerVertex + ActorVariables.ROTATESPEED.offset + 2 + quadIndex] > Constants.TWOPI) {
                entityData[sizePerVertex + ActorVariables.ROTATESPEED.offset + 2 + quadIndex] -= Constants.TWOPI;
            }
            float xpos = entityData[translateOffset + quadIndex];
            float ypos = entityData[translateOffset + 1 + quadIndex];
            elasticity = entityData[sizePerVertex + ActorVariables.ELASTICITY.offset + quadIndex];

            xpos += deltaTime * entityData[sizePerVertex + ActorVariables.MOVEVECTOR.offset + quadIndex];
            ypos += deltaTime * entityData[sizePerVertex + ActorVariables.MOVEVECTOR.offset + 1 + quadIndex];
            boundY = ypos + entityData[sizePerVertex + ActorVariables.BOUNDINGBOX.offset + 1 + quadIndex]
                    - entityData[sizePerVertex + ActorVariables.BOUNDINGBOX.offset + 3 + quadIndex];
            if (boundY < yMin) {
                entityData[sizePerVertex + ActorVariables.MOVEVECTOR.offset + 1
                        + quadIndex] = -entityData[sizePerVertex + ActorVariables.MOVEVECTOR.offset + 1 + quadIndex]
                                * elasticity;
                ypos = ypos + (yMin - boundY) * elasticity;
            }
            if (xpos > xMax) {
                xpos = xMax - (xpos - xMax);
                entityData[sizePerVertex + ActorVariables.MOVEVECTOR.offset
                        + quadIndex] = -entityData[sizePerVertex + ActorVariables.MOVEVECTOR.offset + quadIndex]
                                * entityData[sizePerVertex + ActorVariables.ELASTICITY.offset + quadIndex];
                entityData[sizePerVertex + ActorVariables.ROTATESPEED.offset] = entityData[sizePerVertex
                        + ActorVariables.ROTATESPEED.offset
                        + quadIndex]
                        * elasticity;
            } else if (xpos < xMin) {
                xpos = xMin - (xpos - xMin);
                entityData[sizePerVertex + ActorVariables.MOVEVECTOR.offset + 1
                        + quadIndex] = -entityData[sizePerVertex + ActorVariables.MOVEVECTOR.offset + 1 + quadIndex]
                                * elasticity;
            }
            pos[0] = xpos;
            pos[1] = ypos;
            component.setEntity(sprite, 0, pos, 0, 2);
            quadIndex += entityBuffer.getSizePerEntity();
        }
    }

    @Override
    public void initComponent(NucleusRenderer renderer, RootNode root, SpriteAttributeComponent component) {
        this.root = root;
        scene = root.getNodeById("scene", LayerNode.class);
        // Get the view frustum and create rectangle bounds
        viewFrustum = scene.getViewFrustum();
        viewFrustum.getValues(viewport);
        mapper = component.getParent().getPipeline().getLocationMapping();
        sizePerVertex = mapper.getSizePerVertex(BufferIndex.ATTRIBUTES.index);
        translateOffset = mapper.getOffset(Property.TRANSLATE.getLocation());
        initSprites(component);
    }

    protected void initSprites(SpriteAttributeComponent component) {
        CPUComponentBuffer entityBuffer = (CPUComponentBuffer) component.getEntityBuffer();
        int spriteFrames = component.getFrameCount();
        component.get2DBounds(rectBounds);
        int frame = 0;
        if (entityData == null) {
            entityData = new float[entityBuffer.getSizePerEntity()];
        }
        float[] scale = scene.getTransform().getScale();
        float sceneWidth = viewFrustum.getWidth() / scale[0];
        float sceneHeight = viewFrustum.getHeight() / scale[1];
        for (int currentSprite = 0; currentSprite < component.getCount(); currentSprite++) {
            ActorComponent.getRandomSprite(entityData, 0, frame++, 1, 1, sceneWidth, sceneHeight, random, mapper);
            ActorComponent.getRandomEntityData(entityData, rectBounds, 0.1f, random, mapper, sizePerVertex);
            component.setEntity(currentSprite, 0, entityData, 0, entityData.length);
            if (frame >= spriteFrames) {
                frame = 0;
            }
        }
    }

    protected void updateNodeScale() {
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
    public void releaseSprite(SpriteAttributeComponent component, float[] pos) {
        component.get2DBounds(rectBounds);
        if (entityData != null) {
            float[] scale = scene.getTransform().getScale();
            entityData[translateOffset] = pos[0] / scale[0];
            entityData[translateOffset + 1] = pos[1] / scale[1];
            // Only update position
            component.setEntity(currentSprite, 0, entityData, 0, 2);
            // Reset / new entity data.
            ActorComponent.getRandomEntityData(entityData, rectBounds, 0.01f, random, mapper, sizePerVertex);
            component.setEntity(currentSprite, sizePerVertex, entityData, sizePerVertex, ActorVariables.SIZE.offset);
            currentSprite++;
            if (currentSprite > component.getCount() - 1) {
                currentSprite = 0;
            }
        }
    }

    @Override
    public void initSystem(NucleusRenderer renderer, RootNode root) {
        GRAVITY = Float.parseFloat(root.getProperty(GRAVITY_KEY, DEFAULT_GRAVITY));
        initialized = true;
    }

}
