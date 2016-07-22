package com.franklinho.vidtrain_android.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.activities.MainActivity;
import com.franklinho.vidtrain_android.activities.VideoCaptureActivity;
import com.franklinho.vidtrain_android.models.Unseen;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.models.Video;
import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * The fragment shown at the end of the vidtrain.
 */
public class VidtrainLandingFragment extends Fragment {

    @Bind(R.id.tvVideoCount) TextView _tvVideoCount;
    @Bind(R.id.tvTitle) TextView _tvTitle;

    public static final int VIDEO_CAPTURE = 101;

    private ProgressDialog _progress;
    private String _vidtrainId;
    private String _vidtrainTitle;
    private int _videoCount;

    public static Fragment newInstance(VidTrain vidtrain) {
        VidtrainLandingFragment vidtrainLandingFragment = new VidtrainLandingFragment();
        Bundle args = new Bundle();
        args.putString("vidtrainId", vidtrain.getObjectId());
        args.putString("vidtrainTitle", vidtrain.getTitle());
        args.putInt("videoCount", vidtrain.getVideosCount());
        vidtrainLandingFragment.setArguments(args);
        return vidtrainLandingFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            _vidtrainId = arguments.getString("vidtrainId");
            _vidtrainTitle = arguments.getString("vidtrainTitle");
            _videoCount = arguments.getInt("videoCount");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.landing_fragment, container, false);
        ButterKnife.bind(this, v);

        _tvTitle.setText(_vidtrainTitle);
        _tvVideoCount.setText(String.valueOf(_videoCount));
        return v;
    }

    @OnClick(R.id.btnAddVidTrain)
    public void showCreateFlow(View view) {
        // TODO: should we also check for back camera?
        if (getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            Intent intent = new Intent(getContext(), VideoCaptureActivity.class);
            intent.putExtra(MainActivity.UNIQUE_ID_INTENT, Long.toString(System.currentTimeMillis()));
            intent.putExtra(MainActivity.SHOW_CONFIRM, true);
            startActivityForResult(intent, VIDEO_CAPTURE);
        } else {
            Toast.makeText(getContext(), "No camera on device", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(VidtrainApplication.TAG, "in fragment onActivityResult!");
        if (requestCode != VIDEO_CAPTURE) {
            return;
        }
        if (resultCode == Activity.RESULT_OK && data != null) {
            _progress = ProgressDialog
                    .show(getContext(), "Adding your video", "Just a moment please!", true);
            // data.getData().toString() is file://<path>, file is stored at
            // <path> which is /storage/emulated/0/Movies/VidTrainApp/VID_CAPTURED.mp4
            String videoPath = Utility.getOutputMediaFile(
                    data.getStringExtra(MainActivity.UNIQUE_ID_INTENT)).getPath();
            addVideoToVidtrain(videoPath);
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getContext(), "Video recording cancelled.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "Failed to record video", Toast.LENGTH_LONG).show();
        }
    }

    private void addVideoToVidtrain(final String videoPath) {
        ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.whereEqualTo("objectId", _vidtrainId);
        query.include("user");
        query.include("videos.user");
        query.include("collaborators");
        query.getFirstInBackground(new GetCallback<VidTrain>() {
            @Override
            public void done(final VidTrain vidtrain, ParseException e) {
                if (e != null) {
                    Toast.makeText(getContext(), "This Vidtrain is invalid", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                // Need to re-fetch vidtrain since it becomes null on some phones/OS
                final int videosCount = vidtrain.getVideosCount();
                _tvVideoCount.setText(String.valueOf(videosCount + 1));
                final Video video = new Video();
                final User user = User.getCurrentUser();
                final ParseFile parseFile = Utility.createParseFile(videoPath);
                if (parseFile == null) {
                    return;
                }
                Bitmap thumbnailBitmap = Utility.getImageBitmap(videoPath);
                final ParseFile parseThumbnail = Utility.createParseFileFromBitmap(thumbnailBitmap);
                parseFile.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        video.setUser(user);
                        video.setVideoFile(parseFile);
                        video.setVidTrain(vidtrain);
                        video.setThumbnail(parseThumbnail);
                        video.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                vidtrain.setVideos(vidtrain.maybeInitAndAdd(video));
                                vidtrain.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        _progress.dismiss();
                                        _tvVideoCount.setText(
                                                String.valueOf(vidtrain.getVideosCount()));
                                        Utility.sendNotification(vidtrain);
                                        Unseen.addUnseen(vidtrain);
                                        assert user != null;
                                        user.put("vidtrains", user.maybeInitAndAdd(vidtrain));
                                        user.put("videos", user.maybeInitAndAdd(video));
                                        user.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                Toast.makeText(getContext(),
                                                        "Successfully added video",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }
}
