package com.franklinho.vidtrain_android.models;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * An {@link android.widget.ImageView} layout that maintains a consistent width to height aspect ratio.
 */
public class DynamicVideoView extends VideoView {

    private double _heightRatio;

    public DynamicVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setHeightRatio(double ratio) {
        if (ratio != _heightRatio) {
            _heightRatio = ratio;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO(rahul): need better way to display video
        // https://www.jayway.com/2012/12/12/creating-custom-android-views-part-4-measuring-and-how-to-force-a-view-to-be-square/
        if (_heightRatio > 0.0) {
            // set the image views size
            int height = MeasureSpec.getSize(heightMeasureSpec);
            int width = (int) (height * _heightRatio);
            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}