package com.super2k.tiledspriteengine.jogl;

import com.nucleus.jogl.NucleusApplication;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.super2k.tiledspriteengine.SuperSprites;

public class SuperSpritesJOGL extends NucleusApplication {

    SuperSprites superSprites;

    public SuperSpritesJOGL(String[] args) {
        super(args, Renderers.GLES20);
    }

    public static void main(String[] args) {
        SuperSpritesJOGL spritesApp = new SuperSpritesJOGL(args);
    }

    @Override
    public void contextCreated(int width, int height) {
        super.contextCreated(width, height);
        // TODO Register classname for and create then call the init method, this way the Android implementation of
        // this class can be removed.
        if (superSprites == null) {
            superSprites = new SuperSprites();
            superSprites.init(coreApp);
        }
    }
}
