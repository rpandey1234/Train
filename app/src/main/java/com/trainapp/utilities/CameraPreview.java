package com.trainapp.utilities;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.trainapp.networking.VidtrainApplication;

import java.io.IOException;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder _holder;
    private int _cameraId;
    private Camera _camera;
    private float _dist;

    public CameraPreview(Context context, int cameraId, Camera camera) {
        super(context);
        _cameraId = cameraId;
        _camera = camera;
        _camera.setDisplayOrientation(90);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        _holder = getHolder();
        _holder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        _holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            if(_camera != null) {
                _camera.setDisplayOrientation(90);
                _camera.setPreviewDisplay(holder);
                _camera.startPreview();
            }
        } catch (IOException e) {
            Log.d(VidtrainApplication.TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    /**
     * Adapted from http://stackoverflow.com/questions/18594602/
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Get the pointer ID
        Camera.Parameters params = _camera.getParameters();
        int action = event.getAction();
        
        if (event.getPointerCount() > 1) {
            // handle multi-touch events
            if (action == MotionEvent.ACTION_POINTER_DOWN) {
                _dist = getFingerSpacing(event);
            } else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported()) {
                _camera.cancelAutoFocus();
                handleZoom(event, params);
            }
        } else {
            // handle single touch events
            if (action == MotionEvent.ACTION_UP) {
                handleFocus(event, params);
            }
        }
        return true;
    }

    private void handleZoom(MotionEvent event, Camera.Parameters params) {
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        float newDist = getFingerSpacing(event);
        if (newDist > _dist) {
            //zoom in
            if (zoom < maxZoom)
                zoom++;
        } else if (newDist < _dist) {
            //zoom out
            if (zoom > 0)
                zoom--;
        }
        _dist = newDist;
        params.setZoom(zoom);
        _camera.setParameters(params);
    }

    public void handleFocus(MotionEvent event, Camera.Parameters params) {
        int pointerId = event.getPointerId(0);
        int pointerIndex = event.findPointerIndex(pointerId);
        // Get the pointer's current position
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            _camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
                    // currently set to auto-focus on single touch
                }
            });
        }
    }

    /**
     * Determine the space between the first two fingers
     **/
    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (_holder.getSurface() == null) {
            return;
        }
        // stop preview before making changes
        try {
            _camera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        if (_cameraId == CameraInfo.CAMERA_FACING_BACK) {
            _cameraId = CameraInfo.CAMERA_FACING_FRONT;
        } else {
            _cameraId = CameraInfo.CAMERA_FACING_BACK;
        }
        // set preview size and make any resize, rotate or reformatting changes here
        Utility.setCameraDisplayOrientation(
                ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)),
                _cameraId,
                _camera);

        // start preview with new settings
        try {
            _camera.setDisplayOrientation(90);
            // TODO(rahul): this is a major hack for Nexus 5x devices where camera sOrientation
            // is weird. Should be using Camera2 API.
            if (_cameraId == CameraInfo.CAMERA_FACING_FRONT && Build.MODEL.equals("Nexus 5X")) {
                _camera.setDisplayOrientation(270);
            }
            _camera.setPreviewDisplay(_holder);
            _camera.startPreview();
        } catch (Exception e) {
            Log.d(VidtrainApplication.TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    public Camera getCamera() {
        return _camera;
    }
}
