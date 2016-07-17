package com.franklinho.vidtrain_android.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.adapters.VideoFragmentPagerAdapter;
import com.franklinho.vidtrain_android.fragments.VideoPageFragment;
import com.franklinho.vidtrain_android.fragments.VideoPageFragment.VideoFinishedListener;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.models.Video;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VidTrainDetailActivity extends AppCompatActivity implements VideoFinishedListener {

    @Bind(R.id.tvVideoCount) TextView _tvVideoCount;
    @Bind(R.id.tvTitle) TextView _tvTitle;
    @Bind(R.id.btnAddVidTrain) Button _btnAddVidTrain;
    @Bind(R.id.viewPager) ViewPager _viewPager;

    public static final String VIDTRAIN_KEY = "vidTrain";
    public static final int VIDEO_CAPTURE = 101;
    private ProgressDialog _progress;
    private VidTrain _vidTrain;
    private List<Video> _videos;
    private int _lastPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vid_train_detail);
        ButterKnife.bind(this);
        requestVidTrain();
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
        query.include("videos");
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
                                _vidTrain.setLatestVideo(parseFile);
                                _vidTrain.setVideos(_vidTrain.maybeInitAndAdd(video));
                                _vidTrain.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        _progress.dismiss();
                                        layoutVidTrain();
                                        Utility.sendNotifications(_vidTrain);
                                        assert user != null;
                                        user.put("vidtrains", user.maybeInitAndAdd(_vidTrain));
                                        user.put("videos", user.maybeInitAndAdd(video));
                                        user.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                Toast.makeText(getBaseContext(), "Successfully added video",
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

    public void requestVidTrain() {
        ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.whereEqualTo("objectId", getVidtrainId());
        query.include("user");
        query.include("videos");
        query.getFirstInBackground(new GetCallback<VidTrain>() {
            @Override
            public void done(VidTrain object, ParseException e) {
                if (e != null) {
                    invalidVidtrain();
                    return;
                }
                _vidTrain = object;
                layoutVidTrain();
            }
        });
    }

    void layoutVidTrain() {
        if (!_vidTrain.getWritePrivacy() ||
                Utility.contains(_vidTrain.getCollaborators(), User.getCurrentUser())) {
            _btnAddVidTrain.setVisibility(View.VISIBLE);
        }
        _tvTitle.setText(_vidTrain.getTitle());
        _videos = _vidTrain.getVideos();
        _tvVideoCount.setText(String.valueOf(_vidTrain.getVideosCount()));
        final VideoFragmentPagerAdapter _videoFragmentPagerAdapter =  new VideoFragmentPagerAdapter(
                getSupportFragmentManager(), getBaseContext(), _vidTrain.getVideos());
        _viewPager.setAdapter(_videoFragmentPagerAdapter);
        _viewPager.setClipChildren(false);
        int margin = getResources().getDimensionPixelOffset(R.dimen.view_pager_margin);
        _viewPager.setPageMargin(-margin);
        final SimpleOnPageChangeListener pageChangeListener = new SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(final int position) {
                // Null checks are for only needed for instant run
                VideoPageFragment lastFragment =
                        _videoFragmentPagerAdapter.getFragment(_lastPosition);
                if (lastFragment != null) {
                    lastFragment.stopVideo();
                }
                VideoPageFragment fragment = _videoFragmentPagerAdapter.getFragment(position);
                if (fragment != null) {
                    fragment.playVideo();
                }
                _lastPosition = position;
            }
        };
        // Make sure view pager fragment is already instantiated
        // http://stackoverflow.com/questions/11794269/
        _viewPager.post(new Runnable() {
            @Override
            public void run() {
                _viewPager.addOnPageChangeListener(pageChangeListener);
            }
        });
        pageChangeListener.onPageSelected(0);
    }

    @Override
    public void onVideoCompleted() {
        int currentIndex = _viewPager.getCurrentItem();
        if (currentIndex < _videos.size()) {
            _viewPager.setCurrentItem(currentIndex + 1, true);
        }
    }
}
