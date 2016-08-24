package com.trainapp.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.trainapp.R;
import com.trainapp.adapters.holders.MessagesViewHolder;
import com.trainapp.fragments.VideoPageFragment;
import com.trainapp.fragments.VidtrainLandingFragment;
import com.trainapp.models.User;
import com.trainapp.models.VideoModel;
import com.trainapp.ui.VideoPreview;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by franklinho on 8/23/16.
 */
public class MessagesAdapter extends
        RecyclerView.Adapter<MessagesViewHolder> {

    private List<VideoModel> mVideoModels;
    private Context mContext;
    private VidtrainLandingFragment vidtrainLandingFragment;

    public MessagesAdapter(Context context, List<VideoModel> videoModels, VidtrainLandingFragment fragment) {
        mVideoModels = videoModels;
        mContext = context;
        vidtrainLandingFragment = fragment;
    }

    @Override
    public MessagesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        VideoPreview messageView = new VideoPreview(context);

        // Return a new holder instance
        MessagesViewHolder viewHolder = new MessagesViewHolder(messageView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final MessagesViewHolder holder, int position) {
        final VideoModel video = mVideoModels.get(position);

        String currentUserId = User.getCurrentUser().getObjectId();
        holder.videoPreview.setFromCurrentUser(currentUserId.equals(video.getUserId()));
        holder.videoPreview.setOnThumbnailClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Landing Fragment: clicked on video preview");
                VideoPageFragment _videoPageFragment = VideoPageFragment.newInstance(video);
                // TODO: opening animation
                vidtrainLandingFragment.getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.childFragment, _videoPageFragment)
                        .addToBackStack(null)
                        .commit();
                vidtrainLandingFragment._videoPlaying = true;
                vidtrainLandingFragment._childFragment.setVisibility(View.VISIBLE);
            }
        });
        holder.videoPreview.bind(video);


    }

    @Override
    public int getItemCount() {
        return mVideoModels.size();
    }
}