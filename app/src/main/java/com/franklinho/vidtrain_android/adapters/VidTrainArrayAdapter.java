package com.franklinho.vidtrain_android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.adapters.holders.ConversationViewHolder;
import com.franklinho.vidtrain_android.models.Unseen;
import com.franklinho.vidtrain_android.models.VidTrain;

import java.util.List;

public class VidTrainArrayAdapter extends RecyclerView.Adapter<ConversationViewHolder> {

    private List<VidTrain> _vidtrains;
    private final List<Unseen> _unseens;
    private Context _context;

    public VidTrainArrayAdapter(List<VidTrain> vidtrains, List<Unseen> unseens, Context context) {
        _vidtrains = vidtrains;
        _unseens = unseens;
        _context = context;
    }

    @Override
    public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(_context).inflate(R.layout.conversation_vidtrain_preview,
                parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ConversationViewHolder holder, int position) {
        VidTrain vidtrain = _vidtrains.get(position);
        Unseen unseen = Unseen.getUnseenWithVidtrain(_unseens, vidtrain);
        int unread;
        if (unseen == null) {
            unread = 0;
        } else {
            unread = unseen.getUnseenVideos().size();
        }
        holder.bind(vidtrain, unread);
    }

    @Override
    public int getItemCount() {
        return _vidtrains.size();
    }
}
