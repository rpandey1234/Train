package com.franklinho.vidtrain_android.activities;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.franklinho.vidtrain_android.utilities.CameraPreview;
import com.franklinho.vidtrain_android.utilities.Utility;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class VideoCaptureActivity extends Activity implements MediaRecorder.OnInfoListener {

    private static String uniqueId;
    @Bind(R.id.camera_preview) RelativeLayout preview;
    @Bind(R.id.button_capture) ImageButton captureButton;
    @Bind(R.id.timer) View timerView;
//    @Bind(R.id.button_ChangeCamera) ImageButton switchCamera;

    public static final int MAX_TIME = 5000;
    public static final int UPDATE_FREQUENCY = 50;
    private static Camera mCamera = null;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static int orientation;
    private int camId = CameraInfo.CAMERA_FACING_BACK;
    // Create the Handler object (on the main thread by default)
    private Handler handler = new Handler();
    // Define the code block to be executed
    final Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            // Do something here on the main thread
            // Repeat this the same runnable code block again another 2 seconds
            handler.postDelayed(runnableCode, UPDATE_FREQUENCY);
            Display display = getWindowManager().getDefaultDisplay();
            int width = display.getWidth();
            double fraction = UPDATE_FREQUENCY / (float) MAX_TIME;
            // TODO(rahul): adding 2 pixels is a hack to ensure we get to the end of the screen
            // due to issues with rounding
            int widthToAdd = (int) (fraction * width) + 2;
            int resultWidth = timerView.getWidth() + widthToAdd;
            LayoutParams layoutParams = timerView.getLayoutParams();
            layoutParams.width = resultWidth;
            timerView.setLayoutParams(layoutParams);
        }
    };

//    @OnClick(R.id.button_ChangeCamera)
//    public void switchCamera(View view) {
//        Log.d(VidtrainApplication.TAG, "switch camer clicked!");
//        camId = CameraInfo.CAMERA_FACING_FRONT;
//        prepareVideoRecorder();
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_capture);
        ButterKnife.bind(this);
        uniqueId = getIntent().getStringExtra(HomeActivity.UNIQUE_ID_INTENT);
        Log.d(VidtrainApplication.TAG, "VideoCaptureActivity: " + uniqueId);
        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        preview.addView(mPreview);

        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isRecording) {
                            finishRecording();
                        } else {
                            // initialize video camera
                            if (prepareVideoRecorder()) {
                                // Camera is available and unlocked, MediaRecorder is prepared,
                                // now you can start recording
                                //mCamera.setDisplayOrientation(90);
                                mMediaRecorder.start();
                                captureButton.setImageDrawable(getResources().getDrawable(
                                        R.drawable.ic_stop_black_24dp));
                                // Start the initial runnable task by posting through the handler
                                handler.post(runnableCode);
                                Log.i("Hi", "Hello");
                                // inform the user that recording has started
                                //setCaptureButtonText("Stop");
                                isRecording = true;

                            } else {
                                // prepare didn't work, release the camera
                                releaseMediaRecorder();
                                // inform user
                            }
                        }
                    }
                }
        );
    }

    private void finishRecording() {
        // stop recording and release camera
        mMediaRecorder.stop();  // stop the recording
        releaseMediaRecorder(); // release the MediaRecorder object
        mCamera.lock();         // take camera access back from MediaRecorder

        // inform the user that recording has stopped
        //setCaptureButtonText("Capture");
        isRecording = false;
        handler.removeCallbacks(runnableCode);
        setResult(Activity.RESULT_OK);
        finish();
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
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
        }
        return c; // returns null if camera is unavailable
    }

    private boolean prepareVideoRecorder() {
        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(camId);
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        setCameraDisplayOrientation(this, camId, mCamera);
        //mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        mMediaRecorder = new MediaRecorder();

        final List<Size> supportedVideoSizes = mCamera.getParameters().getSupportedVideoSizes();
//        mCamera.getParameters().
        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        //mCamera.setDisplayOrientation(90);
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
        mMediaRecorder.setOrientationHint(VideoCaptureActivity.orientation);
        /////////
        mMediaRecorder.setVideoFrameRate(16);
        mMediaRecorder.setVideoEncodingBitRate(1000000);
//        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        Log.d(VidtrainApplication.TAG, "" + supportedVideoSizes.size());
        Collections.sort(supportedVideoSizes, new Comparator<Size>() {
            @Override
            public int compare(Size lhs, Size rhs) {
                double avg1 = (lhs.height + lhs.width) / 2.0;
                double avg2 = (rhs.height + rhs.width) / 2.0;
                if (avg1 < avg2) {
                    return -1;
                } else if (avg1 > avg2) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        Size lowestSize = supportedVideoSizes.get(0);
        Log.d(VidtrainApplication.TAG,
                "smallest video size: " + lowestSize.width + " " + lowestSize.height);
        Log.d(VidtrainApplication.TAG,
                "2nd smallest video size: " + supportedVideoSizes.get(1).width + " "
                        + supportedVideoSizes.get(1).height);
        mMediaRecorder.setVideoSize(lowestSize.width, lowestSize.height);
        // max parse file size is 10485760 bytes
        mMediaRecorder.setMaxFileSize(2000000);

//        Parameters params = new Parameters(mCamera.getParameters());
//        mCamera.getParameters().setPictureSize();
//        mCamera.setParameters(params);
        ////
        mMediaRecorder.setMaxDuration(MAX_TIME);
        mMediaRecorder.setOnInfoListener(this);
        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
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
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    private static File getOutputMediaFile(int type) {
        return Utility.getOutputMediaFile(uniqueId);
    }
    /**
     * Create a File for saving an image or video
     */
//    private static File getOutputMediaFile(int type) {
//        // To be safe, you should check that the SDCard is mounted
//        // using Environment.getExternalStorageState() before doing this.
//
//        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES), "VidTrain");
//        // This location works best if you want the created images to be shared
//        // between applications and persist after your app has been uninstalled.
//
//        // Create the storage directory if it does not exist
//        if (!mediaStorageDir.exists()) {
//            if (!mediaStorageDir.mkdirs()) {
//                Log.d("MyCameraApp", "failed to create directory");
//                return null;
//            }
//        }
//
//        // Create a media file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        File mediaFile;
//        if (type == MEDIA_TYPE_IMAGE) {
//            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
//                    "IMG_" + timeStamp + ".jpg");
//        } else if (type == MEDIA_TYPE_VIDEO) {
//            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
//                    "VID_" + timeStamp + ".mp4");
//        } else {
//            return null;
//        }
//
//        return mediaFile;
//    }

    private void releaseCameraAndPreview() {
        //mPreview.setCamera(null);
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {

        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();

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
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        VideoCaptureActivity.orientation=result;
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
        handler.removeCallbacks(runnableCode);
    }
}

