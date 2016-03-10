package com.franklinho.vidtrain_android.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.models.DynamicHeightVideoPlayerManagerView;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.models.Video;
import com.google.common.io.Files;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.volokh.danylo.video_player_manager.manager.PlayerItemChangeListener;
import com.volokh.danylo.video_player_manager.manager.SingleVideoPlayerManager;
import com.volokh.danylo.video_player_manager.manager.VideoPlayerManager;
import com.volokh.danylo.video_player_manager.meta.MetaData;
import com.volokh.danylo.video_player_manager.ui.SimpleMainThreadMediaPlayerListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class VidTrainDetailActivity extends AppCompatActivity {
    public VidTrain vidTrain;
    @Bind(R.id.ivCollaborators)
    ImageView ivCollaborators;
    @Bind(R.id.vvPreview)
    DynamicHeightVideoPlayerManagerView vvPreview;
    @Bind(R.id.ibtnLike)
    ImageButton ibtnLike;
    @Bind(R.id.tvLikeCount)
    TextView tvLikeCount;
    @Bind(R.id.tvCommentCount)
    TextView tvCommentCount;

    private VideoPlayerManager<MetaData> mVideoPlayerManager = new SingleVideoPlayerManager(new PlayerItemChangeListener() {
        @Override
        public void onPlayerItemChanged(MetaData metaData) {

        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vid_train_detail);
        ButterKnife.bind(this);

        vvPreview.setHeightRatio(1);

        String vidTrainObjectID = getIntent().getExtras().getString("vidTrain");
        ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
        query.whereEqualTo("objectId",vidTrainObjectID);
        query.setLimit(1);
        query.findInBackground(new FindCallback<VidTrain>() {
            @Override
            public void done(List<VidTrain> objects, ParseException e) {
                if (e == null) {
                    vidTrain = objects.get(0);

                    vidTrain.getUser().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject object, ParseException e) {
                            String profileImageUrl = ((ParseUser) vidTrain.getUser()).getString("profileImageUrl");
                            Glide.with(getBaseContext()).load(profileImageUrl).into(ivCollaborators);
                        }
                    });


                    vvPreview.setHeightRatio(1);

                    vvPreview.setVisibility(View.VISIBLE);
                    vvPreview.addMediaPlayerListener(new SimpleMainThreadMediaPlayerListener() {
                        @Override
                        public void onVideoCompletionMainThread() {
                            vvPreview.start();
                        }
                    });


                    mVideoPlayerManager.playNewVideo(null, vvPreview, ((ParseFile) vidTrain.get("thumbnail")).getUrl());
                } else {
                    invalidVidTrain();
                }
            }
        });
    }

    public void invalidVidTrain() {
        Toast.makeText(getBaseContext(), "This VidTrain is invalid",
                Toast.LENGTH_SHORT).show();
        this.finish();
    }

    public void showCreateFlow(View view) {
//        Toast.makeText(this, "Should navigate to creation flow", Toast.LENGTH_SHORT).show();

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            startCameraActivity();
        } else {
            Toast.makeText(this, "No camera on device", Toast.LENGTH_LONG).show();
        }
    }

    public void startCameraActivity() {

        Intent i = new Intent(this, CustomCameraActivity.class);
        i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.putExtra("newVidTrain", false);
        startActivityForResult(i, 1);
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            String videoPath = data.getStringExtra("videoPath");
            final Video video = new Video();
            File file = new File(videoPath);
            byte[] videoFileData;
            try {
                videoFileData = Files.toByteArray(file);
                final ParseFile parseFile = new ParseFile("video.mp4", videoFileData);
                parseFile.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        video.setUser(ParseUser.getCurrentUser());
                        video.setVideoFile(parseFile);
                        video.setVidTrain(vidTrain);
                        video.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                ArrayList<Video> videos;
                                vidTrain.setThumbnailFile(parseFile);

                                if (vidTrain.get("videos") == null) {
                                    videos = new ArrayList<>();
                                } else {
                                    videos = (ArrayList<Video>) vidTrain.get("videos");
                                }
                                videos.add(video);
                                vidTrain.setVideos(videos);



                                vidTrain.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {

                                        ArrayList<VidTrain> vidTrains;
                                        if (ParseUser.getCurrentUser().get("vidtrains") == null) {
                                            vidTrains = new ArrayList<>();

                                        } else {
                                            vidTrains = (ArrayList<VidTrain>) ParseUser.getCurrentUser().get("vidtrains");
                                        }
                                        if (!vidTrains.contains(vidTrain)) {
                                            vidTrains.add(vidTrain);
                                        }
                                        ParseUser.getCurrentUser().put("vidtrains", vidTrains);


                                        ArrayList<Video> videos;
                                        if (ParseUser.getCurrentUser().get("videos") == null) {
                                            videos = new ArrayList<>();
                                        } else {
                                            videos = (ArrayList<Video>) ParseUser.getCurrentUser().get("vidtrains");
                                        }
                                        videos.add(video);
                                        ParseUser.getCurrentUser().put("videos", vidTrains);

                                        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                successfullyAddedVideo();
                                            }
                                        });


                                    }
                                });

                            }
                        });
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void successfullyAddedVideo() {
        Toast.makeText(getBaseContext(), "Successfully added video",
                Toast.LENGTH_SHORT).show();

    }
}
