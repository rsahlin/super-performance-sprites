package com.super2k.tiledspriteengine.android;

import android.os.Bundle;

import com.nucleus.android.BaseActivity;
import com.super2k.tiledspriteengine.SuperSprites;

public class TiledSpriteActivity extends BaseActivity {

    private SuperSprites superSprites;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        superSprites = new SuperSprites(baseRenderer, inputProcessor);
        baseRenderer.addFrameListener(superSprites);
    }
}
