package com.franklinho.vidtrain_android.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.adapters.holders.VidTrainViewHolder;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by franklinho on 3/7/16.
 */
public class VidTrainArrayAdapter extends RecyclerView.Adapter<VidTrainViewHolder> {

    private List<VidTrain> mVidTrains;
    private Context mContext;
    private Activity activity;

    public VidTrainArrayAdapter( List<VidTrain> vidTrains, Context context, Activity activity) {
        mVidTrains = vidTrains;
        mContext = context;
        this.activity = activity;
    }

    @Override
    public VidTrainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_vidtrain_image_preview, parent, false);
        return new VidTrainViewHolder(view, activity);
    }

    @Override
    public void onBindViewHolder(final VidTrainViewHolder holder, int position) {
        VidTrain vidTrain = mVidTrains.get(position);
        holder.vidTrain = vidTrain;
        holder.tvTitle.setText(vidTrain.getTitle());
        int videoCount = vidTrain.getVideosCount();
        String totalVideos = mContext.getResources().getQuantityString(R.plurals.videos_count,
                videoCount, videoCount);
//        holder.tvVideoCount.setText(totalVideos);

        holder.btnWatchVideos.setText(String.format("View %s", totalVideos));
//        holder.tvTime.setText(Utility.getRelativeTime(vidTrain.getCreatedAt().getTime()));
        holder.ivCollaborators.setImageResource(0);
        holder.liked = false;
        holder.ibtnLike.setImageResource(R.drawable.heart_icon);
        if (User.hasLikedVidtrain(ParseUser.getCurrentUser(), vidTrain.getObjectId())){
            holder.liked = true;
            holder.ibtnLike.setImageResource(R.drawable.heart_icon_red);
        }

//        holder.tvLikeCount.setText(mContext.getResources().getQuantityString(R.plurals.likes_count,
//                vidTrain.getLikes(), vidTrain.getLikes()));
        holder.tvLikeCount.setText(String.valueOf(vidTrain.getLikes()));
        final ParseUser user = vidTrain.getUser();
        user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (object == null) {
                    return;
                }
                String profileImageUrl = User.getProfileImageUrl((ParseUser) object);
                Glide.with(holder.context).load(profileImageUrl).into(holder.ivCollaborators);
            }
        });

        holder.vpPreview.setAdapter(new ImagePagerAdapter(holder.context, vidTrain.getVideos()));
        holder.cpIndicator.setViewPager(holder.vpPreview);
        int dpRadius = (int) holder.context.getResources().getDisplayMetrics().density * 15;
        holder.cpIndicator.setRadius(dpRadius);
        int dpWidth = (int) holder.context.getResources().getDisplayMetrics().density * 2;
        holder.cpIndicator.setStrokeWidth(dpWidth);
        holder.cpIndicator.invalidate();
        holder.cpIndicator.requestLayout();
    }

    @Override
    public int getItemCount() {
        return mVidTrains.size();
    }
}
