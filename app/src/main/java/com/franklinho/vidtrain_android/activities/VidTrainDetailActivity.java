package com.franklinho.vidtrain_android.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.models.DynamicHeightVideoPlayerManagerView;
import com.franklinho.vidtrain_android.models.VidTrain;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vid_train_detail);
        ButterKnife.bind(this);

        vvPreview.setHeightRatio(1);
    }
}
