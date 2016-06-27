package com.franklinho.vidtrain_android.models;

import android.content.Context;
import android.util.AttributeSet;

import com.volokh.danylo.video_player_manager.ui.VideoPlayerView;

/**
 * An {@link android.widget.ImageView} layout that maintains a consistent width to height aspect ratio.
 */
public class DynamicVideoPlayerView extends VideoPlayerView {

    private double _heightRatio;

    public DynamicVideoPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicVideoPlayerView(Context context) {
        super(context);
    }

    public void setHeightRatio(double ratio) {
        if (ratio != _heightRatio) {
            _heightRatio = ratio;
            requestLayout();
        }
    }

    public double getHeightRatio() {
        return _heightRatio;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (_heightRatio > 0.0) {
            // set the image views size
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = (int) (width * _heightRatio);
            setMeasuredDimension(width, height);
        }
        else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

}