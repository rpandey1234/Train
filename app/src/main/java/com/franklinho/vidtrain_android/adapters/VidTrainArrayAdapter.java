package com.franklinho.vidtrain_android.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.activities.VidTrainDetailActivity;
import com.franklinho.vidtrain_android.models.DynamicHeightVideoPlayerManagerView;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.volokh.danylo.video_player_manager.manager.PlayerItemChangeListener;
import com.volokh.danylo.video_player_manager.manager.SingleVideoPlayerManager;
import com.volokh.danylo.video_player_manager.manager.VideoPlayerManager;
import com.volokh.danylo.video_player_manager.meta.MetaData;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by franklinho on 3/7/16.
 */
public class VidTrainArrayAdapter extends RecyclerView.Adapter<VidTrainArrayAdapter.VidTrainViewHolder> {
    private List<VidTrain> mVidTrains;
    private Context context;
    private VideoPlayerManager<MetaData> mVideoPlayerManager = new SingleVideoPlayerManager(new PlayerItemChangeListener() {
        @Override
        public void onPlayerItemChanged(MetaData metaData) {

        }
    });

    public VidTrainArrayAdapter(List<VidTrain> vidTrains, Context context) {
        mVidTrains = vidTrains;
        this.context = context;
    }

    public static class VidTrainViewHolder extends  RecyclerView.ViewHolder implements View.OnClickListener {
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

        private Context context;

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
            context.startActivity(i);
        }


    }

    @Override
    public VidTrainArrayAdapter.VidTrainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item_vidtrain, parent, false);
        VidTrainViewHolder viewHolder = new VidTrainViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(VidTrainViewHolder holder, int position) {
        VidTrainViewHolder vidTrainViewHolder = (VidTrainViewHolder) holder;
        configureVidTrainViewHolder( vidTrainViewHolder, position);

    }

    @Override
    public int getItemCount() {
        return mVidTrains.size();
//        return 5;
    }

    private void configureVidTrainViewHolder(VidTrainViewHolder holder, int position) {
        final VidTrain vidTrain = mVidTrains.get(position);
        holder.vidTrain = vidTrain;

        ImageView ivCollaborators = holder.ivCollaborators;
        DynamicHeightVideoPlayerManagerView vvPreview = holder.vvPreview;
        ImageButton ibtnLike = holder.ibtnLike;
        TextView tvLikeCount = holder.tvLikeCount;
        TextView tvCommentCount = holder.tvCommentCount;

        vvPreview.setHeightRatio(1);
    }
}
