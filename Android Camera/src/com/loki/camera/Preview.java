package com.loki.camera;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class Preview extends SurfaceView implements SurfaceHolder.Callback { // <1>
  private static final String TAG = "Preview";

  SurfaceHolder mHolder;  // <2>
  public Camera camera; // <3>

  Preview(Context context) {
    super(context);

    // Install a SurfaceHolder.Callback so we get notified when the underlying surface is created and destroyed.
    mHolder = getHolder();
    mHolder.addCallback(this);
    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
  }

  public void surfaceCreated(SurfaceHolder holder) {
    // The Surface has been created, acquire the camera and tell it where to rae
	  camera = getCamera();
    try {
      camera.setPreviewDisplay(holder);

      camera.setPreviewCallback(new PreviewCallback() {
        // Called for each frame previewed
        public void onPreviewFrame(byte[] data, Camera camera) { 
          Log.d(TAG, "onPreviewFrame called at: " + System.currentTimeMillis());
          Preview.this.invalidate();  // <12>
        }
      });
    } catch (IOException e) { 
      e.printStackTrace();
    }
  }

  // Called when the holder is destroyed
  public void surfaceDestroyed(SurfaceHolder holder) {  
	  camera.release();
  }

  // Called when holder has changed
  public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) { 
    camera.startPreview();
  }
  
  public Camera getCamera(){
	  return Camera.open();
  }
  

}