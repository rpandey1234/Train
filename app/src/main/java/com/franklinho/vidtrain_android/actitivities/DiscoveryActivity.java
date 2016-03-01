package com.franklinho.vidtrain_android.actitivities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.franklinho.vidtrain_android.R;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DiscoveryActivity extends AppCompatActivity {
    public final String APP_TAG = "VidTrain";
    public String videoFileName = "myvideo.mp4";

    @Bind(R.id.vvPreview) VideoView vvPreview;
    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;

    private static final int VIDEO_CAPTURE = 101;

    Uri videoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);
        ButterKnife.bind(this);
    }

    public void showCreateFlow(View view) {

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            startCameraActivity();
        } else {
            Toast.makeText(this, "No camera on device", Toast.LENGTH_LONG).show();
        }
    }

    public void startCameraActivity() {
//        Intent startCustomCameraIntent = new Intent(this, CameraActivity.class);
//        startActivityForResult(startCustomCameraIntent, REQUEST_CAMERA);

        File mediaFile =
                new File(
                        getExternalFilesDir(Environment.DIRECTORY_MOVIES), APP_TAG+"/"+videoFileName);
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        videoUri = Uri.fromFile(mediaFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
        startActivityForResult(intent, VIDEO_CAPTURE);
    }

    public Uri getVideoFileUri(String fileName) {
        // Only continue if the SD Card is mounted
        if (isExternalStorageAvailable()) {
            // Get safe storage directory for photos
            // Use `getExternalFilesDir` on Context to access package-specific directories.
            // This way, we don't need to request external read/write runtime permissions.
            File mediaStorageDir = new File(
                    getExternalFilesDir(Environment.DIRECTORY_MOVIES), APP_TAG);

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
                Log.d(APP_TAG, "failed to create directory");
            }

            // Return the file target for the photo based on filename
            return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator + fileName));
        }
        return null;
    }

    // Returns true if external storage for photos is available
    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Video has been saved to:\n" + data.getData(), Toast.LENGTH_LONG).show();
                playbackRecordedVideo();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Video recording cancelled.",  Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to record video",  Toast.LENGTH_LONG).show();
            }
        }
    }

    public void playbackRecordedVideo() {
        vvPreview.setVideoURI(videoUri);
        vvPreview.setMediaController(new MediaController(this));
        vvPreview.requestFocus();
        vvPreview.start();
    }

}
