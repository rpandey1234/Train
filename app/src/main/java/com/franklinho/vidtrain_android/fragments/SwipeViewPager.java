package com.franklinho.vidtrain_android.fragments;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * A viewpager which can be configured to disallow swiping, and callback when a user has
 * finished their tap.
 */
public class SwipeViewPager extends ViewPager {

    private boolean _isPagingEnabled = true;
    private NextVideoListener _nextVideoListener;

    public SwipeViewPager(Context context) {
        super(context);
    }

    public SwipeViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!_isPagingEnabled) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    return false;
                case MotionEvent.ACTION_UP:
                    if (_nextVideoListener != null) {
                        _nextVideoListener.onNextVideo(getCurrentItem());
                    }
                    return false;
            }
        }
        return super.onTouchEvent(event);
    }

    public void setPagingEnabled(boolean pagingEnabled) {
        _isPagingEnabled = pagingEnabled;
    }

    public void setNextVideoListener(NextVideoListener nextVideoListener) {
        _nextVideoListener = nextVideoListener;
    }

    public interface NextVideoListener {

        void onNextVideo(final int position);
    }
}
