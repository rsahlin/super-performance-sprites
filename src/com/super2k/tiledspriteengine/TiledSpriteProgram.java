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

    public enum VARIABLES {
        uMVPMatrix(0, ShaderVariable.VariableType.UNIFORM),
        uRenderVec(1, ShaderVariable.VariableType.UNIFORM),
        aPosition(2, ShaderVariable.VariableType.ATTRIBUTE),
        aTileSprite(3, ShaderVariable.VariableType.ATTRIBUTE);

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
        GLUtils.handleError(gles, "glEnableVertexAttribArray ");
        gles.glVertexAttribPointer(attrib.getLocation(), buffer.getComponentCount(), buffer.getDataType(), false,
                buffer.getByteStride(), buffer.getBuffer().position(0));
        ShaderVariable attrib2 = getShaderVariable(VARIABLES.aTileSprite.index);
        gles.glEnableVertexAttribArray(attrib2.getLocation());
        GLUtils.handleError(gles, "glEnableVertexAttribArray ");
        VertexBuffer buffer2 = mesh.getVerticeBuffer(1);
        gles.glVertexAttribPointer(attrib2.getLocation(), buffer2.getComponentCount(), buffer2.getDataType(), false,
                buffer2.getByteStride(), buffer2.getBuffer().position(0));

    }

    public void bindUniforms(GLES20Wrapper gles, BaseRenderer renderer, Mesh mesh) throws GLException {
        ViewFrustum viewFrustum = renderer.getViewFrustum();
        System.arraycopy(viewFrustum.getProjectionMatrix(), 0, uniformMatrices, 0, 16);
        System.arraycopy(viewFrustum.getProjectionMatrix(), 0, uniformMatrices, 16, 16);
        gles.glUniformMatrix4fv(getShaderVariable(VARIABLES.uMVPMatrix.index).getLocation(), 2, false, uniformMatrices,
                0);
        GLUtils.handleError(gles, "glUniformMatrix4fv ");
        // gles.glUniform4fv(getShaderVariable(VARIABLES.uRenderVec.index).getLocation(), 2, uniformVectors, 0);
    }

}
