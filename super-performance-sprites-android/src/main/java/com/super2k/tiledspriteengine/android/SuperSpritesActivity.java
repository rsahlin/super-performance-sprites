package com.super2k.tiledspriteengine.android;

import com.nucleus.android.NucleusActivity;
import com.super2k.supersprites.SuperSprites;

import android.os.Bundle;

public class SuperSpritesActivity extends NucleusActivity {

    private SuperSprites superSprites;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
