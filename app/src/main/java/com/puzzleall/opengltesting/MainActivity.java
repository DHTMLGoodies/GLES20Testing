package com.puzzleall.opengltesting;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;


public class MainActivity extends ActionBarActivity {

    private GLSurfaceView mGLSurfaceView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity
        mGLSurfaceView = new MyGLSurfaceView(this);
        setContentView(mGLSurfaceView);

        /*
        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity
        mGLSurfaceView = new GLSurfaceView(this);

        final ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        if(supportsEs2){
            mGLSurfaceView.setEGLContextClientVersion(2);

            mGLSurfaceView.setRenderer(new LessonOneRenderer());
        }

         */
    }

    @Override
    protected void onPause() {
        super.onPause();
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        mGLSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        mGLSurfaceView.onResume();
    }
}
