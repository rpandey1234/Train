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
        _timeLeft.setText(getResources().getString(R.string.time_left, videoModel.getTimeLeft()));
    }

    public void addUnseenUsers(List<User> users) {
        if (users == null) {
            return;
        }
        for (User user : users) {
            View profileImage = LayoutInflater.from(_context)
                    .inflate(R.layout.profile_image, this, false);
            RoundedImageView ivUserPic = (RoundedImageView) profileImage
                    .findViewById(R.id.ivProfileCollaborator);
            Glide.with(_context).load(user.getProfileImageUrl()).into(ivUserPic);
            _usersUnseen.addView(profileImage);
        }
    }

    public void addSeenUsers(List<User> users) {
        if (users == null) {
            return;
        }
        for (User user : users) {
            View profileImage = LayoutInflater.from(_context)
                    .inflate(R.layout.profile_image, this, false);
            RoundedImageView ivUserPic = (RoundedImageView) profileImage
                    .findViewById(R.id.ivProfileCollaborator);
            Glide.with(_context).load(user.getProfileImageUrl()).into(ivUserPic);
            _usersSeen.addView(profileImage);
        }
        if (!users.isEmpty()) {
            _usersSeen.setVisibility(VISIBLE);
        }
    }

    public void setFromCurrentUser(boolean fromCurrentUser) {
        RelativeLayout.LayoutParams layoutIvThumbnail =
                (RelativeLayout.LayoutParams) _ivThumbnail.getLayoutParams();
        RelativeLayout.LayoutParams layoutTimeLeft =
                (RelativeLayout.LayoutParams) _timeLeft.getLayoutParams();
        RelativeLayout.LayoutParams layoutSeen =
                (RelativeLayout.LayoutParams) _usersSeen.getLayoutParams();
        RelativeLayout.LayoutParams layoutUnseen =
                (RelativeLayout.LayoutParams) _usersUnseen.getLayoutParams();
        if (fromCurrentUser) {
            layoutIvThumbnail.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutTimeLeft.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            // remove existing rule
            layoutIvThumbnail.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            layoutTimeLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);

            layoutSeen.addRule(RelativeLayout.LEFT_OF, R.id.ivThumbnail);
            layoutSeen.addRule(RelativeLayout.RIGHT_OF, 0);

            layoutUnseen.addRule(RelativeLayout.LEFT_OF, R.id.ivThumbnail);
            layoutUnseen.addRule(RelativeLayout.RIGHT_OF, 0);
        }
    }
}
