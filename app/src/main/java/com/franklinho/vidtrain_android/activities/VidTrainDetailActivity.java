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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.parse.GetDataCallback;
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
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VidTrainDetailActivity extends AppCompatActivity {

    @Bind(R.id.ivCollaborators) ImageView ivCollaborators;
    @Bind(R.id.ibtnLike) ImageButton ibtnLike;
    @Bind(R.id.tvLikeCount) TextView tvLikeCount;
    @Bind(R.id.tvVideoCount) TextView tvVideoCount;
    @Bind(R.id.tvTitle) TextView tvTitle;
    @Bind(R.id.tvTime) TextView tvTime;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.btnAddvidTrain) Button btnAddvidTrain;
    @Bind(R.id.pbProgressAction) View pbProgessAction;
    @Bind(R.id.vpPreview) ViewPager vpPreview;
    @Bind(R.id.cpIndicator) VideoPageIndicator cpIndicator;
    @Bind(R.id.swipeContainer) SwipeRefreshLayout swipeContainer;

    public static final String VIDTRAIN_KEY = "vidTrain";
    public static final int VIDEO_CAPTURE = 101;
    private ProgressDialog progress;
    private VidTrain vidTrain;
    private boolean liked = false;
    private String totalVideos;
    private VideoPagerAdapter videoPagerAdapter;
    private List<File> filesList;
    private String uniqueId = Long.toString(System.currentTimeMillis());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vid_train_detail);
        ButterKnife.bind(this);
        VideoPlayer.resetVideoPlayerManager();

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();



        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDefaultDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        requestVidTrain(true);

        swipeContainer.setColorSchemeResources(R.color.bluePrimary);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestVidTrain(false);
            }
        });
    }

    public void invalidVidtrain() {
        Toast.makeText(this, "This VidTrain is invalid", Toast.LENGTH_SHORT).show();
        this.finish();
    }

    public void showCreateFlow(View view) {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            VideoPlayer.resetVideoPlayerManager();
            Intent intent = new Intent(getBaseContext(), VideoCaptureActivity.class);
            intent.putExtra(HomeActivity.UNIQUE_ID_INTENT, uniqueId);
            intent.putExtra(HomeActivity.SHOW_CONFIRM, true);
            startActivityForResult(intent, VIDEO_CAPTURE);
//            startActivityForResult(Utility.getVideoIntent(), VIDEO_CAPTURE);
        } else {
            Toast.makeText(this, "No camera on device", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != VIDEO_CAPTURE) {
            return;
        }
        if (resultCode == RESULT_OK) {
            final int videosCount = vidTrain.getVideosCount();
            totalVideos = getResources().getQuantityString(R.plurals.videos_count,
                    videosCount + 1, videosCount + 1);
            tvVideoCount.setText(totalVideos);
            progress = ProgressDialog.show(this, "Adding your video", "Just a moment please!",
                    true);
            // data.getData().toString() is the following:
            // "file:///storage/emulated/0/Movies/VidTrainApp/VID_CAPTURED.mp4"
            // below is where data is stored:
            // "/storage/emulated/0/Movies/VidTrainApp/VID_CAPTURED.mp4"
            String videoPath = Utility.getOutputMediaFile(uniqueId).getPath();
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
                    video.setVidTrain(vidTrain);
                    video.setThumbnail(parseThumbnail);
                    video.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            vidTrain.setLatestVideo(parseFile);
                            vidTrain.setVideos(vidTrain.maybeInitAndAdd(video));
                            vidTrain.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    layoutVidTrain();
                                    List<Video> collabvideos = vidTrain.getVideos();
                                    for (final Video collabvideo : collabvideos) {
                                        collabvideo.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                            @Override
                                            public void done(ParseObject object, ParseException e) {
                                                if (collabvideo.getUser().getObjectId() != ParseUser.getCurrentUser().getObjectId()) {
                                                    sendVidtrainUpdatedNotification(collabvideo.getUser(), vidTrain);
                                                }
                                            }
                                        });
                                    }
                                    user.put("vidtrains", User.maybeInitAndAdd(user, vidTrain));
                                    user.put("videos", User.maybeInitAndAdd(user, video));
                                    user.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            progress.dismiss();
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
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Video recording cancelled.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to record video", Toast.LENGTH_LONG).show();
        }
    }

    public void setProfileImageUrlAtIndex(int index) {
        List<Video> videos = vidTrain.getVideos();
        final Video video = videos.get(index);
        video.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                final ParseUser user = video.getUser();
                ivCollaborators.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                        intent.putExtra(ProfileActivity.USER_ID, user.getObjectId());
                        startActivity(intent);
                    }
                });
                user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        String profileImageUrl = User.getProfileImageUrl(video.getUser());
                        Glide.with(getBaseContext()).load(profileImageUrl).into(ivCollaborators);
                    }
                });
            }
        });
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.ivCollaborators)
    public void onCollaboratorClicked(View view) {
        ParseUser user = vidTrain.getUser();
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_ID, user.getObjectId());
        this.startActivity(intent);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.ibtnLike)
    public void onVidTrainLiked(View view) {
        final Animation animScale = AnimationUtils.loadAnimation(this, R.anim.anim_scale);
        if (liked) {
            User.postUnlike(ParseUser.getCurrentUser(), vidTrain.getObjectId());
            liked = false;
            ibtnLike.setImageResource(R.drawable.heart_icon);
            int currentLikeCount = vidTrain.getLikes();
            if (currentLikeCount > 0) {
                vidTrain.setLikes(currentLikeCount - 1);
            } else {
                vidTrain.setLikes(0);
            }
        } else {
            User.postLike(ParseUser.getCurrentUser(), vidTrain.getObjectId());
            liked = true;
            ibtnLike.setImageResource(R.drawable.heart_icon_red);
            vidTrain.setLikes(vidTrain.getLikes() + 1);
        }
        view.startAnimation(animScale);
        tvLikeCount.setText(getResources().getQuantityString(R.plurals.likes_count,
                vidTrain.getLikes(), vidTrain.getLikes()));
    }

    void updateVideos() {
        final Video latestVideo = vidTrain.getVideos().get(vidTrain.getVideos().size());
        latestVideo.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                ParseFile videoFile = latestVideo.getVideoFile();
                videoFile.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] data, ParseException e) {
                        if (e == null) {
                            File localVideoFile = Utility.getOutputMediaFile(
                                    latestVideo.getObjectId());
                            Utility.writeToFile(data, localVideoFile);
                            filesList.add(localVideoFile);
                            videoPagerAdapter.notifyDataSetChanged();
                            cpIndicator.notifyDataSetChanged();
//                            VideoPlayer.resetVideoPlayerManager();
                        } else {
                            Log.d(VidtrainApplication.TAG, e.toString());
                        }
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
            return vidTrain.getVideoFiles();
        }

        @Override
        protected void onPostExecute(final List<File> localFiles) {
            filesList = localFiles;
            videoPagerAdapter =  new VideoPagerAdapter(getBaseContext(), filesList);
            viewPager.setAdapter(videoPagerAdapter);
            viewPager.setClipChildren(false);
            int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20 * 2,
                    getResources().getDisplayMetrics());
            viewPager.setPageMargin(-margin);
            cpIndicator.setViewPager(viewPager);
            cpIndicator.notifyDataSetChanged();
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
            View pagerView = videoPagerAdapter.getView(position);
            if (pagerView != null) {
                final DynamicVideoPlayerView vvPreview = (DynamicVideoPlayerView) pagerView.findViewById(R.id.vvPreview);
                final ImageView ivThumbnail = (ImageView) pagerView.findViewById(R.id.ivThumbnail);
                ivThumbnail.setVisibility(View.GONE);
                vvPreview.addMediaPlayerListener(new SimpleMainThreadMediaPlayerListener() {
                    @Override
                    public void onVideoCompletionMainThread() {
                        if (position < filesList.size()) {
                            ivThumbnail.setVisibility(View.VISIBLE);
                            vpPreview.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                        }
                    }
                });
                if (position == videoPagerAdapter.getCount() - 1) {
                    // restart from beginning on click
                    ivThumbnail.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            vpPreview.setCurrentItem(0, true);
                        }
                    });
                }
                VideoPlayer.playVideo(vvPreview, filesList.get(position).getPath());
            }
        }
    }

    public void showProgressBar() {
        // Show progress item
        pbProgessAction.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        // Hide progress item
        pbProgessAction.setVisibility(View.GONE);
    }

    void requestVidTrain(Boolean newView) {
        Intent intent = getIntent();
        String vidTrainId;
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            vidTrainId = uri.getQueryParameter(VIDTRAIN_KEY);
        } else {
            vidTrainId = getIntent().getExtras().getString(VIDTRAIN_KEY);
        }

        ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.whereEqualTo("objectId", vidTrainId);
        query.include("user");
        query.getFirstInBackground(new GetCallback<VidTrain>() {
            @Override
            public void done(VidTrain object, ParseException e) {
                swipeContainer.setRefreshing(false);
                if (e != null) {
                    invalidVidtrain();
                    return;
                }
                vidTrain = object;
                layoutVidTrain();
            }
        });
    }

    void layoutVidTrain() {
        if (!vidTrain.getWritePrivacy() ||
                Utility.contains(vidTrain.getCollaborators(), ParseUser.getCurrentUser())) {
            btnAddvidTrain.setVisibility(View.VISIBLE);
        }

        if (User.hasLikedVidtrain(ParseUser.getCurrentUser(), vidTrain.getObjectId())) {
            liked = true;
            ibtnLike.setImageResource(R.drawable.heart_icon_red);
        }

        tvLikeCount.setText(getResources().getQuantityString(R.plurals.likes_count,
                vidTrain.getLikes(), vidTrain.getLikes()));

        tvTitle.setText(vidTrain.getTitle());
        int videosCount = vidTrain.getVideosCount();
        totalVideos = getResources().getQuantityString(R.plurals.videos_count,
                videosCount, videosCount);
        tvVideoCount.setText(totalVideos);
        tvTime.setText(Utility.getRelativeTime(vidTrain.getCreatedAt().getTime()));
        vidTrain.getUser().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                String profileImageUrl = User.getProfileImageUrl(vidTrain.getUser());
                Glide.with(getBaseContext()).load(profileImageUrl).placeholder(
                        R.drawable.profile_icon).into(ivCollaborators);
            }
        });
        new VideoDownloadTask(vpPreview).execute(vidTrain);
    }

    public void sendVidtrainUpdatedNotification(ParseUser user, VidTrain vidtrain) {
        ParseQuery pushQuery = ParseInstallation.getQuery();
        pushQuery.whereEqualTo("user", user.getObjectId());
        String currentUserName = ParseUser.getCurrentUser().getString("name");
        String alertString = currentUserName + " has just updated the vidtrain: " + vidtrain.getTitle();
        JSONObject data = new JSONObject();

        try {
            data.put("alert", alertString);
            data.put("title", "VidTrain");
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
