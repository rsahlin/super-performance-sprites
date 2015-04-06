package com.super2k.tiledspriteengine.sprite;

import com.nucleus.transform.Vector2D;

/**
 * Base sprite class
 * Has data needed for movement, animation and graphics.
 * 
 * @author Richard Sahlin
 *
 */
public abstract class Sprite {

    /**
     * Store the data used by subclasses into this class to prepare for rendering.
     * For some implementations this may do nothing, others may need to copy data from this class.
     */
    abstract void prepare();

    /**
     * Index to x position.
     */
    public final static int X_POS = 0;
    /**
     * Index to y position.
     */
    public final static int Y_POS = 1;
    /**
     * Index to z position.
     */
    public final static int Z_POS = 2;

    public final static int GRAVITY_X = 3;
    public final static int GRAVITY_Y = 4;
    public final static int ELASTICITY = 5;
    public final static int ROTATION = 6; // z axis rotation
    public final static int FRAME = 7;

    /**
     * All sprites can move using a vector
     */
    public Vector2D moveVector = new Vector2D();
    public final float[] floatData = new float[16];
    public final int[] intData = new int[16];

    /**
     * Applies movement and gravity to position, then prepare() is called to let subclasses update
     * 
     * @param deltaTime
     */
    public void move(float deltaTime) {
        floatData[X_POS] += deltaTime * moveVector.vector[Vector2D.X_AXIS] * moveVector.vector[Vector2D.MAGNITUDE] +
                floatData[GRAVITY_X] * deltaTime;
        floatData[Y_POS] += deltaTime * moveVector.vector[Vector2D.Y_AXIS] * moveVector.vector[Vector2D.MAGNITUDE] +
                floatData[GRAVITY_Y] * deltaTime;
        prepare();
    }

    /**
     * Sets the x, y position and frame of this sprite.
     * 
     * @param x
     * @param y
     */
    public void setPosition(float x, float y) {
        floatData[X_POS] = x;
        floatData[Y_POS] = y;
        prepare();
    }

    public void setFrame(int frame) {
    	floatData[FRAME] = frame;
    }
    
    public void setRotation(float rotation) {
    	floatData[ROTATION] = rotation;
    }
    
    /**
     * Updates the gravity vector according to the specified gravity and time
     * 
     * @param x
     * @param y
     * @param deltaTime
     */
    public void updateGravity(float x, float y, float deltaTime) {
        floatData[GRAVITY_X] += x * deltaTime;
        floatData[GRAVITY_Y] += y * deltaTime;
    }
}
