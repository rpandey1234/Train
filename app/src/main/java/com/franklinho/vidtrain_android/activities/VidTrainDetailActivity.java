package com.franklinho.vidtrain_android.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.volokh.danylo.video_player_manager.manager.SingleVideoPlayerManager;
import com.volokh.danylo.video_player_manager.ui.MediaPlayerWrapper.MainThreadMediaPlayerListener;

import java.io.File;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class VidTrainDetailActivity extends AppCompatActivity {
    @Bind(R.id.ivCollaborators) ImageView ivCollaborators;
    @Bind(R.id.vvPreview) DynamicVideoPlayerView vvPreview;
    @Bind(R.id.vvThumbnail) ImageView vvThumbnail;
    @Bind(R.id.ibtnLike) ImageButton ibtnLike;
    @Bind(R.id.tvLikeCount) TextView tvLikeCount;
    @Bind(R.id.tvVideoCount) TextView tvVideoCount;
    @Bind(R.id.tvTime) TextView tvTime;
    @Bind(R.id.toolbar) Toolbar toolbar;

    public VidTrain vidTrain;
    private static final int VIDEO_CAPTURE = 101;
    public static final String VIDTRAIN_KEY = "vidTrain";
    private int nextIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vid_train_detail);
        ButterKnife.bind(this);
        nextIndex = 0;
        vvPreview.setHeightRatio(1);
        String vidTrainId = getIntent().getExtras().getString(VIDTRAIN_KEY);
        ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        query.whereEqualTo("objectId", vidTrainId);;
        query.getFirstInBackground(new GetCallback<VidTrain>() {
            @Override
            public void done(VidTrain object, ParseException e) {
                if (e != null) {
                    invalidVidtrain();
                    return;
                }
                vidTrain = object;
                toolbar.setTitle(vidTrain.getTitle());
                int videosCount = vidTrain.getVideosCount();
                String totalVideos = getResources().getQuantityString(R.plurals.videos_count,
                        videosCount, videosCount);
                tvVideoCount.setText(totalVideos);
                tvTime.setText(Utility.getRelativeTime(vidTrain.getUpdatedAt().getTime()));
                vidTrain.getUser().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        String profileImageUrl = User.getProfileImageUrl(vidTrain.getUser());
                        Glide.with(getBaseContext()).load(profileImageUrl).into(ivCollaborators);
                    }
                });

                vvPreview.setHeightRatio(1);
                final List<File> localFiles = vidTrain.getVideoFiles();
                vvPreview.addMediaPlayerListener(new MainThreadMediaPlayerListener() {
                    @Override
                    public void onVideoSizeChangedMainThread(int width, int height) {}

                    @Override
                    public void onVideoPreparedMainThread() {}

                    @Override
                    public void onVideoCompletionMainThread() {
                        nextIndex += 1;
                        if (nextIndex >= localFiles.size()) {
                            Log.d(VidtrainApplication.TAG, "Finished playing all videos!");
                            return;
                        }
                        Log.d(VidtrainApplication.TAG,
                                String.format("Finished playing video %s of %s",
                                        nextIndex + 1, localFiles.size()));
                        VideoPlayer.playVideo(vvPreview, localFiles.get(nextIndex).getPath());
                    }

                    @Override
                    public void onErrorMainThread(int what, int extra) {}

                    @Override
                    public void onBufferingUpdateMainThread(int percent) {}

                    @Override
                    public void onVideoStoppedMainThread() {}
                });

                vvThumbnail.setImageBitmap(Utility.getImageBitmap(localFiles.get(nextIndex)
                        .getPath()));
                vvThumbnail.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        vvThumbnail.setVisibility(View.GONE);
                        VideoPlayer.playVideo(vvPreview, localFiles.get(nextIndex).getPath());
                    }
                });
                vvPreview.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        VideoPlayer.playVideo(vvPreview, localFiles.get(0).getPath());
                    }
                });
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
}
