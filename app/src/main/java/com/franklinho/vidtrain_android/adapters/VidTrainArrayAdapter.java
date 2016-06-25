package com.franklinho.vidtrain_android.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.adapters.holders.ConversationViewHolder;
import com.franklinho.vidtrain_android.models.VidTrain;

import java.util.List;

public class VidTrainArrayAdapter extends RecyclerView.Adapter<ConversationViewHolder> {

    private List<VidTrain> _vidTrains;
    private Context _context;
    private Activity _activity;

    public VidTrainArrayAdapter( List<VidTrain> vidTrains, Context context, Activity activity) {
        _vidTrains = vidTrains;
        _context = context;
        _activity = activity;
    }

    @Override
    public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(_context).inflate(R.layout.conversation_vidtrain_preview, parent, false);
        return new ConversationViewHolder(view, _activity);
    }

    @Override
    public void onBindViewHolder(final ConversationViewHolder holder, int position) {
        VidTrain vidTrain = _vidTrains.get(position);
        holder.bind(vidTrain);
    }

    @Override
    public int getItemCount() {
        return _vidTrains.size();
    }
}
