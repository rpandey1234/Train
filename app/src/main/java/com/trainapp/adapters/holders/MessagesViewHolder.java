package com.trainapp.adapters.holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.trainapp.R;
import com.trainapp.models.User;
import com.trainapp.models.VideoModel;
import com.trainapp.ui.VideoPreview;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by franklinho on 8/23/16.
 */
public class MessagesViewHolder extends RecyclerView.ViewHolder {
    public VideoPreview videoPreview;
    private Context _context;

    public MessagesViewHolder(VideoPreview view) {
        super(view);
        videoPreview = view;
        _context = view.getContext();
        ButterKnife.bind(this, view);
        
    }

    public Context getContext() {
        return _context;
    }

}