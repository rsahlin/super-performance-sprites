package com.super2k.tiledspriteengine.sprite;

import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLES20Wrapper.GLES20;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.texturing.Texture2D;
import com.super2k.tiledspriteengine.MeshBuilder;
import com.super2k.tiledspriteengine.TiledSpriteProgram;

/**
 * Controller for tiled sprites, this controller creates the tiled sprite objects.
 * A tiled sprite (quad) can be drawn in one draw call together with a large number of other sprites (they share the
 * same Mesh).
 * This is to allow a very large number of sprites in just 1 draw call to the underlying render API (OpenGLES).
 * Performance is increased, but all sprites must share the same texture atlas.
 * 
 * @author Richard Sahlin
 *
 */
public class TiledSpriteController {

    /**
     * Number of floats for each tiled sprite in the attribute data.
     */
    public final static int SPRITE_ATTRIBUTE_DATA = TiledSprite.PER_VERTEX_DATA * TiledSprite.VERTICES_PER_SPRITE;

    TiledSprite[] sprites;
    private Mesh mesh;
    private float[] uniformVector = new float[] { 0, 0, 0, 0, 0, 0, 0, 0 };
    float[] data;
    int count;

    /**
     * Creates a TiledSpriteController with the specified number of sprites, each sprite can be seen as a portion of the
     * Mesh it belongs to. Each tiled sprite will be created.
     * Before the sprites can be rendered the Mesh must be created, by calling createMesh()
     * 
     * @param count Number of tiled sprites to create.
     * 
     */
    public TiledSpriteController(int count) {
        this.count = count;
        sprites = new TiledSprite[count];
        data = new float[count * SPRITE_ATTRIBUTE_DATA];
        int frame = 0;
        for (int i = 0; i < count; i++) {
            sprites[i] = new TiledSprite(data, i * SPRITE_ATTRIBUTE_DATA);
        }
        
    }

    /**
     * Creates the Mesh to be rendered.
     * @param program
     * @param texture
     * @param width
     * @param height
     * @param texFractionS Texture width of this sprite as a fraction of tile sprite sheet, ie 0.1 for 1/10 of the width
     * @param texFractionT Texture height of this sprite as a fraction of tile sprite sheet, ie 0.1 for 1/10 of the height
     * @return
     */
    public Mesh createMesh(ShaderProgram program, Texture2D texture, float width, float height, float texFractionS, float texFractionT) {
    	
        mesh = MeshBuilder.buildTileSpriteMesh(program, count, width, height, 0, GLES20.GL_FLOAT);
        mesh.setTexture(texture, Texture2D.TEXTURE_0);
        mesh.setUniformVectors(uniformVector);
        mesh.getUniformVectors()[TiledSpriteProgram.UNIFORM_TEX_FRACTION_S_INDEX] = texFractionS;
        mesh.getUniformVectors()[TiledSpriteProgram.UNIFORM_TEX_FRACTION_T_INDEX] = texFractionT;
        mesh.getUniformVectors()[TiledSpriteProgram.UNIFORM_TEX_ONEBY_S_INDEX] = (int) (1 / texFractionS);
        
        return mesh;
    }
    
    public float[] getData() {
        return data;
    }

    public int getCount() {
        return count;
    }

    public TiledSprite[] getSprites() {
        return sprites;
    }
    
    public Mesh getMesh() {
    	return mesh;
    }

}
