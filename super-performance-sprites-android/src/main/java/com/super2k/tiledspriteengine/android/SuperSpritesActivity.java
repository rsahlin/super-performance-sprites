package com.super2k.tiledspriteengine.android;

import com.nucleus.android.NucleusActivity;
import com.super2k.supersprites.SuperSprites;

import android.os.Bundle;

public class SuperSpritesActivity extends NucleusActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	clientClass = SuperSprites.class;
        super.onCreate(savedInstanceState);
    }
}
