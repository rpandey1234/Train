package com.franklinho.vidtrain_android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.adapters.holders.VidTrainViewHolder;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.volokh.danylo.video_player_manager.manager.SingleVideoPlayerManager;

import java.io.File;
import java.util.List;

/**
 * Created by franklinho on 3/7/16.
 */
public class VidTrainArrayAdapter extends RecyclerView.Adapter<VidTrainViewHolder> {

    private final SingleVideoPlayerManager mPlayer;
    private List<VidTrain> mVidTrains;
    private Context mContext;


    public VidTrainArrayAdapter( List<VidTrain> vidTrains, Context context) {
        mVidTrains = vidTrains;
        mContext = context;
        mPlayer = VidtrainApplication.getVideoPlayer();
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
        holder.vvPreview.setVisibility(View.VISIBLE);

        final File videoFile = Utility.getOutputMediaFile(vidTrain.getObjectId());
        if (videoFile == null) {
            return;
        }
        vidTrain.getLatestVideo().getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                if (e != null) {
                    Log.d(VidtrainApplication.TAG, e.toString());
                    return;
                }
                Utility.writeToFile(data, videoFile);
                holder.vvPreview.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (mPlayer.getCurrentPlayerState()) {
                            case STARTED:
                                mPlayer.stopAnyPlayback();
                                break;
                            default:
                                mPlayer.playNewVideo(null, holder.vvPreview, videoFile.getPath());
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return mVidTrains.size();
    }
}
