package com.franklinho.vidtrain_android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;

import com.desmond.squarecamera.ImageParameters;
import com.desmond.squarecamera.ResizeAnimation;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.models.DynamicHeightImageView;
import com.franklinho.vidtrain_android.models.DynamicVideoView;
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
import butterknife.OnClick;

public class VideoCaptureActivity extends Activity implements MediaRecorder.OnInfoListener {

    @Bind(R.id.camera_preview) RelativeLayout _preview;
    @Bind(R.id.button_capture) FloatingActionButton _captureButton;
    @Bind(R.id.timer) View _timerView;
    @Bind(R.id.vTop) View _vTop;
    @Bind(R.id.vBottom) View _vBottom;

    public static final int MAX_TIME = 5000;
    public static final int UPDATE_FREQUENCY = 50;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private static boolean showConfirm;
    private static String uniqueId;
    private static Camera mCamera = null;
    public static int orientation;

    private ImageParameters _imageParameters;
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
            // Do something here on the main thread
            // Repeat this the same runnable code block again another 2 seconds
            _handler.postDelayed(_runnableCode, UPDATE_FREQUENCY);
            Display display = getWindowManager().getDefaultDisplay();
            int width = display.getWidth();
            double fraction = UPDATE_FREQUENCY / (float) MAX_TIME;
            // TODO(rahul): adding 2 pixels is a hack to ensure we get to the end of the screen
            // due to issues with rounding
            int widthToAdd = (int) (fraction * width) + 2;
            int resultWidth = _timerView.getWidth() + widthToAdd;
            LayoutParams layoutParams = _timerView.getLayoutParams();
            layoutParams.width = resultWidth;
            _timerView.setLayoutParams(layoutParams);
        }
    };

    @OnClick(R.id.button_change_camera)
    public void switchCamera(View view) {
        Log.d(VidtrainApplication.TAG, "switch camera clicked!");
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.video_capture);
        ButterKnife.bind(this);
        uniqueId = getIntent().getStringExtra(MainActivity.UNIQUE_ID_INTENT);
        showConfirm = getIntent().getBooleanExtra(MainActivity.SHOW_CONFIRM, false);
        Log.d(VidtrainApplication.TAG, "show confirm? " + showConfirm);
        Log.d(VidtrainApplication.TAG, "uniqueId: " + uniqueId);
        initializeCamera();
    }


    protected void initializeCamera(){
        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        _cameraPreview = new CameraPreview(this, mCamera);
        _preview.addView(_cameraPreview);

        _imageParameters = new ImageParameters();
        relayoutCovers();

        _captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
                                //setCaptureButtonText("Stop");
                                _isRecording = true;

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
        _mediaRecorder.stop();  // stop the recording
        releaseMediaRecorder(); // release the MediaRecorder object
        mCamera.lock();         // take camera access back from MediaRecorder

        // inform the user that recording has stopped
        //setCaptureButtonText("Capture");
        _isRecording = false;
        _handler.removeCallbacks(_runnableCode);
        if (showConfirm) {
            final String videoPath = Utility.getOutputMediaFile(uniqueId).getPath();
            View itemView = getLayoutInflater().inflate(R.layout.image_video_overlay, null);
            itemView.setPadding(itemView.getPaddingLeft(), itemView.getPaddingTop() + 10,
                    itemView.getPaddingRight(), itemView.getPaddingBottom());
            final DynamicVideoView vvPreview = (DynamicVideoView) itemView.findViewById(R.id.vvPreview);
            vvPreview.setHeightRatio(1);
            final DynamicHeightImageView ivThumbnail = (DynamicHeightImageView) itemView.findViewById(R.id.ivThumbnail);
            ivThumbnail.setHeightRatio(1);
            ivThumbnail.setImageBitmap(Utility.getImageBitmap(videoPath));
            vvPreview.setVideoPath(videoPath);
            vvPreview.setVisibility(View.GONE);
            vvPreview.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    vvPreview.setVisibility(View.GONE);
                    ivThumbnail.setVisibility(View.VISIBLE);
                }
            });
            ivThumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ivThumbnail.setVisibility(View.GONE);
                    vvPreview.setVisibility(View.VISIBLE);
                    vvPreview.setZOrderOnTop(true);
                    vvPreview.requestFocus();
                    vvPreview.start();
                }
            });
            AlertDialog.Builder builder = new Builder(this)
                    .setTitle("Add to Vidtrain?")
                    .setView(itemView)
                    .setPositiveButton("Yes", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(VidtrainApplication.TAG, "yes clicked!!");
                            Intent dataBack = new Intent();
                            dataBack.putExtra(MainActivity.UNIQUE_ID_INTENT, uniqueId);
                            setResult(Activity.RESULT_OK, dataBack);
                            finish();
                        }
                    })
                    .setNegativeButton("Cancel", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(VidtrainApplication.TAG, "Cancel clicked!!");
                            setResult(Activity.RESULT_CANCELED);
                            finish();
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            Intent dataBack = new Intent();
            dataBack.putExtra(MainActivity.UNIQUE_ID_INTENT, uniqueId);
            setResult(Activity.RESULT_OK, dataBack);
            finish();
        }
    }

    private boolean checkCameraHardware(Context context) {
        // this device has a camera or not
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
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

        final List<Size> supportedVideoSizes = mCamera.getParameters().getSupportedVideoSizes();
        //Size smallestSize = getSmallestSize(supportedVideoSizes);

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        //mCamera.setDisplayOrientation(90);
        _mediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        _mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        _mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        _mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));

        // Step 4: Set output file
        _mediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        // Step 5: Set the preview output
        _mediaRecorder.setPreviewDisplay(_cameraPreview.getHolder().getSurface());
        _mediaRecorder.setOrientationHint(VideoCaptureActivity.orientation);
