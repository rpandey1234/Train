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
import com.franklinho.vidtrain_android.utilities.Utility;

import java.util.List;

/**
 * Created by franklinho on 3/7/16.
 */
public class VidTrainArrayAdapter extends RecyclerView.Adapter<ConversationViewHolder> {

    private List<VidTrain> mVidTrains;
    private Context mContext;
    private Activity mActivity;

    public VidTrainArrayAdapter( List<VidTrain> vidTrains, Context context, Activity activity) {
        mVidTrains = vidTrains;
        mContext = context;
        mActivity = activity;
    }

    @Override
    public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.conversation_vidtrain_preview, parent, false);
        return new ConversationViewHolder(view, mActivity);
    }

    @Override
    public void onBindViewHolder(final ConversationViewHolder holder, int position) {
        VidTrain vidTrain = mVidTrains.get(position);
        holder.bind(vidTrain);
    }

    @Override
    public int getItemCount() {
        return mVidTrains.size();
    }
}
