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
import android.widget.TextView;
import android.widget.Toast;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.activities.MainActivity;
import com.franklinho.vidtrain_android.activities.VideoCaptureActivity;
import com.franklinho.vidtrain_android.models.Unseen;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.models.Video;
import com.franklinho.vidtrain_android.models.VideoModel;
import com.franklinho.vidtrain_android.models.VidtrainModel;
import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.franklinho.vidtrain_android.ui.VideoPreview;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * The fragment shown at the end of the vidtrain.
 */
public class VidtrainLandingFragment extends Fragment {

    @Bind(R.id.tvVideoCount) TextView _tvVideoCount;
    @Bind(R.id.tvTitle) TextView _tvTitle;
    @Bind(R.id.imageAttribution1) VideoPreview _videoPreview1;
    @Bind(R.id.imageAttribution2) VideoPreview _videoPreview2;
    @Bind(R.id.imageAttribution3) VideoPreview _videoPreview3;

    public static final int VIDEO_CAPTURE = 101;
    public static final int MAX_VIDEOS_SHOWN = 3;
    public static final String VIDTRAIN_MODEL_KEY = "VIDTRAIN_MODEL_KEY";

    private ProgressDialog _progress;
    private String _videoPath;
    private VidtrainModel _vidtrainModel;

    public static Fragment newInstance(VidTrain vidtrain) {
        VidtrainLandingFragment vidtrainLandingFragment = new VidtrainLandingFragment();
        Bundle args = new Bundle();
        args.putParcelable(VIDTRAIN_MODEL_KEY, new VidtrainModel(vidtrain, MAX_VIDEOS_SHOWN));
        vidtrainLandingFragment.setArguments(args);
        return vidtrainLandingFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        _vidtrainModel = arguments.getParcelable(VIDTRAIN_MODEL_KEY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.landing_fragment, container, false);
        ButterKnife.bind(this, v);
        _tvTitle.setText(_vidtrainModel.getTitle());
        _tvVideoCount.setText(String.valueOf(_vidtrainModel.getVideoCount()));
        final List<VideoModel> videosShown = _vidtrainModel.getVideoModelsToShow();
        _videoPreview1.bind(videosShown.get(0));
        if (videosShown.size() > 1) {
            _videoPreview2.bind(videosShown.get(1));
        }
        if (videosShown.size() > 2) {
            _videoPreview3.bind(videosShown.get(2));
        }
        ParseQuery<Unseen> query = ParseQuery.getQuery("Unseen");
        // need to wrap in vidtrain object because pointer field needs a pointer value
        VidTrain vidtrain = new VidTrain();
        vidtrain.setObjectId(_vidtrainModel.getId());
        query.whereEqualTo(Unseen.VIDTRAIN_KEY, vidtrain);
        query.include(Unseen.USER_KEY);
        query.include(Unseen.VIDEOS_KEY);
        query.findInBackground(new FindCallback<Unseen>() {
            @Override
            public void done(List<Unseen> unseens, ParseException e) {
                if (e != null) {
                    Log.d(VidtrainApplication.TAG, "Could not get unseen data: " + e.toString());
                    return;
                }
                // map video id to list of users who have NOT seen this video as first
                Map<String, List<User>> firstUnseenMap = new HashMap<>();
                List<User> usersAllSeen = new ArrayList<>();
                List<User> usersNoneSeen = new ArrayList<>();
                for (int i = 0; i < unseens.size(); i++) {
                    Unseen unseen = unseens.get(i);
                    User user = unseen.getUser();
                    List<Video> unseenVideos = unseen.getUnseenVideos();
                    if (unseenVideos.isEmpty()) {
                        usersAllSeen.add(user);
                    } else {
                        Video firstUnseen = unseenVideos.get(0);
                        if (!_vidtrainModel.containsVideo(firstUnseen.getObjectId())) {
                            usersNoneSeen.add(user);
                        }
                        List<User> users;
                        if (firstUnseenMap.containsKey(firstUnseen.getObjectId())) {
                            users = firstUnseenMap.get(firstUnseen.getObjectId());
                        } else {
                            users = new ArrayList<>();
                        }
                        users.add(user);
                        firstUnseenMap.put(firstUnseen.getObjectId(), users);
                    }
                }
                _videoPreview1.showSeenUsers(usersAllSeen);
                if (firstUnseenMap.containsKey(_vidtrainModel.getVideoIdRecent(0))) {
                    _videoPreview1.showUnseenUsers(firstUnseenMap.get(_vidtrainModel.getVideoIdRecent(0)));
                }
                if (videosShown.size() > 1 && firstUnseenMap.containsKey(_vidtrainModel.getVideoIdRecent(1))) {
                    _videoPreview2.showUnseenUsers(firstUnseenMap.get(_vidtrainModel.getVideoIdRecent(1)));
                }
                if (videosShown.size() > 2) {
                    if (firstUnseenMap.containsKey(_vidtrainModel.getVideoIdRecent(2))) {
                        usersNoneSeen.addAll(firstUnseenMap.get(_vidtrainModel.getVideoIdRecent(2)));
                    }
                    _videoPreview3.showUnseenUsers(usersNoneSeen);
                }
            }
        });
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
        query.whereEqualTo("objectId", _vidtrainModel.getId());
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
