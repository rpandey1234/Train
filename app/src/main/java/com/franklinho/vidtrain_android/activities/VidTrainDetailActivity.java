package com.franklinho.vidtrain_android.activities;

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
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.volokh.danylo.video_player_manager.manager.PlayerItemChangeListener;
import com.volokh.danylo.video_player_manager.manager.SingleVideoPlayerManager;
import com.volokh.danylo.video_player_manager.manager.VideoPlayerManager;
import com.volokh.danylo.video_player_manager.meta.MetaData;
import com.volokh.danylo.video_player_manager.ui.SimpleMainThreadMediaPlayerListener;

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

                    ((ParseFile) vidTrain.get("thumbnail")).getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] data, ParseException e) {

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
}
