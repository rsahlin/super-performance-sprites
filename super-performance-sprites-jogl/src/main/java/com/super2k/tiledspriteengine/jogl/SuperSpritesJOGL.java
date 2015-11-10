package com.super2k.tiledspriteengine.jogl;

import com.nucleus.jogl.NucleusApplication;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.super2k.tiledspriteengine.SuperSprites;

public class SuperSpritesJOGL extends NucleusApplication {

    public static void main(String[] args) {
        SuperSpritesJOGL spritesApp = new SuperSpritesJOGL();
        spritesApp.setProperties(args);
        spritesApp.create(Renderers.GLES20);
    }

    public void create(Renderers version) {
        super.createCore(version);
    }

    @Override
    public void contextCreated(int width, int height) {
        super.contextCreated(width, height);
        SuperSprites sprites = new SuperSprites();
        sprites.init(coreApp);
    }
}
