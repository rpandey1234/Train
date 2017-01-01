package com.trainapp.adapters.holders;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.trainapp.R;
import com.trainapp.models.User;
import com.trainapp.models.VidTrain;
import com.trainapp.models.Video;
import com.trainapp.ui.Participant;
import com.trainapp.utilities.Utility;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ConversationViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

    @Bind(R.id.conversation_title) TextView _conversationTitle;
    @Bind(R.id.timestamp) TextView _timestamp;
    @Bind(R.id.participants) LinearLayout _participants;
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
        if (User.getCurrentUser() == null) {
            return;
        }
        int colorId = android.R.color.white;
        float alpha = 1.0f;
        Video latestVideo = vidTrain.getLatestVideo();
        // After the Dec. 2016 change with deleting older videos (but not deleting the relation
        // from the Unseen object), the unseen highlighting is broken.
//        if (numUnseen > 0) {
//            colorId = R.color.cardBackground;
//        }
        // Check if all videos expired. If so, gray out this conversation
        if (latestVideo == null || latestVideo.isVideoExpired()) {
            alpha = 0.4f;
        }
        _cardView.setCardBackgroundColor(ContextCompat.getColor(_context, colorId));
        _cardView.setAlpha(alpha);
        _vidTrain = vidTrain;
        _conversationTitle.setText(_vidTrain.getGeneratedTitle(_context.getResources()));
        _timestamp.setText(Utility.getRelativeTime(vidTrain.getUpdatedAt().getTime()));
        _participants.removeAllViews();
        // Too many issues with the layout width/clicks when trying to put participants into a
        // (nested) recycler view, e.g. http://stackoverflow.com/questions/26649406.
        // Revert to simply adding views to a linear layout since there should not be many
        // participants in a conversation. Drawback is that this won't scroll
        List<User> collaborators = _vidTrain.getCollaborators();
        for (User user : collaborators) {
            if (User.getCurrentUser().getObjectId().equals(user.getObjectId())
                    && collaborators.size() != 1) {
                // If the user is having a conversation with themselves (size == 1), show their face
                continue;
            }
            Participant participant = new Participant(_context);
            participant.bind(user);
            _participants.addView(participant);
        }
        Glide.with(_context)
                .load(latestVideo != null ? latestVideo.getThumbnail().getUrl() : null)
                .asBitmap()
                .placeholder(R.drawable.ic_placeholder_video)
                .into(_videoImagePreview);
    }

    @Override
    public void onClick(View v) {
        Utility.goVidtrainDetail(_context, _vidTrain.getObjectId());
    }
}
