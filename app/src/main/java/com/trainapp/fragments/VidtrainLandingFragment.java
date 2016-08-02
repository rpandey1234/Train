package com.trainapp.fragments;

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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trainapp.R;
import com.trainapp.activities.MainActivity;
import com.trainapp.activities.VideoCaptureActivity;
import com.trainapp.models.Unseen;
import com.trainapp.models.User;
import com.trainapp.models.VidTrain;
import com.trainapp.models.Video;
import com.trainapp.models.VideoModel;
import com.trainapp.models.VidtrainModel;
import com.trainapp.networking.VidtrainApplication;
import com.trainapp.ui.VideoPreview;
import com.trainapp.utilities.Utility;
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
    @Bind(R.id.previews) LinearLayout _previews;

    public static final int VIDEO_CAPTURE = 101;
    public static final int MAX_VIDEOS_SHOWN = 100;
    private static final String USERS_ALL_SEEN = "-1";
    private static final String USERS_NONE_SEEN = "-2";
    public static final String VIDTRAIN_MODEL_KEY = "VIDTRAIN_MODEL_KEY";

    private ProgressDialog _progress;
    private String _videoPath;
    private VidtrainModel _vidtrainModel;
    private List<VideoPreview> _videoPreviews;

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
        _videoPreviews = new ArrayList<>();
        String currentUserId = User.getCurrentUser().getObjectId();
        for (VideoModel video : _vidtrainModel.getVideoModelsToShow()) {
            VideoPreview videoPreview = new VideoPreview(getContext());
            videoPreview.setFromCurrentUser(currentUserId.equals(video.getUserId()));
            _videoPreviews.add(videoPreview);
        }
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
        for (int i = 0; i < videosShown.size(); i++) {
            VideoModel video = videosShown.get(i);
            _videoPreviews.get(i).bind(video);
            _previews.addView(_videoPreviews.get(i));
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
                Map<String, List<User>> unseenMap = generateUnseenMap(unseens);
                _videoPreviews.get(0).addSeenUsers(unseenMap.get(USERS_ALL_SEEN));
                for (int i = 0; i < _videoPreviews.size(); i++) {
                    VideoPreview videoPreview = _videoPreviews.get(i);
                    videoPreview.addUnseenUsers(unseenMap.get(videosShown.get(i).getId()));
                }
                _videoPreviews.get(_videoPreviews.size() - 1)
                        .addUnseenUsers(unseenMap.get(USERS_NONE_SEEN));
            }
        });
        return v;
    }

    /**
     * This method generates a map, with key of video id and value of a list
     * of users for which this is FIRST unseen video.
     */
    public Map<String, List<User>> generateUnseenMap(List<Unseen> unseens) {
        Map<String, List<User>> unseenMap = new HashMap<>();
        List<User> usersAllSeen = new ArrayList<>();
        List<User> usersNoneSeen = new ArrayList<>();
        for (Unseen unseen : unseens) {
            User user = unseen.getUser();
            if (User.getCurrentUser().getObjectId().equals(user.getObjectId())) {
                usersAllSeen.add(user);
                continue;
            }
            List<Video> unseenVideos = unseen.getUnseenVideos();
            if (unseenVideos.isEmpty()) {
                usersAllSeen.add(user);
            } else {
                Video firstUnseen = unseenVideos.get(0);
                if (!_vidtrainModel.containsVideo(firstUnseen.getObjectId())) {
                    usersNoneSeen.add(user);
                }
                List<User> users;
                if (unseenMap.containsKey(firstUnseen.getObjectId())) {
                    users = unseenMap.get(firstUnseen.getObjectId());
                } else {
                    users = new ArrayList<>();
                }
                users.add(user);
                unseenMap.put(firstUnseen.getObjectId(), users);
            }
        }
        // two special cases
        unseenMap.put(USERS_ALL_SEEN, usersAllSeen);
        unseenMap.put(USERS_NONE_SEEN, usersNoneSeen);
        return unseenMap;
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
