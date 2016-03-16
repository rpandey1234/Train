package com.franklinho.vidtrain_android.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
import com.franklinho.vidtrain_android.utilities.Utility;
import com.franklinho.vidtrain_android.utilities.VideoPageIndicator;
import com.franklinho.vidtrain_android.utilities.VideoPlayer;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.volokh.danylo.video_player_manager.PlayerMessageState;
import com.volokh.danylo.video_player_manager.ui.SimpleMainThreadMediaPlayerListener;

import java.io.File;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VidTrainDetailActivity extends AppCompatActivity {
    @Bind(R.id.ivCollaborators) ImageView ivCollaborators;
//    @Bind(R.id.vvPreview) DynamicVideoPlayerView vvPreview;
//    @Bind(R.id.ivThumbnail) ImageView ivThumbnail;
    @Bind(R.id.ibtnLike) ImageButton ibtnLike;
    @Bind(R.id.tvLikeCount) TextView tvLikeCount;
    @Bind(R.id.tvVideoCount) TextView tvVideoCount;
    @Bind(R.id.tvTime) TextView tvTime;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.btnAddvidTrain) Button btnAddvidTrain;
    @Bind(R.id.pbProgressAction) View pbProgessAction;
    @Bind(R.id.vpPreview) ViewPager vpPreview;
    @Bind(R.id.cpIndicator) VideoPageIndicator cpIndicator;

    public static final String VIDTRAIN_KEY = "vidTrain";
    private ProgressDialog progress;
    private VidTrain vidTrain;
    private static final int VIDEO_CAPTURE = 101;
    private int nextIndex;
    public boolean liked = false;
    String totalVideos;
    VideoPagerAdapter videoPagerAdapter;
    List<File> filesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vid_train_detail);
        ButterKnife.bind(this);
        nextIndex = 0;
//        vvPreview.setHeightRatio(1);
        final View view = getWindow().getDecorView();
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                vpPreview.post(new Runnable() {
                    public void run() {
                        int width = view.getWidth();
//                        int height = width;
//                        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
//                        vpPreview.setLayoutParams(lp);
                        ViewGroup.LayoutParams lp = vpPreview.getLayoutParams();
                        lp.height = width;
                        vpPreview.setLayoutParams(lp);
                    }
                });
            }
        });


        String vidTrainId = getIntent().getExtras().getString(VIDTRAIN_KEY);
        ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.whereEqualTo("objectId", vidTrainId);
        query.include("user");
        query.getFirstInBackground(new GetCallback<VidTrain>() {
            @Override
            public void done(VidTrain object, ParseException e) {
                if (e != null) {
                    invalidVidtrain();
                    return;
                }
                vidTrain = object;
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

                toolbar.setTitle(vidTrain.getTitle());
                int videosCount = vidTrain.getVideosCount();
                totalVideos = getResources().getQuantityString(R.plurals.videos_count,
                        videosCount, videosCount);
                tvVideoCount.setText(totalVideos);
                tvTime.setText(Utility.getRelativeTime(vidTrain.getCreatedAt().getTime()));
                vidTrain.getUser().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        String profileImageUrl = User.getProfileImageUrl(vidTrain.getUser());
                        Glide.with(getBaseContext()).load(profileImageUrl).into(ivCollaborators);
                    }
                });
                new VideoDownloadTask(vpPreview).execute(vidTrain);
            }
        });
    }

    public void invalidVidtrain() {
        Toast.makeText(this, "This VidTrain is invalid", Toast.LENGTH_SHORT).show();
        this.finish();
    }

    public void showCreateFlow(View view) {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            startActivityForResult(Utility.getVideoIntent(), VIDEO_CAPTURE);
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
            progress = ProgressDialog.show(this, "Adding your video", "Just a moment please!",
                    true);
            // data.getData().toString() is the following:
            // "file:///storage/emulated/0/Movies/VidTrainApp/VID_CAPTURED.mp4"
            // below is where data is stored:
            // "/storage/emulated/0/Movies/VidTrainApp/VID_CAPTURED.mp4"
            String videoPath = Utility.getOutputMediaFile().getPath();
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
                    final SaveCallback vidTrainSaved = new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
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
                    };
                    video.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            vidTrain.setLatestVideo(parseFile);
                            vidTrain.setVideos(vidTrain.maybeInitAndAdd(video));
                            vidTrain.saveInBackground(vidTrainSaved);
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
                ParseUser user = video.getUser();
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
            cpIndicator.setViewPager(viewPager);
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
            View pagerView = videoPagerAdapter.positionMap.get(position);
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

    public void showProgressBar() {
        // Show progress item
        pbProgessAction.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        // Hide progress item
        pbProgessAction.setVisibility(View.GONE);
    }

}
