package com.franklinho.vidtrain_android.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.models.User;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A view with an image and the author's profile picture in the bottom right.
 */
public class ImageAttribution extends FrameLayout {

    @Bind(R.id.ivThumbnail) ImageView _ivThumbnail;
    @Bind(R.id.ivUserPic) ImageView _ivUserPic;
    @Bind(R.id.usersSeen) LinearLayout _usersSeen;
    @Bind(R.id.usersUnseen) LinearLayout _usersUnseen;

    private Context _context;

    public ImageAttribution(Context context) {
        this(context, null);
    }

    public ImageAttribution(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageAttribution(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        _context = getContext();
        LayoutInflater.from(_context).inflate(R.layout.image_attribution, this, true);
        ButterKnife.bind(this);
    }

    public void bind(String imageUrl, String userUrl) {
        Glide.with(_context).load(imageUrl).into(_ivThumbnail);
        Glide.with(_context).load(userUrl).into(_ivUserPic);
    }

    public void showUnseenUsers(List<User> users) {
        for (User user : users) {
            View profileImage = LayoutInflater.from(_context)
                    .inflate(R.layout.profile_image, this, false);
            RoundedImageView ivUserPic = (RoundedImageView) profileImage
                    .findViewById(R.id.ivProfileCollaborator);
            Glide.with(_context).load(user.getProfileImageUrl()).into(ivUserPic);
            _usersUnseen.addView(profileImage);
        }
    }

    public void showSeenUsers(List<User> users) {
        for (User user : users) {
            View profileImage = LayoutInflater.from(_context)
                    .inflate(R.layout.profile_image, this, false);
            RoundedImageView ivUserPic = (RoundedImageView) profileImage
                    .findViewById(R.id.ivProfileCollaborator);
            Glide.with(_context).load(user.getProfileImageUrl()).into(ivUserPic);
            _usersSeen.addView(profileImage);
        }
        _usersSeen.setVisibility(VISIBLE);
    }
}
