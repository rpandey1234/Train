package com.trainapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.trainapp.R;
import com.trainapp.adapters.MessagesAdapter.VideoPreviewViewHolder;
import com.trainapp.fragments.VideoPageFragment;
import com.trainapp.fragments.VidtrainLandingFragment;
import com.trainapp.models.User;
import com.trainapp.models.VideoModel;
import com.trainapp.models.VidtrainMessage;
import com.trainapp.ui.VideoPreview;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<VideoPreviewViewHolder> {

    private List<VidtrainMessage> _vidtrainMessages;
    private Context _context;
    private VidtrainLandingFragment _vidtrainLandingFragment;

    public MessagesAdapter(Context context, List<VidtrainMessage> vidtrainMessages,
            VidtrainLandingFragment fragment) {
        _vidtrainMessages = vidtrainMessages;
        _context = context;
        _vidtrainLandingFragment = fragment;
    }

    @Override
    public VideoPreviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        VideoPreview videoPreview = new VideoPreview(_context);
        return new VideoPreviewViewHolder(videoPreview);
    }

    @Override
    public void onBindViewHolder(final VideoPreviewViewHolder holder, int position) {
        holder._videoPreview.prepareForReuse();

        VidtrainMessage vidtrainMessage = _vidtrainMessages.get(position);
        final VideoModel video = vidtrainMessage.getVideoModel();

        String currentUserId = User.getCurrentUser().getObjectId();
        holder._videoPreview.setFromCurrentUser(currentUserId.equals(video.getUserId()));
        holder._videoPreview.setOnThumbnailClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _vidtrainLandingFragment._videoPageFragment = VideoPageFragment.newInstance(video);
                // TODO: opening animation
                _vidtrainLandingFragment.getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.childFragment, _vidtrainLandingFragment._videoPageFragment)
                        .commit();
                _vidtrainLandingFragment._videoPlaying = true;
                _vidtrainLandingFragment.setChildFragmentVisibility(View.VISIBLE);
            }
        });
        holder._videoPreview.bind(video);
        holder._videoPreview.addSeenUsers(vidtrainMessage.getSeenUsers());
        holder._videoPreview.addUnseenUsers(vidtrainMessage.getUnseenUsers());
    }

    @Override
    public int getItemCount() {
        return _vidtrainMessages.size();
    }

    public class VideoPreviewViewHolder extends RecyclerView.ViewHolder {

        public VideoPreview _videoPreview;

        public VideoPreviewViewHolder(VideoPreview videoPreview) {
            super(videoPreview);
            _videoPreview = videoPreview;
        }
    }
}
