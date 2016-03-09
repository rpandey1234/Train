package com.franklinho.vidtrain_android.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.models.DynamicHeightVideoPlayerManagerView;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.models.Video;
import com.google.common.io.Files;
import com.parse.ParseException;
import com.parse.ParseFile;
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

import butterknife.Bind;
import butterknife.ButterKnife;

public class CreationDetailActivity extends AppCompatActivity {
    @Bind(R.id.vvPreview) DynamicHeightVideoPlayerManagerView vvPreview;
    @Bind(R.id.btnSubmit)
    Button btnSubmit;
    @Bind(R.id.spnReadPrivacy)
    Spinner spnReadPrivacy;
    @Bind(R.id.etTitle)
    EditText etTitle;
    @Bind(R.id.cbWritePermissions)
    CheckBox cbWritePermissions;

    String videoPath;

    private VideoPlayerManager<MetaData> mVideoPlayerManager = new SingleVideoPlayerManager(new PlayerItemChangeListener() {
        @Override
        public void onPlayerItemChanged(MetaData metaData) {

        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creation_detail);
        ButterKnife.bind(this);
        videoPath = getIntent().getExtras().getString("videoPath");
        Toast.makeText(this, "Video path: " + videoPath,
                Toast.LENGTH_SHORT).show();

        vvPreview.setHeightRatio(1);

        if (videoPath != null) {
            vvPreview.addMediaPlayerListener(new SimpleMainThreadMediaPlayerListener() {
                @Override
                public void onVideoCompletionMainThread() {
                    vvPreview.start();
                }
            });

            mVideoPlayerManager.playNewVideo(null, vvPreview, videoPath);

        }
    }

    public void submitVidTrain(View view) {
        File file = new File(videoPath);
        final Video video = new Video();
        final VidTrain vidTrain = new VidTrain();

        byte[] data;
        try {
            data = Files.toByteArray(file);
            final ParseFile parseFile = new ParseFile("video.mp4", data);
            parseFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    video.setUser(ParseUser.getCurrentUser());
                    video.setVideoFile(parseFile);
                    video.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            vidTrain.setTitle(etTitle.getText().toString());
                            vidTrain.setUser(ParseUser.getCurrentUser());
                            ArrayList<Video> videos = new ArrayList<>();
                            videos.add(video);
                            vidTrain.setVideos(videos);
                            vidTrain.setWritePrivacy(cbWritePermissions.isChecked());
                            if (spnReadPrivacy.getSelectedItemPosition() == 0) {
                                vidTrain.setReadPrivacy(false);
                            } else {
                                vidTrain.setReadPrivacy(true);
                            }

                            vidTrain.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    successfullySavedVidTrain();
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

    public void successfullySavedVidTrain() {
        Toast.makeText(getBaseContext(), "Successfully saved vidtrain",
                Toast.LENGTH_SHORT).show();
        this.finish();
    }
}
