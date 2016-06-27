package com.franklinho.vidtrain_android.adapters.holders;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.io.Resources;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.Resource;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.activities.VidTrainDetailActivity;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.models.Video;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.makeramen.roundedimageview.RoundedImageView;
import com.parse.ParseUser;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ConversationViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

    @Bind(R.id.conversation_title) TextView _conversationTitle;
    @Bind(R.id.video_count) TextView _videoCount;
    @Bind(R.id.timestamp) TextView _timestamp;
    @Bind(R.id.participants_rv) RecyclerView _rvParticipants;
    @Bind(R.id.image_preview) ImageView _videoImagePreview;

    private final Activity _activity;
    private Context _context;
    public VidTrain _vidTrain;

    public ConversationViewHolder(View view, Activity activity) {
        super(view);
        ButterKnife.bind(this, view);
        _context = view.getContext();
        _activity = activity;
        view.setOnClickListener(this);
    }

    public void bind(VidTrain vidTrain) {
        _vidTrain = vidTrain;
        _conversationTitle.setText(vidTrain.getTitle());
        int videoCount = _vidTrain.getVideosCount();
        _videoCount.setText(_context.getResources()
                .getQuantityString(R.plurals.videos_count, videoCount, videoCount));
        _timestamp.setText(Utility.getRelativeTime(vidTrain.getCreatedAt().getTime()));
        _rvParticipants.setLayoutManager(new LinearLayoutManager(_context, LinearLayoutManager.HORIZONTAL, false));
        _rvParticipants.setAdapter(new ParticipantsAdapter());
        List<Video> videos = _vidTrain.getVideos();
        Video lastVideo = videos.get(videos.size() - 1);
        Glide.with(_context).load(lastVideo.getThumbnail().getUrl()).into(_videoImagePreview);
    }

    @Override
    public void onClick(View v) {
        if (_vidTrain != null) {
            Intent i = new Intent(_context, VidTrainDetailActivity.class);
            i.putExtra(VidTrainDetailActivity.VIDTRAIN_KEY, _vidTrain.getObjectId());
            _context.startActivity(i);
        }
    }

    public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantViewHolder> {

        public ParticipantsAdapter() {}

        @Override
        public ParticipantViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(_context).inflate(R.layout.participant, parent, false);
            return new ParticipantViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ParticipantViewHolder holder, int position) {
            ParseUser parseUser = _vidTrain.getCollaborators().get(position);
            holder.bind(parseUser);
        }

        @Override
        public int getItemCount() {
            List<ParseUser> collaborators = _vidTrain.getCollaborators();
            if (collaborators != null) {
                return collaborators.size();
            } else {
                return 0;
            }
        }
    }

    public class ParticipantViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        @Bind(R.id.user_image) RoundedImageView userImage;

        public ParticipantViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void bind(ParseUser parseUser) {
            userImage.setOval(true);
            Glide.with(_context).load(User.getProfileImageUrl(parseUser)).into(userImage);
        }

        @Override
        public void onClick(View v) {
            if (_vidTrain != null) {
                Intent i = new Intent(_context, VidTrainDetailActivity.class);
                i.putExtra(VidTrainDetailActivity.VIDTRAIN_KEY, _vidTrain.getObjectId());
                _context.startActivity(i);
            }
        }
    }
}
