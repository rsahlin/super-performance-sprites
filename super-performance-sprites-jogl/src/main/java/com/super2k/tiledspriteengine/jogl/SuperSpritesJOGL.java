package com.super2k.tiledspriteengine.jogl;

import com.nucleus.jogl.JOGLApplication;
import com.super2k.supersprites.SuperSprites;

public class SuperSpritesJOGL extends JOGLApplication {

    public SuperSpritesJOGL(String[] args) {
        super(args, SuperSprites.GL_VERSION, SuperSprites.class);
    }

    public static void main(String[] args) {
        SuperSpritesJOGL spritesApp = new SuperSpritesJOGL(args);
    }
}
