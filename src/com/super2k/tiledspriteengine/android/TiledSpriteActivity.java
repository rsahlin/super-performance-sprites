package com.super2k.tiledspriteengine.android;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.nucleus.android.BaseActivity;
import com.nucleus.matrix.android.AndroidMatrixEngine;
import com.nucleus.texture.android.AndroidImageFactory;
import com.super2k.tiledspriteengine.TiledSpriteRenderer;

public class TiledSpriteActivity extends BaseActivity {

    TiledSpriteRenderer tiledRenderer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tiledRenderer = new TiledSpriteRenderer(gles, new AndroidImageFactory(), new AndroidMatrixEngine(),
                inputProcessor);
        setup(tiledRenderer, GLSurfaceView.RENDERMODE_CONTINUOUSLY, LayoutParams.FLAG_FULLSCREEN,
                Window.FEATURE_NO_TITLE);
    }
}
