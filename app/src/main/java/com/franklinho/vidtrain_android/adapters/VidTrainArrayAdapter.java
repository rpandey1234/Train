package com.franklinho.vidtrain_android.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.adapters.holders.VidTrainViewHolder;
import com.franklinho.vidtrain_android.models.DynamicHeightVideoPlayerManagerView;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.volokh.danylo.video_player_manager.ui.SimpleMainThreadMediaPlayerListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by franklinho on 3/7/16.
 */
public class VidTrainArrayAdapter extends RecyclerView.Adapter<VidTrainViewHolder> {
    private List<VidTrain> mVidTrains;
    private Context context;

    public VidTrainArrayAdapter( List<VidTrain> vidTrains, Context context) {
        mVidTrains = vidTrains;
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
    public void onBindViewHolder(final VidTrainViewHolder holder, int position) {
        final VidTrain vidTrain = mVidTrains.get(position);
        holder.vidTrain = vidTrain;

        final ImageView ivCollaborators = holder.ivCollaborators;
        final DynamicHeightVideoPlayerManagerView vvPreview = holder.vvPreview;
        ImageButton ibtnLike = holder.ibtnLike;
        TextView tvLikeCount = holder.tvLikeCount;
        TextView tvTitle = holder.tvTitle;
        TextView tvVideoCount = holder.tvVideoCount;
        TextView tvTime = holder.tvTime;

        tvTitle.setText(vidTrain.getTitle());
        int videoCount = vidTrain.getVideosCount();
        String totalVideos = context.getResources().getQuantityString(R.plurals.videos_count,
                videoCount, videoCount);
        tvVideoCount.setText(totalVideos);
        tvTime.setText(Utility.getRelativeTime(vidTrain.getUpdatedAt().getTime()));

        ivCollaborators.setImageResource(0);

        final ParseUser user = vidTrain.getUser();
        user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (object == null) {
                    return;
                }
                String profileImageUrl = User.getProfileImageUrl((ParseUser) object);
                Glide.with(holder.context).load(profileImageUrl).into(ivCollaborators);
            }
        });

        vvPreview.setHeightRatio(1);
        vvPreview.setVisibility(View.VISIBLE);

        final ParseFile parseFile = ((ParseFile) vidTrain.get("thumbnail"));

        parseFile.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                try {
                    final File videoFile = VidtrainApplication.getOutputMediaFile(vidTrain.getObjectId());
                    FileOutputStream out;

                    out = new FileOutputStream(videoFile);
                    out.write(data);
                    out.close();

                    vvPreview.addMediaPlayerListener(new SimpleMainThreadMediaPlayerListener() {
                        @Override
                        public void onVideoCompletionMainThread() {
                            Toast.makeText(
                                    holder.context,
                                    "Video has been prepared for: " + vidTrain.getTitle(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                    vvPreview.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            VidtrainApplication
                                    .getVideoPlayerInstance()
                                    .playNewVideo(null,
                                            vvPreview,
                                            videoFile.getPath());
                        }
                    });

                } catch (FileNotFoundException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    Log.d("TAG", "Error: " + e1.toString());
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mVidTrains.size();
    }
}
