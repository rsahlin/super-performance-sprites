package com.super2k.tiledspriteengine.lwjgl3;

import com.nucleus.lwjgl3.LWJGL3Application;
import com.super2k.supersprites.SuperSprites;
import com.super2k.supersprites.SuperSprites.ClientClasses;

public class SuperSpritesLWJGL3 extends LWJGL3Application {

    public SuperSpritesLWJGL3(String[] args) {
        super(args, SuperSprites.GL_VERSION, ClientClasses.clientclass);

    }

    public static void main(String[] args) {
        SuperSpritesLWJGL3 main = new SuperSpritesLWJGL3(args);
        main.run();
    }

}
