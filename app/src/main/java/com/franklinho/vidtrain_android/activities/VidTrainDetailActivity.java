package com.franklinho.vidtrain_android.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.adapters.VideoFragmentPagerAdapter;
import com.franklinho.vidtrain_android.fragments.VideoPageFragment;
import com.franklinho.vidtrain_android.fragments.VideoPageFragment.VideoFinishedListener;
import com.franklinho.vidtrain_android.models.Unseen;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.models.Video;
import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VidTrainDetailActivity extends FragmentActivity implements VideoFinishedListener {

    @Bind(R.id.tvVideoCount) TextView _tvVideoCount;
    @Bind(R.id.tvTitle) TextView _tvTitle;
    @Bind(R.id.btnAddVidTrain) Button _btnAddVidTrain;
    @Bind(R.id.viewPager) ViewPager _viewPager;

    private static final boolean MARK_SEEN_VIDEOS = false;
    public static final String VIDTRAIN_KEY = "vidTrain";
    public static final int VIDEO_CAPTURE = 101;
    private ProgressDialog _progress;
    private VidTrain _vidTrain;
    private int _lastPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vid_train_detail);
        ButterKnife.bind(this);
        ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.whereEqualTo("objectId", getVidtrainId());
        query.include("user");
        query.include("videos.user");
        query.include("collaborators");
        query.getFirstInBackground(new GetCallback<VidTrain>() {
            @Override
            public void done(VidTrain object, ParseException e) {
                if (e != null) {
                    invalidVidtrain();
                    return;
                }
                _vidTrain = object;
                ParseQuery<Unseen> query = ParseQuery.getQuery("Unseen");
                query.whereEqualTo(Unseen.USER_KEY, ParseUser.getCurrentUser());
                query.whereEqualTo(Unseen.VIDTRAIN_KEY, _vidTrain);
                query.include("vidTrain.videos");
                query.include(Unseen.VIDEOS_KEY);
                query.findInBackground(new FindCallback<Unseen>() {
                    @Override
                    public void done(List<Unseen> unseenList, ParseException e) {
                        if (e != null) {
                            Log.e(VidtrainApplication.TAG, e.toString());
                            return;
                        }
                        int unseenIndex;
                        if (unseenList.isEmpty()) {
                            // This should not happen (only for older vidtrains)
                            unseenIndex = _vidTrain.getVideosCount() - 1;
                        } else {
                            unseenIndex = unseenList.get(0).getUnseenIndex();
                            Log.d(VidtrainApplication.TAG, "go directly to index: " + unseenIndex);
                        }
                        layoutVidTrain(unseenIndex);
                    }
                });
            }
        });
    }

    public void invalidVidtrain() {
        Toast.makeText(this, "This Vidtrain is invalid", Toast.LENGTH_SHORT).show();
        this.finish();
    }

    @OnClick(R.id.btnAddVidTrain)
    public void showCreateFlow(View view) {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            Intent intent = new Intent(getBaseContext(), VideoCaptureActivity.class);
            intent.putExtra(MainActivity.UNIQUE_ID_INTENT, Long.toString(System.currentTimeMillis()));
            intent.putExtra(MainActivity.SHOW_CONFIRM, true);
            startActivityForResult(intent, VIDEO_CAPTURE);
        } else {
            Toast.makeText(this, "No camera on device", Toast.LENGTH_LONG).show();
        }
    }

    private String getVidtrainId() {
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            return uri.getQueryParameter(VIDTRAIN_KEY);
        } else {
            return getIntent().getExtras().getString(VIDTRAIN_KEY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != VIDEO_CAPTURE) {
            return;
        }
        if (resultCode == RESULT_OK && data != null) {
            _progress = ProgressDialog
                    .show(this, "Adding your video", "Just a moment please!", true);
            // data.getData().toString() is file://<path>, file is stored at
            // <path> which is /storage/emulated/0/Movies/VidTrainApp/VID_CAPTURED.mp4
            String videoPath = Utility.getOutputMediaFile(
                    data.getStringExtra(MainActivity.UNIQUE_ID_INTENT)).getPath();
            addVideoToVidtrain(videoPath);
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Video recording cancelled.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to record video", Toast.LENGTH_LONG).show();
        }
    }

    private void addVideoToVidtrain(final String videoPath) {
        ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.whereEqualTo("objectId", getVidtrainId());
        query.include("user");
        query.include("videos.user");
        query.include("collaborators");
        query.getFirstInBackground(new GetCallback<VidTrain>() {
            @Override
            public void done(VidTrain object, ParseException e) {
                if (e != null) {
                    invalidVidtrain();
                    return;
                }
                // Need to re-fetch vidtrain since it becomes null on some phones/OS
                _vidTrain = object;
                final int videosCount = _vidTrain.getVideosCount();
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
                        video.setVidTrain(_vidTrain);
                        video.setThumbnail(parseThumbnail);
                        video.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                _vidTrain.setThumbnail(parseFile);
                                _vidTrain.setVideos(_vidTrain.maybeInitAndAdd(video));
                                _vidTrain.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        _progress.dismiss();
                                        layoutVidTrain(_vidTrain.getVideosCount() - 1);
                                        Utility.sendNotifications(_vidTrain);
                                        Unseen.addUnseen(_vidTrain);
                                        assert user != null;
                                        user.put("vidtrains", user.maybeInitAndAdd(_vidTrain));
                                        user.put("videos", user.maybeInitAndAdd(video));
                                        user.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                Toast.makeText(getBaseContext(),
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

    void layoutVidTrain(int initialIndex) {
        boolean shouldPlayVideo = true;
        if (initialIndex == -1) {
            // The user has seen all the videos. They can only swipe through the pics now, start
            // them at the last video
            shouldPlayVideo = false;
            initialIndex = _vidTrain.getVideosCount() - 1;
        }
        _tvTitle.setText(_vidTrain.getTitle());
        _tvVideoCount.setText(String.valueOf(_vidTrain.getVideosCount()));
        final VideoFragmentPagerAdapter _videoFragmentPagerAdapter =  new VideoFragmentPagerAdapter(
                getSupportFragmentManager(), getBaseContext(), _vidTrain.getVideos());
        _viewPager.setAdapter(_videoFragmentPagerAdapter);
        final boolean finalShouldPlayVideo = shouldPlayVideo;
        _viewPager.addOnPageChangeListener(new SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(final int position) {
                if (finalShouldPlayVideo) {
                    // Null checks are only needed for instant run
                    VideoPageFragment lastFragment =
                            _videoFragmentPagerAdapter.getFragment(_lastPosition);
                    if (lastFragment != null) {
                        lastFragment.stopVideo();
                    }
                    VideoPageFragment fragment = _videoFragmentPagerAdapter.getFragment(position);
                    if (fragment != null) {
                        fragment.playVideo();
                    }
                }
                _lastPosition = position;
            }
        });
        _viewPager.setCurrentItem(initialIndex);
    }

    @Override
    public void onVideoCompleted(Video video) {
        if (MARK_SEEN_VIDEOS) {
            Unseen.removeUnseen(_vidTrain, User.getCurrentUser(), video);
        }
        int currentIndex = _viewPager.getCurrentItem();
        if (currentIndex < _vidTrain.getVideos().size()) {
            _viewPager.setCurrentItem(currentIndex + 1, true);
        }
    }
}