//        _mediaRecorder.setVideoFrameRate(24);
        _mediaRecorder.setVideoEncodingBitRate(5000000);
//        _mediaRecorder.setVideoSize(smallestSize.width, smallestSize.height);
        _mediaRecorder.setMaxFileSize(8000000); // max parse file size is 10485760 bytes
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

    private Size getSmallestSize(List<Size> supportedVideoSizes) {
        Log.d(VidtrainApplication.TAG, "Num supported sizes: " + supportedVideoSizes.size());
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
        Size smallestSize = supportedVideoSizes.get(0);
        Log.d(VidtrainApplication.TAG,
                "smallest video size: " + smallestSize.width + " " + smallestSize.height);
        Log.d(VidtrainApplication.TAG,
                "2nd smallest video size: " + supportedVideoSizes.get(1).width + " "
                        + supportedVideoSizes.get(1).height);
        return smallestSize;
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

    private static File getOutputMediaFile(int type) {
        return Utility.getOutputMediaFile(uniqueId);
    }

    private void releaseCameraAndPreview() {
        //_cameraPreview.setCamera(null);
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
            // Hack so that the resulting video is straight.
            result += 180 % 360;
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
        _handler.removeCallbacks(_runnableCode);
    }

   void relayoutCovers() {
       _imageParameters.mIsPortrait =
               getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;


       ViewTreeObserver observer = _preview.getViewTreeObserver();
       observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
           @Override
           public void onGlobalLayout() {
               _imageParameters.mPreviewWidth = _preview.getWidth();
               _imageParameters.mPreviewHeight = _preview.getHeight();

               _imageParameters.mCoverWidth = _imageParameters.mCoverHeight
                       = _imageParameters.calculateCoverWidthHeight();

//                    Log.d(TAG, "parameters: " + _imageParameters.getStringValues());
//                    Log.d(TAG, "cover height " + topCoverView.getHeight());
               resizeTopAndBtmCover(_vTop, _vBottom);

               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                   _preview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
               } else {
                   _preview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
               }
           }
       });
   }

    private void resizeTopAndBtmCover( final View topCover, final View bottomCover) {
        ResizeAnimation resizeTopAnimation
                = new ResizeAnimation(topCover, _imageParameters);
        resizeTopAnimation.setDuration(800);
        resizeTopAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        topCover.startAnimation(resizeTopAnimation);

        ResizeAnimation resizeBtmAnimation
                = new ResizeAnimation(bottomCover, _imageParameters);
        resizeBtmAnimation.setDuration(800);
        resizeBtmAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        bottomCover.startAnimation(resizeBtmAnimation);
    }
}

