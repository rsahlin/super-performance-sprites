package com.super2k.supersprites.android;

import com.nucleus.CoreApp;
import com.nucleus.android.NucleusActivity;
import com.super2k.supersprites.SuperSprites;
import com.super2k.supersprites.SuperSprites.ClientClasses;

import android.os.Bundle;

public class SuperSpritesActivity extends NucleusActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        useChoreographer = true;
        CoreApp.setClientClass(ClientClasses.clientclass);
        super.onCreate(savedInstanceState);
    }

    @Override
    public com.nucleus.renderer.NucleusRenderer.Renderers getRenderVersion() {
        return SuperSprites.GL_VERSION;
    }

    @Override
    public int getSamples() {
        return 0;
    }

}
