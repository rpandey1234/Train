package com.franklinho.vidtrain_android.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.common.io.Files;

import com.bumptech.glide.Glide;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.models.DynamicHeightVideoPlayerManagerView;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.models.Video;
import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.volokh.danylo.video_player_manager.ui.SimpleMainThreadMediaPlayerListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class VidTrainDetailActivity extends AppCompatActivity {
    @Bind(R.id.ivCollaborators) ImageView ivCollaborators;
    @Bind(R.id.vvPreview) DynamicHeightVideoPlayerManagerView vvPreview;
    @Bind(R.id.ibtnLike) ImageButton ibtnLike;
    @Bind(R.id.tvLikeCount) TextView tvLikeCount;
    @Bind(R.id.tvCommentCount) TextView tvCommentCount;
    @Bind(R.id.tvVideoCount) TextView tvVideoCount;

    public VidTrain vidTrain;
    private static final int VIDEO_CAPTURE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vid_train_detail);
        ButterKnife.bind(this);

        vvPreview.setHeightRatio(1);

        final String vidTrainObjectID = getIntent().getExtras().getString("vidTrain");
        ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        query.whereEqualTo("objectId", vidTrainObjectID);;

        query.setLimit(1);
        // TODO: use query.getFirstInBackground(new GetCallback<ParseObject>() {
        query.findInBackground(new FindCallback<VidTrain>() {
            @Override
            public void done(List<VidTrain> objects, ParseException e) {
                if (e == null) {
                    vidTrain = objects.get(0);
                    String countString = String.format(getString(R.string.video_count), vidTrain.getVideosCount());
                    tvVideoCount.setText(countString);
                    vidTrain.getUser().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject object, ParseException e) {
                            String profileImageUrl = User.getProfileImageUrl(vidTrain.getUser());
                            Glide.with(getBaseContext()).load(profileImageUrl).into(ivCollaborators);
                        }
                    });

                    ArrayList<Video> videos = vidTrain.getVideos();
                    // TODO: sequential loading
                    for (Video video : videos) {
                        try {
                            video.fetchIfNeeded();
                        } catch (ParseException parseException) {
                            Log.d(VidtrainApplication.TAG, parseException.toString());
                        }
                    }
                    vvPreview.setHeightRatio(1);
                    vvPreview.setVisibility(View.VISIBLE);
                    final ParseFile parseFile = ((ParseFile) vidTrain.get("thumbnail"));
                    parseFile.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] data, ParseException e) {
                            try {
                                File videoFile = VidtrainApplication.getOutputMediaFile(vidTrain.getObjectId());
                                FileOutputStream out;

                                out = new FileOutputStream(videoFile);
                                out.write(data);
                                out.close();

                                vvPreview.addMediaPlayerListener(
                                        new SimpleMainThreadMediaPlayerListener() {
                                            @Override
                                            public void onVideoCompletionMainThread() {
                                                Toast.makeText(getBaseContext(),
                                                        "Video ready for: " + vidTrain.getTitle(),
                                                        Toast.LENGTH_SHORT).show();
                                                vvPreview.start();
                                            }
                                        });
                                VidtrainApplication
                                        .getVideoPlayerInstance()
                                        .playNewVideo(null, vvPreview, videoFile.getPath());
                            } catch (FileNotFoundException e1) {
                                e1.printStackTrace();
                                Log.d("TAG", "Error: " + e1.toString());
                            } catch (IOException ioe) {
                                ioe.printStackTrace();
                            }
                        }
                    });
                } else {
                    invalidVidTrain();
                }
            }
        });
    }

    public void invalidVidTrain() {
        Toast.makeText(getBaseContext(), "This VidTrain is invalid", Toast.LENGTH_SHORT).show();
        this.finish();
    }

    public void showCreateFlow(View view) {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            startCameraActivity();
        } else {
            Toast.makeText(this, "No camera on device", Toast.LENGTH_LONG).show();
        }
    }

    public void startCameraActivity() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        Uri videoUri = VidtrainApplication.getOutputMediaFileUri();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri); ;
        startActivityForResult(intent, VIDEO_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != VIDEO_CAPTURE) {
            return;
        }
        if (resultCode == RESULT_OK) {
            Toast.makeText(this, "Video saved to:\n" + data.getData(), Toast.LENGTH_SHORT).show();
            final Video video = new Video();
            File file = VidtrainApplication.getOutputMediaFile();
            byte[] videoFileData;
            try {
                videoFileData = Files.toByteArray(file);
                final ParseUser currentUser = ParseUser.getCurrentUser();
                final ParseFile parseFile = new ParseFile("video.mp4", videoFileData);
                parseFile.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        video.setUser(currentUser);
                        video.setVideoFile(parseFile);
                        video.setVidTrain(vidTrain);
                        video.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                vidTrain.setThumbnailFile(parseFile);
                                vidTrain.setVideos(vidTrain.maybeInitAndAdd(video));
                                vidTrain.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        currentUser.put("vidtrains", User.maybeInitAndAdd(
                                                currentUser, vidTrain));
                                        currentUser.put("videos", User.maybeInitAndAdd(
                                                currentUser, video));
                                        currentUser.saveInBackground(
                                                new SaveCallback() {
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
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Video recording cancelled.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to record video", Toast.LENGTH_LONG).show();
        }
    }

    public void successfullyAddedVideo() {
        Toast.makeText(getBaseContext(), "Successfully added video", Toast.LENGTH_SHORT).show();
    }
}
