package com.super2k.tiledspriteengine;

import com.nucleus.camera.ViewFrustum;
import com.nucleus.geometry.Mesh;
import com.nucleus.geometry.VertexBuffer;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.renderer.BaseRenderer;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.shader.ShaderVariable;
import com.nucleus.shader.ShaderVariable.VariableType;

/**
 * This class defined the mappings for the Fractal vertex and fragment shaders.
 * 
 * @author Richard Sahlin
 *
 */
public class TiledSpriteProgram extends ShaderProgram {

	/**
	 * Index into uniform sprite data data where the texture fraction s (width) is
	 */
	public final static int UNIFORM_TEX_FRACTION_S_INDEX = 0;
	/**
	 * Index into uniform sprite data data where the texture fraction t (height) is
	 */
	public final static int UNIFORM_TEX_FRACTION_T_INDEX = 1;
	
	/**
	 * Index into uniform sprite data where 1 / texture fraction w - this is used to calculate y pos from frame index
	 */
	public final static int UNIFORM_TEX_ONEBY_S_INDEX = 2;

	public final static int ATTRIBUTE_1_OFFSET = 0;
	public final static int ATTRIBUTE_2_OFFSET = 4;
	
	/**
	 * Index into aTileSprite for x position
	 */
	public final static int ATTRIBUTE_SPRITE_X_INDEX = 0;
	/**
	 * Index into aTileSprite for y position
	 */
	public final static int ATTRIBUTE_SPRITE_Y_INDEX = 1;
	/**
	 * Index into aTileSprite texture u coordinate - this is used to calculate texture coordinate with frame.
	 */
	public final static int ATTRIBUTE_SPRITE_U_INDEX = 2;
	/**
	 * Index into aTileSprite texture v coordinate - this is used to calculate texture coordinate with frame.
	 */
	public final static int ATTRIBUTE_SPRITE_V_INDEX = 3;
	/**
	 * Index into aTileSprite frame number, this is the sprite frame number to use.
	 */
	public final static int ATTRIBUTE_SPRITE_FRAME_INDEX = 4;
	/**
	 * Index into aTileSprite z axis rotation
	 */
	public final static int ATTRIBUTE_SPRITE_ROTATION_INDEX = 5;
	
    public enum VARIABLES {
        uMVPMatrix(0, ShaderVariable.VariableType.UNIFORM),
        uSpriteData(1, ShaderVariable.VariableType.UNIFORM),
        aPosition(2, ShaderVariable.VariableType.ATTRIBUTE),
        aTileSprite(3, ShaderVariable.VariableType.ATTRIBUTE),
        aTileSprite2(4, ShaderVariable.VariableType.ATTRIBUTE);

        public final int index;
        private final VariableType type;

        private VARIABLES(int index, VariableType type) {
            this.index = index;
            this.type = type;
        }

    }

    protected final static String VERTEX_SHADER_NAME = "assets/vertexshader.essl";
    protected final static String FRAGMENT_SHADER_NAME = "assets/fragmentshader.essl";

    public TiledSpriteProgram() {
        super();
        uniformMatrices = new float[16 * 2];
    }

    @Override
    public int getVariableIndex(ShaderVariable variable) {
        return VARIABLES.valueOf(getVariableName(variable)).index;
    }

    public int getVariableCount() {
        return VARIABLES.values().length;
    }

    public void bindAttributes(GLES20Wrapper gles, BaseRenderer renderer, Mesh mesh) throws GLException {

        VertexBuffer buffer = mesh.getVerticeBuffer(0);
        ShaderVariable attrib = getShaderVariable(VARIABLES.aPosition.index);
        gles.glEnableVertexAttribArray(attrib.getLocation());
        GLUtils.handleError(gles, "glEnableVertexAttribArray1 ");
        gles.glVertexAttribPointer(attrib.getLocation(), buffer.getComponentCount(), buffer.getDataType(), false,
                buffer.getByteStride(), buffer.getBuffer().position(0));
        GLUtils.handleError(gles, "glVertexAttribPointer1 ");
        ShaderVariable attrib2 = getShaderVariable(VARIABLES.aTileSprite.index);
        gles.glEnableVertexAttribArray(attrib2.getLocation());
        GLUtils.handleError(gles, "glEnableVertexAttribArray2 ");
        VertexBuffer buffer2 = mesh.getVerticeBuffer(1);
        gles.glVertexAttribPointer(attrib2.getLocation(), buffer2.getComponentCount(), buffer2.getDataType(), false,
                buffer2.getByteStride(), buffer2.getBuffer().position(ATTRIBUTE_1_OFFSET));
        ShaderVariable attrib3 = getShaderVariable(VARIABLES.aTileSprite2.index);
        if (attrib3 != null) {
            gles.glEnableVertexAttribArray(attrib3.getLocation());
            GLUtils.handleError(gles, "glEnableVertexAttribArray3 ");
            VertexBuffer buffer3 = mesh.getVerticeBuffer(1);
            gles.glVertexAttribPointer(attrib3.getLocation(), buffer3.getComponentCount(), buffer3.getDataType(), false,
            		buffer3.getByteStride(), buffer3.getBuffer().position(ATTRIBUTE_2_OFFSET));
        }
        GLUtils.handleError(gles, "glVertexAttribPointer3 ");

    }

    public void bindUniforms(GLES20Wrapper gles, BaseRenderer renderer, Mesh mesh) throws GLException {
        ViewFrustum viewFrustum = renderer.getViewFrustum();
        System.arraycopy(viewFrustum.getProjectionMatrix(), 0, uniformMatrices, 0, 16);
        System.arraycopy(viewFrustum.getProjectionMatrix(), 0, uniformMatrices, 16, 16);
        gles.glUniformMatrix4fv(getShaderVariable(VARIABLES.uMVPMatrix.index).getLocation(), 2, false, uniformMatrices, 0);
        GLUtils.handleError(gles, "glUniformMatrix4fv ");
        ShaderVariable v = getShaderVariable(VARIABLES.uSpriteData.index);
        if (v != null) {
            gles.glUniform4fv(v.getLocation(), 2, mesh.getUniformVectors(), 0);
        }
    }

}
