package com.franklinho.vidtrain_android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.adapters.holders.VidTrainViewHolder;
import com.franklinho.vidtrain_android.models.DynamicHeightVideoPlayerManagerView;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.volokh.danylo.video_player_manager.manager.VideoPlayerManager;
import com.volokh.danylo.video_player_manager.meta.MetaData;
import com.volokh.danylo.video_player_manager.ui.SimpleMainThreadMediaPlayerListener;

import java.util.List;

/**
 * Created by franklinho on 3/7/16.
 */
public class VidTrainArrayAdapter extends RecyclerView.Adapter<VidTrainViewHolder> {
    private List<VidTrain> mVidTrains;
    private Context context;
    private VideoPlayerManager<MetaData> mVideoPlayerManager;


    public VidTrainArrayAdapter(VideoPlayerManager videoPlayerManager, List<VidTrain> vidTrains, Context context) {
        mVidTrains = vidTrains;
        mVideoPlayerManager = videoPlayerManager;
        this.context = context;
    }



    @Override
    public VidTrainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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

    private void configureVidTrainViewHolder(final VidTrainViewHolder holder, int position) {
        final VidTrain vidTrain = mVidTrains.get(position);
        holder.vidTrain = vidTrain;

        final ImageView ivCollaborators = holder.ivCollaborators;
        final DynamicHeightVideoPlayerManagerView vvPreview = holder.vvPreview;
        ImageButton ibtnLike = holder.ibtnLike;
        TextView tvLikeCount = holder.tvLikeCount;
        TextView tvCommentCount = holder.tvCommentCount;

        ivCollaborators.setImageResource(0);

        vidTrain.getUser().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                String profileImageUrl = ((ParseUser) vidTrain.getUser()).getString("profileImageUrl");
                Glide.with(holder.context).load(profileImageUrl).into(ivCollaborators);
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

        holder.vidTrain.mVideoPlayerManager = mVideoPlayerManager;
        holder.vidTrain.mDirectUrl = ((ParseFile) vidTrain.get("thumbnail")).getUrl();

//        mVideoPlayerManager.playNewVideo(null, vvPreview, ((ParseFile) vidTrain.get("thumbnail")).getUrl());
    }
}
