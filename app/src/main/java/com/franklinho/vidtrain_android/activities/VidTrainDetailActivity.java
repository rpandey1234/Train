package com.franklinho.vidtrain_android.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.adapters.VideoPagerAdapter;
import com.franklinho.vidtrain_android.models.DynamicVideoPlayerView;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.models.Video;
import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.franklinho.vidtrain_android.utilities.VideoPageIndicator;
import com.franklinho.vidtrain_android.utilities.VideoPlayer;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.volokh.danylo.video_player_manager.ui.SimpleMainThreadMediaPlayerListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VidTrainDetailActivity extends AppCompatActivity {

    @Bind(R.id.ivCollaborators) ImageView _ivCollaborators;
    @Bind(R.id.tvVideoCount) TextView _tvVideoCount;
    @Bind(R.id.tvAuthor) TextView _tvAuthor;
    @Bind(R.id.tvTitle) TextView _tvTitle;
    @Bind(R.id.tvTime) TextView _tvTime;
    @Bind(R.id.toolbar) Toolbar _toolbar;
    @Bind(R.id.btnAddvidTrain) Button _btnAddVidTrain;
    @Bind(R.id.pbProgressAction) View _pbProgessAction;
    @Bind(R.id.vpPreview) ViewPager _vpPreview;
    @Bind(R.id.cpIndicator) VideoPageIndicator _cpIndicator;
    @Bind(R.id.swipeContainer) SwipeRefreshLayout _swipeContainer;

    public static final String VIDTRAIN_KEY = "vidTrain";
    public static final int VIDEO_CAPTURE = 101;
    private ProgressDialog _progress;
    private VidTrain _vidTrain;
    private VideoPagerAdapter _videoPagerAdapter;
    private List<File> _fileList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vid_train_detail);
        ButterKnife.bind(this);
        VideoPlayer.resetVideoPlayerManager();

        setSupportActionBar(_toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        _toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        _toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        requestVidTrain();

        _swipeContainer.setColorSchemeResources(R.color.bluePrimary);
        _swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestVidTrain();
            }
        });
    }

    public void invalidVidtrain() {
        Toast.makeText(this, "This Vidtrain is invalid", Toast.LENGTH_SHORT).show();
        this.finish();
    }

    @OnClick(R.id.btnAddvidTrain)
    public void showCreateFlow(View view) {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            VideoPlayer.resetVideoPlayerManager();
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
        if (data == null) {
            Log.d(VidtrainApplication.TAG, "intent data is null");
            Toast.makeText(this, "Intent data is null.",  Toast.LENGTH_LONG).show();
            return;
        }
        if (resultCode == RESULT_OK) {
            _progress = ProgressDialog
                    .show(this, "Adding your video", "Just a moment please!", true);
            // data.getData().toString() is the following:
            // "file:///storage/emulated/0/Movies/VidTrainApp/VID_CAPTURED.mp4"
            // below is where data is stored:
            // "/storage/emulated/0/Movies/VidTrainApp/VID_CAPTURED.mp4"
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
        query.getFirstInBackground(new GetCallback<VidTrain>() {
            @Override
            public void done(VidTrain object, ParseException e) {
                _swipeContainer.setRefreshing(false);
                if (e != null) {
                    invalidVidtrain();
                    return;
                }
                // Need to re-fetch vidtrain since it becomes null on some phones/OS
                _vidTrain = object;
                final int videosCount = _vidTrain.getVideosCount();
                _tvVideoCount.setText(String.valueOf(videosCount + 1));
                final Video video = new Video();
                final ParseUser user = ParseUser.getCurrentUser();
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
                                        layoutVidTrain();
                                        sendNotifications(_vidTrain);
                                        user.put("vidtrains", User.maybeInitAndAdd(user, _vidTrain));
                                        user.put("videos", User.maybeInitAndAdd(user, video));
                                        user.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                _progress.dismiss();
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

    private void sendNotifications(final VidTrain vidtrain) {
        List<Video> collabvideos = vidtrain.getVideos();
        final List<ParseUser> notificationsSent = new ArrayList<>();
        for (final Video collabVideo : collabvideos) {
            collabVideo.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (!collabVideo.getUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                        if (!notificationsSent.contains(collabVideo.getUser())) {
                            sendVidtrainUpdatedNotification(collabVideo.getUser(), vidtrain);
                            notificationsSent.add(collabVideo.getUser());
                        }
                    }
                }
            });
        }
    }

    public void setProfileImageUrlAtIndex(int index) {
        List<Video> videos = _vidTrain.getVideos();
        final Video video = videos.get(index);
        video.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                final ParseUser user = video.getUser();
                user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        String profileImageUrl = User.getProfileImageUrl(video.getUser());
                        Glide.with(getBaseContext()).load(profileImageUrl).into(_ivCollaborators);
                        _tvAuthor.setText(User.getName(video.getUser()));
                    }
                });
            }
        });
    }

    private class VideoDownloadTask extends AsyncTask<VidTrain, Void, List<File>> {
        ViewPager viewPager;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressBar();
        }

        public VideoDownloadTask(ViewPager viewPager) {
            this.viewPager = viewPager;
        }

        @Override
        protected List<File> doInBackground(VidTrain... params) {
            return _vidTrain.getVideoFiles();
        }

        @Override
        protected void onPostExecute(final List<File> localFiles) {
            _fileList = localFiles;
            _videoPagerAdapter =  new VideoPagerAdapter(getBaseContext(), _fileList);
            viewPager.setAdapter(_videoPagerAdapter);
            viewPager.setClipChildren(false);
            int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20 * 2,
                    getResources().getDisplayMetrics());
            viewPager.setPageMargin(-margin);
            _cpIndicator.setViewPager(viewPager);
            _cpIndicator.notifyDataSetChanged();
            playVideoAtPosition(0);

            viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(final int position) {
                    playVideoAtPosition(position);
                }
            });
        }

        private void playVideoAtPosition(final int position) {
            setProfileImageUrlAtIndex(position);
            View pagerView = _videoPagerAdapter.getView(position);
            if (pagerView != null) {
                final DynamicVideoPlayerView vvPreview = (DynamicVideoPlayerView) pagerView.findViewById(R.id.vvPreview);
                final ImageView ivThumbnail = (ImageView) pagerView.findViewById(R.id.ivThumbnail);
                ivThumbnail.setVisibility(View.GONE);
                vvPreview.addMediaPlayerListener(new SimpleMainThreadMediaPlayerListener() {
                    @Override
                    public void onVideoCompletionMainThread() {
                        if (position < _fileList.size()) {
                            ivThumbnail.setVisibility(View.VISIBLE);
                            _vpPreview.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                        }
                    }
                });
                if (position == _videoPagerAdapter.getCount() - 1) {
                    // restart from beginning on click
                    ivThumbnail.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            _vpPreview.setCurrentItem(0, true);
                        }
                    });
                }
                VideoPlayer.playVideo(vvPreview, _fileList.get(position).getPath());
            }
        }
    }

    public void showProgressBar() {
        // Show _progress item
        _pbProgessAction.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        // Hide progress item
        _pbProgessAction.setVisibility(View.GONE);
    }

    void requestVidTrain() {
        ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.whereEqualTo("objectId", getVidtrainId());
        query.include("user");
        query.getFirstInBackground(new GetCallback<VidTrain>() {
            @Override
            public void done(VidTrain object, ParseException e) {
                _swipeContainer.setRefreshing(false);
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
                Utility.contains(_vidTrain.getCollaborators(), ParseUser.getCurrentUser())) {
            _btnAddVidTrain.setVisibility(View.VISIBLE);
        }
        _tvTitle.setText(_vidTrain.getTitle());
        int videosCount = _vidTrain.getVideosCount();
        _tvVideoCount.setText(String.valueOf(videosCount));
        _tvTime.setText(Utility.getRelativeTime(_vidTrain.getCreatedAt().getTime()));
        _vidTrain.getUser().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                String profileImageUrl = User.getProfileImageUrl(_vidTrain.getUser());
                Glide.with(getBaseContext()).load(profileImageUrl).placeholder(
                        R.drawable.profile_icon).into(_ivCollaborators);
                _tvAuthor.setText(User.getName(_vidTrain.getUser()));
            }
        });
        new VideoDownloadTask(_vpPreview).execute(_vidTrain);
    }

    public void sendVidtrainUpdatedNotification(ParseUser user, VidTrain vidtrain) {
        ParseQuery pushQuery = ParseInstallation.getQuery();
        pushQuery.whereEqualTo("user", user.getObjectId());
//        pushQuery.whereEqualTo("channels", vidtrain.getObjectId()); // Set the channel
        pushQuery.whereNotEqualTo("objectId", ParseInstallation.getCurrentInstallation().getObjectId());
        String currentUserName = ParseUser.getCurrentUser().getString("name");
        String alertString = currentUserName + " has just updated the Vidtrain: " + vidtrain.getTitle();
        JSONObject data = new JSONObject();

        try {
            data.put("alert", alertString);
            data.put("title", "Vidtrain");
            data.put("vidTrain", vidtrain.getObjectId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ParsePush push = new ParsePush();
        push.setQuery(pushQuery);
        push.setData(data);
        push.sendInBackground();
    }
}
