package com.franklinho.vidtrain_android.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.activities.MainActivity;
import com.franklinho.vidtrain_android.activities.VideoCaptureActivity;
import com.franklinho.vidtrain_android.models.Unseen;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.models.Video;
import com.franklinho.vidtrain_android.ui.ImageAttribution;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * The fragment shown at the end of the vidtrain.
 */
public class VidtrainLandingFragment extends Fragment {

    @Bind(R.id.tvVideoCount) TextView _tvVideoCount;
    @Bind(R.id.tvTitle) TextView _tvTitle;
    @Bind(R.id.imageAttribution1) ImageAttribution _imageAttribution1;
    @Bind(R.id.imageAttribution2) ImageAttribution _imageAttribution2;
    @Bind(R.id.imageAttribution3) ImageAttribution _imageAttribution3;

    public static final int VIDEO_CAPTURE = 101;
    public static final int MAX_THUMBNAILS = 3;
    public static final String VIDTRAIN_ID = "VIDTRAIN_ID";
    public static final String VIDTRAIN_TITLE = "VIDTRAIN_TITLE";
    public static final String VIDEO_COUNT = "VIDEO_COUNT";
    public static final String THUMBNAILS = "THUMBNAILS";
    public static final String USER_URLS = "USER_URLS";

    private ProgressDialog _progress;
    private String _vidtrainId;
    private String _vidtrainTitle;
    private int _videoCount;
    private ArrayList<String> _thumbnails;
    private ArrayList<String> _userUrls;
    private String _videoPath;

    public static Fragment newInstance(VidTrain vidtrain) {
        VidtrainLandingFragment vidtrainLandingFragment = new VidtrainLandingFragment();
        Bundle args = new Bundle();
        args.putString(VIDTRAIN_ID, vidtrain.getObjectId());
        args.putString(VIDTRAIN_TITLE, vidtrain.getTitle());
        int videosCount = vidtrain.getVideosCount();
        args.putInt(VIDEO_COUNT, videosCount);
        ArrayList<String> thumbnails = new ArrayList<>();
        ArrayList<String> userUrls = new ArrayList<>();
        int numShown = Math.min(MAX_THUMBNAILS, videosCount);
        for (int i = 0; i < numShown; i++) {
            Video video = vidtrain.getVideos().get(videosCount - i - 1);
            thumbnails.add(video.getThumbnail().getUrl());
            userUrls.add(video.getUser().getProfileImageUrl());
        }
        args.putStringArrayList(THUMBNAILS, thumbnails);
        args.putStringArrayList(USER_URLS, userUrls);
        vidtrainLandingFragment.setArguments(args);
        return vidtrainLandingFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            _vidtrainId = arguments.getString(VIDTRAIN_ID);
            _vidtrainTitle = arguments.getString(VIDTRAIN_TITLE);
            _videoCount = arguments.getInt(VIDEO_COUNT);
            _thumbnails = arguments.getStringArrayList(THUMBNAILS);
            _userUrls = arguments.getStringArrayList(USER_URLS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.landing_fragment, container, false);
        ButterKnife.bind(this, v);

        Context context = getContext();
        _tvTitle.setText(_vidtrainTitle);
        _tvVideoCount.setText(String.valueOf(_videoCount));
        _imageAttribution1.bind(_thumbnails.get(0), _userUrls.get(0));
        if (_thumbnails.size() > 1) {
            _imageAttribution2.bind(_thumbnails.get(1), _userUrls.get(1));
        }
        if (_thumbnails.size() > 2) {
            _imageAttribution3.bind(_thumbnails.get(2), _userUrls.get(2));
        }
        return v;
    }

    @OnClick(R.id.btnAddVidTrain)
    public void showCreateFlow(View view) {
        if (getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Intent intent = new Intent(getContext(), VideoCaptureActivity.class);
            intent.putExtra(MainActivity.UNIQUE_ID_INTENT,
                    Long.toString(System.currentTimeMillis()));
            startActivityForResult(intent, VIDEO_CAPTURE);
        } else {
            Toast.makeText(getContext(), "No camera on device", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != VIDEO_CAPTURE) {
            return;
        }
        if (resultCode == Activity.RESULT_OK && data != null) {
            _progress = ProgressDialog
                    .show(getContext(), "Adding your video", "Just a moment please!", true);
            // data.getData().toString() is file://<path>, file is stored at
            // <path> which is /storage/emulated/0/Movies/VidTrainApp/VID_CAPTURED.mp4
            _videoPath = Utility.getOutputMediaFile(
                    data.getStringExtra(MainActivity.UNIQUE_ID_INTENT)).getPath();
            addVideoToVidtrain();
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getContext(), "Video recording cancelled.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "Failed to record video", Toast.LENGTH_LONG).show();
        }
    }

    private void addVideoToVidtrain() {
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
                final ParseFile parseFile = Utility.createParseFile(_videoPath);
                if (parseFile == null) {
                    return;
                }
                Bitmap thumbnailBitmap = Utility.getImageBitmap(_videoPath);
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
                                                getActivity().onBackPressed();
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

    @Override
    public void onStop() {
        Utility.deleteFile(_videoPath);
        super.onStop();
    }
}
