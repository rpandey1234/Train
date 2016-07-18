package com.franklinho.vidtrain_android.adapters.holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.models.User;
import com.makeramen.roundedimageview.RoundedImageView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A view holder for a row in the list of friends
 */
public class FriendViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.ivProfileImage) RoundedImageView _friendImage;
    @Bind(R.id.tvName) TextView _friendName;

    private Context _context;

    public FriendViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        _context = view.getContext();
    }

    public void bind(User user) {
        _friendName.setText(user.getName());
        Glide.with(_context).load(user.getProfileImageUrl()).placeholder(
                R.drawable.profile_icon).into(_friendImage);
    }
}
