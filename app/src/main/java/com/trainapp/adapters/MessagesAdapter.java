package com.trainapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trainapp.R;
import com.trainapp.adapters.holders.MessagesViewHolder;
import com.trainapp.fragments.VideoPageFragment;
import com.trainapp.fragments.VidtrainLandingFragment;
import com.trainapp.models.User;
import com.trainapp.models.VideoModel;
import com.trainapp.models.VidtrainMessage;
import com.trainapp.networking.VidtrainApplication;
import com.trainapp.ui.VideoPreview;

import java.util.List;

public class MessagesAdapter extends
        RecyclerView.Adapter<MessagesViewHolder> {

    private List<VidtrainMessage> _vidtrainMessages;
    private Context _context;
    private VidtrainLandingFragment _vidtrainLandingFragment;

    public MessagesAdapter(Context context, List<VidtrainMessage> vidtrainMessages, VidtrainLandingFragment fragment) {
        _vidtrainMessages = vidtrainMessages;
        _context = context;
        _vidtrainLandingFragment = fragment;
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
        VidtrainMessage vidtrainMessage = _vidtrainMessages.get(position);
        final VideoModel video = vidtrainMessage.getVideoModel();

        String currentUserId = User.getCurrentUser().getObjectId();
        holder._videoPreview.setFromCurrentUser(currentUserId.equals(video.getUserId()));
        holder._videoPreview.setOnThumbnailClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(VidtrainApplication.TAG, "Landing Fragment: clicked on video preview");
                VideoPageFragment _videoPageFragment = VideoPageFragment.newInstance(video);
                // TODO: opening animation
                _vidtrainLandingFragment.getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.childFragment, _videoPageFragment)
                        .addToBackStack(null)
                        .commit();
                _vidtrainLandingFragment._videoPlaying = true;
                _vidtrainLandingFragment._childFragment.setVisibility(View.VISIBLE);
            }
        });
        holder._videoPreview.bind(video);

        if (vidtrainMessage.getSeenUsers() != null) {
            holder._videoPreview.addSeenUsers(vidtrainMessage.getSeenUsers());
        }
        if (vidtrainMessage.getUnseenUsers() != null) {
            holder._videoPreview.addUnseenUsers(vidtrainMessage.getSeenUsers());
        }

    }

    @Override
    public int getItemCount() {
        return _vidtrainMessages.size();
    }
}
