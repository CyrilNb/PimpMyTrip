package fr.univtln.cniobechoudayer.pimpmytrip.opengl;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class OpenGLActivity extends Activity{

    private GLSurfaceView glSurfaceView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        glSurfaceView = new OpenGLSurfaceView(this);
        setContentView(glSurfaceView);
    }

} 
