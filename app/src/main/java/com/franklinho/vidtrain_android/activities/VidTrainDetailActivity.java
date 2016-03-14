package com.franklinho.vidtrain_android.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.franklinho.vidtrain_android.models.DynamicVideoPlayerView;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.models.Video;
import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.franklinho.vidtrain_android.utilities.VideoPlayer;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.volokh.danylo.video_player_manager.ui.SimpleMainThreadMediaPlayerListener;

import java.io.File;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VidTrainDetailActivity extends AppCompatActivity {
    @Bind(R.id.ivCollaborators) ImageView ivCollaborators;
    @Bind(R.id.vvPreview) DynamicVideoPlayerView vvPreview;
    @Bind(R.id.ivThumbnail) ImageView ivThumbnail;
    @Bind(R.id.ibtnLike) ImageButton ibtnLike;
    @Bind(R.id.tvLikeCount) TextView tvLikeCount;
    @Bind(R.id.tvVideoCount) TextView tvVideoCount;
    @Bind(R.id.tvTime) TextView tvTime;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.btnAddvidTrain) Button btnAddvidTrain;
    @Bind(R.id.pbProgressAction) View pbProgessAction;

    public static final String VIDTRAIN_KEY = "vidTrain";
    private ProgressDialog progress;
    private VidTrain vidTrain;
    private static final int VIDEO_CAPTURE = 101;
    private int nextIndex;
    public boolean liked = false;
    String totalVideos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vid_train_detail);
        ButterKnife.bind(this);
        nextIndex = 0;
        vvPreview.setHeightRatio(1);
        String vidTrainId = getIntent().getExtras().getString(VIDTRAIN_KEY);
        ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.whereEqualTo("objectId", vidTrainId);;
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

                if (User.hasLikedVidtrain(ParseUser.getCurrentUser(), vidTrain.getObjectId())){
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
//                vidTrain.getUser().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
//                    @Override
//                    public void done(ParseObject object, ParseException e) {
//                        String profileImageUrl = User.getProfileImageUrl(vidTrain.getUser());
//                        Glide.with(getBaseContext()).load(profileImageUrl).into(ivCollaborators);
//                    }
//                });

                vvPreview.setHeightRatio(1);

                //Insert async here


                new VideoDownloadTask(vvPreview).execute(vidTrain);

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
            parseFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    video.setUser(user);
                    video.setVideoFile(parseFile);
                    video.setVidTrain(vidTrain);
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
            User.postUnlike(ParseUser.getCurrentUser(), vidTrain.getObjectId().toString());
            liked = false;
            ibtnLike.setImageResource(R.drawable.heart_icon);
            int currentLikeCount = vidTrain.getLikes();
            if (currentLikeCount > 0) {
                vidTrain.setLikes(currentLikeCount - 1);
            } else {
                vidTrain.setLikes(0);
            }
        } else {
            User.postLike(ParseUser.getCurrentUser(), vidTrain.getObjectId().toString());
            liked = true;
            ibtnLike.setImageResource(R.drawable.heart_icon_red);
            vidTrain.setLikes(vidTrain.getLikes() + 1);
        }
        view.startAnimation(animScale);
        tvLikeCount.setText(vidTrain.getLikes() + " likes");
    }

    public void configureVideoPlayer(final List<File> localFiles) {
        vvPreview.addMediaPlayerListener(new SimpleMainThreadMediaPlayerListener() {
            @Override
            public void onVideoCompletionMainThread() {
                nextIndex += 1;
                if (nextIndex >= localFiles.size()) {
                    Log.d(VidtrainApplication.TAG, "Finished playing all videos!");
                    tvVideoCount.setText(totalVideos);
                    nextIndex = 0;
                    setProfileImageUrlAtIndex(0);
                    ivThumbnail.setImageBitmap(Utility.getImageBitmap(localFiles.get(0)
                            .getPath()));
                    ivThumbnail.setVisibility(View.VISIBLE);

                    return;
                }
                Log.d(VidtrainApplication.TAG,
                        String.format("Finished playing video %s of %s",
                                nextIndex + 1, localFiles.size()));
                VideoPlayer.playVideo(vvPreview, localFiles.get(nextIndex).getPath());
                int videoLabelIndex = nextIndex + 1;
                tvVideoCount.setText("Playing "+ videoLabelIndex + " of " + totalVideos);
                setProfileImageUrlAtIndex(nextIndex);
            }
        });
        ivThumbnail.setImageBitmap(Utility.getImageBitmap(localFiles.get(nextIndex)
                .getPath()));
        ivThumbnail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                VideoPlayer.playVideo(vvPreview, localFiles.get(nextIndex).getPath());
                ivThumbnail.setVisibility(View.GONE);
                int videoLabelIndex = nextIndex + 1;
                tvVideoCount.setText("Playing " + videoLabelIndex + " of " + totalVideos);
                setProfileImageUrlAtIndex(nextIndex);
            }
        });
        vvPreview.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                VideoPlayer.playVideo(vvPreview, localFiles.get(0).getPath());
            }
        });
        setProfileImageUrlAtIndex(nextIndex);
        hideProgressBar();
    }

    private class VideoDownloadTask extends AsyncTask<VidTrain, Void, List<File>> {
        DynamicVideoPlayerView videoPlayerView;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressBar();
        }

        public VideoDownloadTask(DynamicVideoPlayerView videoPlayerView) {
            this.videoPlayerView = videoPlayerView;
        }

        @Override
        protected List<File> doInBackground(VidTrain... params) {
            return vidTrain.getVideoFiles();
        }

        @Override
        protected void onPostExecute(List<File> localFiles) {

            configureVideoPlayer(localFiles);
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
