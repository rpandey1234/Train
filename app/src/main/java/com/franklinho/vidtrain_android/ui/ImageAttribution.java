package com.franklinho.vidtrain_android.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.franklinho.vidtrain_android.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A view with an image and the author's profile picture in the bottom right.
 */
public class ImageAttribution extends FrameLayout {

    @Bind(R.id.ivThumbnail) ImageView _ivThumbnail;
    @Bind(R.id.ivUserPic) ImageView _ivUserPic;

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
        LayoutInflater.from(getContext()).inflate(R.layout.image_attribution, this, true);
        ButterKnife.bind(this);
    }

    public void bind(String imageUrl, String userUrl) {
        Glide.with(getContext()).load(imageUrl).into(_ivThumbnail);
        Glide.with(getContext()).load(userUrl).into(_ivUserPic);
    }
}
