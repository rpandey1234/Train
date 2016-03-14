package com.franklinho.vidtrain_android.models;

import android.content.Context;
import android.util.AttributeSet;

import com.volokh.danylo.video_player_manager.ui.VideoPlayerView;

/**
 * An {@link android.widget.ImageView} layout that maintains a consistent width to height aspect ratio.
 */
public class DynamicVideoPlayerView extends VideoPlayerView {


    private double mHeightRatio;

    public DynamicVideoPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicVideoPlayerView(Context context) {
        super(context);
    }

    public void setHeightRatio(double ratio) {
        if (ratio != mHeightRatio) {
            mHeightRatio = ratio;
            requestLayout();
        }
    }

    public double getHeightRatio() {
        return mHeightRatio;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mHeightRatio > 0.0) {
            // set the image views size
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = (int) (width * mHeightRatio);
            setMeasuredDimension(width, height);
        }
        else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

}