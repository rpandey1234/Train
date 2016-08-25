package com.trainapp.adapters.holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.trainapp.ui.VideoPreview;

import butterknife.ButterKnife;

public class MessagesViewHolder extends RecyclerView.ViewHolder {
    public VideoPreview _videoPreview;
    private Context _context;

    public MessagesViewHolder(VideoPreview view) {
        super(view);
        _videoPreview = view;
        _context = view.getContext();
        ButterKnife.bind(this, view);
    }
}