package com.franklinho.vidtrain_android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.models.Comment;
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
public class CommentArrayAdapter extends RecyclerView.Adapter<CommentArrayAdapter.CommentViewHolder> {
    private List<Comment> mComments;
    private Context context;
    private VideoPlayerManager<MetaData> mVideoPlayerManager = new SingleVideoPlayerManager(new PlayerItemChangeListener() {
        @Override
        public void onPlayerItemChanged(MetaData metaData) {

        }
    });

    public CommentArrayAdapter(List<Comment> comments, Context context) {
        mComments = comments;
        this.context = context;
    }

    public static class CommentViewHolder extends  RecyclerView.ViewHolder implements View.OnClickListener {
        public Comment comment;
        @Bind(R.id.ivProfileImage)
        ImageView ivProfileImage;
        @Bind(R.id.tvTimeStamp)
        TextView tvTimeStamp;
        @Bind(R.id.tvUsername)
        TextView tvUsername;
        @Bind(R.id.tvBody)
        TextView tvBody;


        private Context context;

        public CommentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            context = itemView.getContext();
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
//            int position = getLayoutPosition();
//            Intent i = new Intent(context, VidTrainDetailActivity.class);
//            i.putExtra("status", status );
//            context.startActivity(i);
        }


    }

    @Override
    public CommentArrayAdapter.CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item_comment, parent, false);
        CommentViewHolder viewHolder = new CommentViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        CommentViewHolder commentViewHolder = (CommentViewHolder) holder;
        configureCommentViewHolder(commentViewHolder, position);

    }

    @Override
    public int getItemCount() {
        return mComments.size();
//        return 5;
    }

    private void configureCommentViewHolder(CommentViewHolder holder, int position) {
        final Comment comment = mComments.get(position);
        holder.comment = comment;

        ImageView ivProfileImage = holder.ivProfileImage;
        TextView tvTimeStamp = holder.tvTimeStamp;
        TextView tvUsername = holder.tvUsername;
        TextView tvBody = holder.tvBody;


    }
}
