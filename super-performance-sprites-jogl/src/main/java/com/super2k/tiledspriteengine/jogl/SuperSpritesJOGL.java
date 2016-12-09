package com.super2k.tiledspriteengine.jogl;

import com.nucleus.jogl.NucleusApplication;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.super2k.tiledspriteengine.SuperSprites;

public class SuperSpritesJOGL extends NucleusApplication {

    public SuperSpritesJOGL(String[] args) {
        super(args, Renderers.GLES20);
    }

    public static void main(String[] args) {
        SuperSpritesJOGL spritesApp = new SuperSpritesJOGL(args);
    }

    @Override
    public void contextCreated(int width, int height) {
        super.contextCreated(width, height);
        SuperSprites sprites = new SuperSprites();
        sprites.init(coreApp);
    }
}
