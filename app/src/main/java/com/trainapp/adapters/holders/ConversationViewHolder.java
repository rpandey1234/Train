package com.trainapp.adapters.holders;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.trainapp.R;
import com.trainapp.models.User;
import com.trainapp.models.VidTrain;
import com.trainapp.models.Video;
import com.trainapp.utilities.Utility;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ConversationViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

    @Bind(R.id.conversation_title) TextView _conversationTitle;
    @Bind(R.id.timestamp) TextView _timestamp;
    @Bind(R.id.participants_rv) RecyclerView _rvParticipants;
    @Bind(R.id.image_preview) ImageView _videoImagePreview;
    @Bind(R.id.card_view) CardView _cardView;

    private Context _context;
    public VidTrain _vidTrain;

    public ConversationViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        _context = view.getContext();
        view.setOnClickListener(this);
    }

    public void bind(VidTrain vidTrain, int numUnseen) {
        int colorId = android.R.color.white;
        if (numUnseen > 0) {
            colorId = R.color.cardBackground;
        }
        _cardView.setCardBackgroundColor(_context.getResources().getColor(colorId));
        _vidTrain = vidTrain;
        _conversationTitle.setText(vidTrain.getTitle());
        _timestamp.setText(Utility.getRelativeTime(vidTrain.getUpdatedAt().getTime()));
        _rvParticipants.setLayoutManager(
                new LinearLayoutManager(_context, LinearLayoutManager.HORIZONTAL, false));
        _rvParticipants.setAdapter(new ParticipantsAdapter());
        Video lastVideo = _vidTrain.getLatestVideo();
        Glide.with(_context).load(lastVideo.getThumbnail().getUrl()).into(_videoImagePreview);
    }

    @Override
    public void onClick(View v) {
        if (_vidTrain != null) {
            Utility.goVidtrainDetail(_context, _vidTrain.getObjectId());
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
            User parseUser = _vidTrain.getCollaborators().get(position);
            holder.bind(parseUser);
        }

        @Override
        public int getItemCount() {
            List<User> collaborators = _vidTrain.getCollaborators();
            if (collaborators != null) {
                return collaborators.size();
            } else {
                return 0;
            }
        }
    }

    public class ParticipantViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        @Bind(R.id.user_image) RoundedImageView _userImage;

        public ParticipantViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void bind(User user) {
            _userImage.setOval(true);
            Glide.with(_context).load(user.getProfileImageUrl()).into(_userImage);
        }

        @Override
        public void onClick(View v) {
            if (_vidTrain != null) {
                Utility.goVidtrainDetail(_context, _vidTrain.getObjectId());
            }
        }
    }
}
