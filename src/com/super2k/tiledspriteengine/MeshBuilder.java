package com.super2k.tiledspriteengine;

import com.nucleus.geometry.ElementBuffer;
import com.nucleus.geometry.ElementBuffer.Mode;
import com.nucleus.geometry.ElementBuffer.Type;
import com.nucleus.geometry.ElementBuilder;
import com.nucleus.geometry.Material;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.VertexBuffer;
import com.nucleus.opengl.GLES20Wrapper.GLES20;
import com.nucleus.shader.ShaderVariable;
import com.super2k.tiledspriteengine.TiledSpriteProgram.VARIABLES;
import com.super2k.tiledspriteengine.sprite.TiledSprite;

/**
 * Utility class to build different type of meshes, different shader programs need different data from the mesh.
 * This class helps to build meshes.
 * 
 * @author Richard Sahlin
 *
 */
public class MeshBuilder {

    /**
     * Number of vertices per sprite - this is for a quad that is created using element buffer.
     */
    public final static int VERTICES_PER_SPRITE = 4;

    /**
     * Draw using an index list each quad is made up of 6 indices (2 triangles)
     */
    public final static int INDICES_PER_SPRITE = 6;

    /**
     * Default number of components (x,y,z)
     */
    public final static int DEFAULT_COMPONENTS = 3;

    private final static String ILLEGAL_DATATYPE_STR = "Illegal datatype: ";

    /**
     * Builds a mesh with data that can be rendered using a tiled sprite renderer, this will draw a number of
     * sprites using one drawcall.
     * Vertex buffer will have storage for XYZ + UV.
     * 
     * @param tiledSpriteProgram The program that the vertice/attribute buffers will be appended to - this must be a
     * program that can use the attribute data.
     * @param spriteCount Number of sprites to build, this is NOT the vertex count.
     * @param width The width of a sprite, the sprite will be centered in the middle.
     * @param width The height of a sprite, the sprite will be centered in the middle.
     * @param z The z position for each vertice.
     * @param type The datatype for attribute data - GLES20.GL_FLOAT
     * 
     * @return The mesh that can be rendered.
     * @throws IllegalArgumentException if type is not GLES20.GL_FLOAT
     */
    public static Mesh buildTileSpriteMesh(TiledSpriteProgram tiledSpriteProgram, int spriteCount, float width,
            float height, float z, int type) {

        int vertexStride = DEFAULT_COMPONENTS;
        float[] quadPositions = new float[vertexStride * VERTICES_PER_SPRITE];

        float halfWidth = width / 2;
        float halfHeight = height / 2;
        com.nucleus.geometry.MeshBuilder.setPosition(-halfWidth, -halfHeight, z, quadPositions, 0);
        com.nucleus.geometry.MeshBuilder.setPosition(halfWidth, -halfHeight, z, quadPositions,
                vertexStride);
        com.nucleus.geometry.MeshBuilder.setPosition(halfWidth, halfHeight, z, quadPositions,
                vertexStride * 2);
        com.nucleus.geometry.MeshBuilder.setPosition(-halfWidth, halfHeight, z, quadPositions,
                vertexStride * 3);

        return buildTileSpriteMesh(tiledSpriteProgram, spriteCount, quadPositions, type);
    }

    /**
     * Builds a mesh with data that can be rendered using a tiled sprite renderer, this will draw a number of
     * sprites using one drawcall.
     * Vertex buffer will have storage for XYZ + UV.
     * 
     * @param tiledSpriteProgram The program that the vertice/attribute buffers will be appended to - this must be a
     * program that can use the attribute data.
     * @param spriteCount Number of sprites to build, this is NOT the vertex count.
     * @param quadPositions Array with x,y,z - this is set for each tile. Must contain data for 4 vertices.
     * @param type The datatype for attribute data - GLES20.GL_FLOAT
     * 
     * @return The mesh that can be rendered.
     * @throws IllegalArgumentException if type is not GLES20.GL_FLOAT
     */
    public static Mesh buildTileSpriteMesh(TiledSpriteProgram tiledSpriteProgram, int spriteCount,
            float[] quadPositions,
            int type) {
        if (type != GLES20.GL_FLOAT) {
            throw new IllegalArgumentException(ILLEGAL_DATATYPE_STR + type);
        }
        VertexBuffer[] attributes = new VertexBuffer[2];
        /**
         * Create the buffer for vertex position and UV
         */
        attributes[0] = new VertexBuffer(spriteCount * VERTICES_PER_SPRITE, DEFAULT_COMPONENTS, DEFAULT_COMPONENTS,
                type);
        attributes[1] = new VertexBuffer(spriteCount * VERTICES_PER_SPRITE, 4, TiledSprite.PER_VERTEX_DATA,
                GLES20.GL_FLOAT);
        ElementBuffer indices = new ElementBuffer(Mode.TRIANGLES, INDICES_PER_SPRITE * spriteCount, Type.SHORT);
        ElementBuilder.buildQuadBuffer(indices, indices.getCount() / INDICES_PER_SPRITE, 0);

        float[] vertices = new float[spriteCount * VERTICES_PER_SPRITE * DEFAULT_COMPONENTS];
        int destPos = 0;
        for (int i = 0; i < spriteCount; i++) {
            System.arraycopy(quadPositions, 0, vertices, destPos, quadPositions.length);
            destPos += quadPositions.length;
        }

        attributes[0].setPosition(vertices, 0, 0, spriteCount * VERTICES_PER_SPRITE);
        Material material = new Material(tiledSpriteProgram);
        Mesh mesh = new Mesh(indices, attributes, material, null);
        // TODO: Move to generic method, pass Variable to use as vector/matrix storage
        ShaderVariable uVectors = tiledSpriteProgram.getShaderVariable(VARIABLES.uSpriteData.index);
        float[] uniformVectors = new float[uVectors.getSizeInFloats()];
        mesh.setUniformVectors(uniformVectors);
        ShaderVariable uMatrices = tiledSpriteProgram.getShaderVariable(VARIABLES.uMVPMatrix.index);
        float[] uniformMatrices = new float[uMatrices.getSizeInFloats()];
        mesh.setUniformMatrices(uniformMatrices);
        return mesh;

    }
}
