package com.trainapp.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.trainapp.R;
import com.trainapp.models.User;
import com.trainapp.models.VideoModel;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A view with an image and the author's profile picture in the bottom right.
 */
public class VideoPreview extends FrameLayout {

    @Bind(R.id.ivThumbnail) ImageView _ivThumbnail;
    @Bind(R.id.ivUserPic) ImageView _ivUserPic;
    @Bind(R.id.usersSeen) LinearLayout _usersSeen;
    @Bind(R.id.usersUnseen) LinearLayout _usersUnseen;
    @Bind(R.id.timeLeft) TextView _timeLeft;

    private Context _context;

    public VideoPreview(Context context) {
        this(context, null);
    }

    public VideoPreview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        _context = getContext();
        LayoutInflater.from(_context).inflate(R.layout.video_preview, this, true);
        ButterKnife.bind(this);
    }

    public void bind(VideoModel videoModel) {
        Glide.with(_context).load(videoModel.getThumbnailUrl()).into(_ivThumbnail);
        Glide.with(_context).load(videoModel.getUserUrl()).into(_ivUserPic);
        _timeLeft.setText(videoModel.getTimeLeft(getResources()));
    }

    public void addUnseenUsers(List<User> users) {
        updateViewForUsers(_usersUnseen, users);
    }

    public void addSeenUsers(List<User> users) {
        updateViewForUsers(_usersSeen, users);
    }

    private void updateViewForUsers(LinearLayout usersView, List<User> users) {
        if (users == null) {
            return;
        }
        for (User user : users) {
            View img = LayoutInflater.from(_context).inflate(R.layout.profile_image, this, false);
            RoundedImageView ivUserPic = (RoundedImageView) img.findViewById(R.id.ivProfileCollaborator);
            Glide.with(_context).load(user.getProfileImageUrl()).into(ivUserPic);
            usersView.addView(img);
        }
        usersView.setVisibility(users.isEmpty() ? GONE : VISIBLE);
    }

    public void setFromCurrentUser(boolean fromCurrentUser) {
        RelativeLayout.LayoutParams layoutIvThumbnail =
                (RelativeLayout.LayoutParams) _ivThumbnail.getLayoutParams();
        RelativeLayout.LayoutParams layoutTimeLeft =
                (RelativeLayout.LayoutParams) _timeLeft.getLayoutParams();
        if (fromCurrentUser) {
            alignParentRight(layoutIvThumbnail);
            alignParentRight(layoutTimeLeft);
        } else {
            alignParentLeft(layoutIvThumbnail);
            alignParentLeft(layoutTimeLeft);
        }
    }

    private void alignParentRight(RelativeLayout.LayoutParams layoutParams) {
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
    }

    private void alignParentLeft(RelativeLayout.LayoutParams layoutParams) {
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
    }

    public void setOnThumbnailClick(OnClickListener onThumbnailClick) {
        _ivThumbnail.setOnClickListener(onThumbnailClick);
    }

    public void prepareForReuse() {
        _ivThumbnail.setImageResource(0);
        _ivUserPic.setImageResource(0);
        _timeLeft.setText("");
        _usersSeen.removeAllViews();
        _usersUnseen.removeAllViews();
    }
}
