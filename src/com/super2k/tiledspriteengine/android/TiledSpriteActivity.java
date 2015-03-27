package com.super2k.tiledspriteengine.android;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.nucleus.android.AndroidSurfaceView;
import com.nucleus.mmi.PointerInputProcessor;
import com.super2k.tiledspriteengine.TiledSpriteRenderer;

public class TiledSpriteActivity extends Activity {

    private GLSurfaceView mGLView;
    PointerInputProcessor inputProcessor = new PointerInputProcessor();
    Renderer renderer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        renderer = new TiledSpriteRenderer(inputProcessor);
        mGLView = new AndroidSurfaceView(getApplicationContext(), renderer, inputProcessor);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(mGLView);
    }

}
