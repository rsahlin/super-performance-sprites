package com.super2k.tiledspriteengine.jogl;

import com.nucleus.jogl.NucleusApplication;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.super2k.supersprites.SuperSprites;

public class SuperSpritesJOGL extends NucleusApplication {

    public SuperSpritesJOGL(String[] args) {
        super(args, Renderers.GLES20, SuperSprites.class);
    }

    public static void main(String[] args) {
        SuperSpritesJOGL spritesApp = new SuperSpritesJOGL(args);
    }
}
