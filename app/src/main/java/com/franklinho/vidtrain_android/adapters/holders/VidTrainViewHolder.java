package com.franklinho.vidtrain_android.adapters.holders;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.activities.VidTrainDetailActivity;
import com.franklinho.vidtrain_android.models.DynamicHeightScalableVideoView;
import com.franklinho.vidtrain_android.models.VidTrain;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by franklinho on 3/10/16.
 */
public class VidTrainViewHolder extends  RecyclerView.ViewHolder implements View.OnClickListener {
    public VidTrain vidTrain;
    @Bind(R.id.ivCollaborators)
    public ImageView ivCollaborators;
    @Bind(R.id.vvPreview)
    public DynamicHeightScalableVideoView vvPreview;
    @Bind(R.id.ibtnLike)
    public ImageButton ibtnLike;
    @Bind(R.id.tvLikeCount)
    public TextView tvLikeCount;
    @Bind(R.id.tvCommentCount)
    public TextView tvCommentCount;

    public  Context context;

    public VidTrainViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        context = itemView.getContext();
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int position = getLayoutPosition();
        Intent i = new Intent(context, VidTrainDetailActivity.class);
        i.putExtra("vidTrain", vidTrain.getObjectId());
        context.startActivity(i);
    }


}