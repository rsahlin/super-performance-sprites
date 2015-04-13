package com.super2k.tiledspriteengine.android;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.nucleus.android.AndroidGLES20Wrapper;
import com.nucleus.android.AndroidRenderer;
import com.nucleus.android.AndroidSurfaceView;
import com.nucleus.matrix.android.AndroidMatrixEngine;
import com.nucleus.mmi.PointerInputProcessor;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.texture.android.AndroidImageFactory;
import com.super2k.tiledspriteengine.TiledSpriteRenderer;

public class TiledSpriteActivity extends Activity {

    private GLSurfaceView mGLView;
    PointerInputProcessor inputProcessor = new PointerInputProcessor();
    Renderer renderer;
    GLES20Wrapper gles = new AndroidGLES20Wrapper();
    TiledSpriteRenderer tiledRenderer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tiledRenderer = new TiledSpriteRenderer(gles, new AndroidImageFactory(), new AndroidMatrixEngine(),
                inputProcessor);
        renderer = new AndroidRenderer(tiledRenderer);
        mGLView = new AndroidSurfaceView(getApplicationContext(), renderer, inputProcessor);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(mGLView);
    }
}
