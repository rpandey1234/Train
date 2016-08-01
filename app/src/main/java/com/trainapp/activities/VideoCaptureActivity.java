package com.trainapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.VideoView;

import com.trainapp.R;
import com.trainapp.networking.VidtrainApplication;
import com.trainapp.utilities.CameraPreview;
import com.trainapp.utilities.Utility;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VideoCaptureActivity extends Activity implements MediaRecorder.OnInfoListener {

    @Bind(R.id.camera_preview) LinearLayout _preview;
    @Bind(R.id.button_capture) FloatingActionButton _captureButton;
    @Bind(R.id.button_change_camera) ImageButton _btnChangeCamera;
    @Bind(R.id.button_send) ImageButton _btnSend;
    @Bind(R.id.videoView) VideoView _videoView;
    @Bind(R.id.timer) View _timerView;

    public static final int MAX_TIME = 7000;
    public static final int UPDATE_FREQUENCY = 50;

    private static String uniqueId;
    private static Camera mCamera = null;
    public static int orientation;

    private CameraPreview _cameraPreview;
    private MediaRecorder _mediaRecorder;
    private boolean _isRecording = false;
    private int _cameraId = CameraInfo.CAMERA_FACING_BACK;
    // Create the Handler object (on the main thread by default)
    private Handler _handler = new Handler();
    // Define the code block to be executed
    final Runnable _runnableCode = new Runnable() {
        @Override
        public void run() {
            _handler.postDelayed(_runnableCode, UPDATE_FREQUENCY);
            Display display = getWindowManager().getDefaultDisplay();
            int width = display.getWidth();
            double fraction = UPDATE_FREQUENCY / (float) MAX_TIME;
            int widthToAdd = (int) (fraction * width);
            int resultWidth = _timerView.getWidth() + widthToAdd;
            LayoutParams layoutParams = _timerView.getLayoutParams();
            layoutParams.width = resultWidth;
            _timerView.setLayoutParams(layoutParams);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.video_capture);
        ButterKnife.bind(this);
        uniqueId = getIntent().getStringExtra(MainActivity.UNIQUE_ID_INTENT);
        Log.d(VidtrainApplication.TAG, "uniqueId: " + uniqueId);
        initializeCamera();
    }

    @OnClick(R.id.button_change_camera)
    public void switchCamera(View view) {
        releaseCameraAndPreview();
        if (_cameraId == CameraInfo.CAMERA_FACING_BACK) {
            _cameraId = CameraInfo.CAMERA_FACING_FRONT;
        } else {
            _cameraId = CameraInfo.CAMERA_FACING_BACK;
        }
        mCamera = Camera.open(_cameraId);
        _cameraPreview = new CameraPreview(this, mCamera);
        _preview.removeAllViews();
        _preview.addView(_cameraPreview);
        setCameraDisplayOrientation(this, _cameraId, mCamera);
    }

    @OnClick(R.id.button_capture)
    public void toggleRecording(View view) {
        if (_isRecording) {
            finishRecording();
        } else {
            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                _mediaRecorder.start();
                _captureButton.setImageDrawable(getResources().getDrawable(
                        R.drawable.icon_square_white));
                // Start the initial runnable task by posting through the handler
                _handler.post(_runnableCode);
                // inform the user that recording has started
                _isRecording = true;
                _btnChangeCamera.setVisibility(View.GONE);

            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                // inform user
            }
        }
    }

    @OnClick(R.id.button_send)
    public void proceedCreationFlow(View view) {
        Intent dataBack = new Intent();
        dataBack.putExtra(MainActivity.UNIQUE_ID_INTENT, uniqueId);
        setResult(Activity.RESULT_OK, dataBack);
        finish();
    }

    protected void initializeCamera() {
        // Create an instance of Camera
        mCamera = getCameraInstance();
        // Create our Preview view and set it as the content of our activity.
        _cameraPreview = new CameraPreview(this, mCamera);
        _preview.addView(_cameraPreview);
    }

    private void finishRecording() {
        // stop recording and release camera
        _mediaRecorder.stop();  // stop the recording
        releaseMediaRecorder(); // release the MediaRecorder object
        mCamera.lock();         // take camera access back from MediaRecorder

        // inform the user that recording has stopped
        _isRecording = false;
        _handler.removeCallbacks(_runnableCode);
        // hide the UI elements
        _captureButton.setVisibility(View.GONE);
        _timerView.setVisibility(View.GONE);
        _btnChangeCamera.setVisibility(View.GONE);
        _preview.removeAllViews();
        // play (and repeat) the video
        _videoView.setVideoPath(Utility.getOutputMediaFile(uniqueId).getPath());
        _videoView.start();
        _videoView.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                _videoView.start();
            }
        });
        _videoView.setVisibility(View.VISIBLE);
        // Show the "send" button
        // Logo credit: http://www.flaticon.com/free-icon/send-button_60525
        _btnSend.setVisibility(View.VISIBLE);
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Log.e(VidtrainApplication.TAG, e.toString());
        }
        return c; // returns null if camera is unavailable
    }

    private boolean prepareVideoRecorder() {
        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(_cameraId);
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        setCameraDisplayOrientation(this, _cameraId, mCamera);
        if (_cameraId == CameraInfo.CAMERA_FACING_FRONT) {
            mCamera.setDisplayOrientation(90);
        }
        _mediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        //mCamera.setDisplayOrientation(90);
        _mediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        _mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        _mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        _mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));

        // Step 4: Set output file
        _mediaRecorder.setOutputFile(Utility.getOutputMediaFile(uniqueId).toString());

        // Step 5: Set the preview output
        _mediaRecorder.setPreviewDisplay(_cameraPreview.getHolder().getSurface());
        _mediaRecorder.setOrientationHint(VideoCaptureActivity.orientation);
        // Only bitrate can reduce file size (not frame rate)
        _mediaRecorder.setVideoEncodingBitRate(500000);
        // Max parse file size is 10485760 bytes (~10MB)
        _mediaRecorder.setMaxFileSize(4000000);
        _mediaRecorder.setMaxDuration(MAX_TIME);
        _mediaRecorder.setOnInfoListener(this);
        // Step 6: Prepare configured MediaRecorder
        try {
            _mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("Issue", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d("Issue 2", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCamera == null){
            initializeCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseMediaRecorder() {
        if (_mediaRecorder != null) {
            _mediaRecorder.reset();   // clear recorder configuration
            _mediaRecorder.release(); // release the recorder object
            _mediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
            _cameraPreview.getHolder().removeCallback(_cameraPreview);
        }
    }

    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    public static void setCameraDisplayOrientation(
            Activity activity, int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
            // Hack so that the resulting video is straight.
            result += 180 % 360;
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        VideoCaptureActivity.orientation = result;
        camera.setDisplayOrientation(result);
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            Log.v(VidtrainApplication.TAG, "Maximum Duration Reached");
            finishRecording();
        } else if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
            Log.v(VidtrainApplication.TAG, "Maximum size reached");
            finishRecording();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        _handler.removeCallbacks(_runnableCode);
    }
}
