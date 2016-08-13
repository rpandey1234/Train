package com.trainapp.utilities;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.trainapp.networking.VidtrainApplication;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder _holder;
    private int _cameraId;
    private Camera _camera;

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
            // TODO(rahul): this is a major hack for Nexus 5x devices where camera orientation
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
}
