package com.trainapp.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.trainapp.R;
import com.trainapp.models.User;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A view which simply contains a user image
 */
public class Participant extends LinearLayout {

    @Bind(R.id.user_image) RoundedImageView _userImage;

    private final Context _context;

    public Participant(Context context) {
        this(context, null);
    }

    public Participant(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Participant(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        _context = context;
        init();
    }

    protected void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.participant, this, true);
        ButterKnife.bind(this);
    }

    public void bind(User user) {
        _userImage.setOval(true);
        Glide.with(_context).load(user.getProfileImageUrl()).into(_userImage);
    }
}
