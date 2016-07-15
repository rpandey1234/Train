package com.franklinho.vidtrain_android.models;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * An {@link android.widget.ImageView} layout that maintains a consistent width to height aspect ratio.
 */
public class DynamicHeightImageView extends ImageView {

    private double _heightRatio;

    public DynamicHeightImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicHeightImageView(Context context) {
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
            int height = MeasureSpec.getSize(heightMeasureSpec);
            int width = (int) (height * _heightRatio);
            setMeasuredDimension(width, height);
        }
        else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

}