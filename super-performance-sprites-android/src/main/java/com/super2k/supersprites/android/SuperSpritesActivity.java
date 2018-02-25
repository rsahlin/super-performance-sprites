package com.super2k.supersprites.android;

import com.nucleus.android.NucleusActivity;
import com.nucleus.opengl.GLESWrapper.Renderers;
import com.super2k.supersprites.SuperSprites;

import android.os.Bundle;

public class SuperSpritesActivity extends NucleusActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        clientClass = SuperSprites.class;
        super.onCreate(savedInstanceState);
    }

    @Override
    public Renderers getRenderVersion() {
        return SuperSprites.GL_VERSION;
    }

    @Override
    public int getSamples() {
        return 0;
    }

}
