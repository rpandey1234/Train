package com.franklinho.vidtrain_android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.adapters.holders.VidTrainViewHolder;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.franklinho.vidtrain_android.utilities.VideoPlayer;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by franklinho on 3/7/16.
 */
public class VidTrainArrayAdapter extends RecyclerView.Adapter<VidTrainViewHolder> {

    private List<VidTrain> mVidTrains;
    private Context mContext;
    private Map<String, Boolean> likesMap;

    public VidTrainArrayAdapter( List<VidTrain> vidTrains, Context context) {
        mVidTrains = vidTrains;
        mContext = context;
        likesMap = User.getLikes(ParseUser.getCurrentUser());
    }

    @Override
    public VidTrainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_vidtrain, parent, false);
        return new VidTrainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final VidTrainViewHolder holder, int position) {
        VidTrain vidTrain = mVidTrains.get(position);
        holder.vidTrain = vidTrain;
        holder.tvTitle.setText(vidTrain.getTitle());
        int videoCount = vidTrain.getVideosCount();
        String totalVideos = mContext.getResources().getQuantityString(R.plurals.videos_count,
                videoCount, videoCount);
        holder.tvVideoCount.setText(totalVideos);
        holder.tvTime.setText(Utility.getRelativeTime(vidTrain.getUpdatedAt().getTime()));
        holder.ivCollaborators.setImageResource(0);
        holder.liked = false;
        holder.ibtnLike.setImageResource(R.drawable.heart_icon);
        if (User.hasLikedVidtrain(ParseUser.getCurrentUser(), vidTrain.getObjectId())){
            holder.liked = true;
            holder.ibtnLike.setImageResource(R.drawable.heart_icon_red);
        }

        holder.tvLikeCount.setText(mContext.getResources().getQuantityString(R.plurals.likes_count,
                vidTrain.getLikes(), vidTrain.getLikes()));
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

        holder.vvPreview.setHeightRatio(1);
        final File videoFile = Utility.getOutputMediaFile(vidTrain.getObjectId());
        if (videoFile == null) {
            return;
        }
        vidTrain.getLatestVideo().getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                if (e == null) {
                    Utility.writeToFile(data, videoFile);
                    holder.vvThumbnail.setImageBitmap(Utility.getImageBitmap(videoFile.getPath()));
                    holder.vvThumbnail.setOnClickListener(
                            new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    holder.vvThumbnail.setVisibility(View.GONE);
                                    VideoPlayer.playVideo(holder.vvPreview, videoFile.getPath());
                                }
                            }
                    );
                    holder.vvPreview.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            switch (VideoPlayer.getState()) {
                                case STARTED:
                                    VideoPlayer.stop();
                                    break;
                                case PAUSED:
                                default:
                                    VideoPlayer.playVideo(holder.vvPreview, videoFile.getPath());
                            }
                        }
                    });
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mVidTrains.size();
    }
}
