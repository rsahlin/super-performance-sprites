package com.super2k.tiledspriteengine.android;

import android.os.Bundle;

import com.nucleus.android.NucleusActivity;
import com.super2k.tiledspriteengine.SuperSprites;

public class SuperSpritesActivity extends NucleusActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO Register classname for and create then call the init method, this way the Android implementation of
        // this class can be removed.
        SuperSprites superSprites = new SuperSprites();
        superSprites.init(coreApp);
    }
}
