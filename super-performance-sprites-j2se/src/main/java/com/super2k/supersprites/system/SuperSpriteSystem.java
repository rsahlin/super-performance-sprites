package com.super2k.supersprites.system;

import com.graphicsengine.component.SpriteComponent;
import com.graphicsengine.component.SpriteComponent.SpriteData;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.component.Component;
import com.nucleus.geometry.AttributeUpdater.PropertyMapper;
import com.nucleus.renderer.NucleusRenderer.Layer;
import com.nucleus.scene.Node;
import com.nucleus.scene.RootNode;
import com.nucleus.scene.ViewNode;
import com.nucleus.shader.ShaderProgram;
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
    private ViewNode viewNode;
    public static float[] worldLimit = new float[4];
    private float orthoLeft;
    private float orthoTop;
    private boolean initialized = false;
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
        float[] attributeData = spriteComponent.getAttributeData();
        float[] spriteData = spriteComponent.getSpriteData();
        Vector2D[] moveVector = spriteComponent.getMoveVector();
        PropertyMapper mapper = spriteComponent.getMapper();

        int readIndex = 0;
        int writeIndex = 0;
        int readLength = SpriteComponent.SpriteData.getSize();
        for (int sprite = 0; sprite < spriteCount; sprite++) {
            spriteData[SpriteData.ROTATE.index + readIndex] += deltaTime
                    * spriteData[SpriteData.ROTATE_SPEED.index + readIndex];
            if (spriteData[SpriteData.ROTATE.index + readIndex] > TWOPI) {
                spriteData[SpriteData.ROTATE.index + readIndex] -= TWOPI;
            }
            // Update gravity
            spriteData[SpriteData.MOVE_VECTOR_Y.index
                    + readIndex] += GRAVITY * deltaTime;

            float xpos = spriteData[SpriteData.TRANSLATE_X.index + readIndex];
            float ypos = spriteData[SpriteData.TRANSLATE_Y.index + readIndex];

            xpos += deltaTime * moveVector[sprite].vector[VecMath.X] * moveVector[sprite].vector[Vector2D.MAGNITUDE] + spriteData[SpriteData.MOVE_VECTOR_X.index + readIndex] * deltaTime;
            ypos += deltaTime * moveVector[sprite].vector[VecMath.Y] * moveVector[sprite].vector[Vector2D.MAGNITUDE] + spriteData[SpriteData.MOVE_VECTOR_Y.index + readIndex] * deltaTime;
            if (ypos < worldLimit[3]) {
	             spriteData[SpriteData.MOVE_VECTOR_Y.index + readIndex] = -spriteData[SpriteData.MOVE_VECTOR_Y.index + readIndex] * spriteData[SpriteData.ELASTICITY.index + readIndex];
	             ypos = worldLimit[3] - (ypos - worldLimit[3]);
            }
            if (xpos > worldLimit[2]) {
            	xpos = worldLimit[2] - (xpos - worldLimit[2]);
            	moveVector[sprite].vector[VecMath.X] = -moveVector[sprite].vector[VecMath.X] * spriteData[SpriteData.ELASTICITY.index + readIndex];
            	spriteData[SpriteData.ROTATE_SPEED.index + readIndex] = -spriteData[SpriteData.ROTATE_SPEED.index + readIndex] * spriteData[SpriteData.ELASTICITY.index];
            } else if (xpos < worldLimit[0]) {
            	xpos = worldLimit[0] - (xpos - worldLimit[0]);
            	moveVector[sprite].vector[VecMath.X] = -moveVector[sprite].vector[VecMath.X] * spriteData[SpriteData.ELASTICITY.index + readIndex];
            	spriteData[SpriteData.ROTATE_SPEED.index + readIndex] = -spriteData[SpriteData.ROTATE_SPEED.index + readIndex] * spriteData[SpriteData.ELASTICITY.index + readIndex];
            }
            
            float rotate = spriteData[SpriteData.ROTATE.index + readIndex];
            spriteData[SpriteData.TRANSLATE_X.index + readIndex] = xpos;
            spriteData[SpriteData.TRANSLATE_Y.index + readIndex] = ypos;
            for (int i = 0; i < ShaderProgram.VERTICES_PER_SPRITE; i++) {
                attributeData[writeIndex + mapper.translateOffset] = xpos;
                attributeData[writeIndex + mapper.translateOffset + 1] = ypos;
                // attributeData[index + mapper.TRANSLATE_INDEX + 2] = zpos;
                attributeData[writeIndex + mapper.rotateOffset + 2] = rotate;
                writeIndex += mapper.attributesPerVertex;
            }
            readIndex += readLength;
        }
    }

    @Override
    public void initSystem(RootNode root) {
        initialized = true;
        this.root = root;

        Node scene = root.getScene();
        ViewFrustum vf = scene.getViewFrustum();
        float[] values = vf.getValues();
        orthoLeft = values[ViewFrustum.LEFT_INDEX];
        orthoTop = values[ViewFrustum.TOP_INDEX];
        viewNode = root.getViewNode(Layer.SCENE);
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

}
