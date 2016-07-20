package com.franklinho.vidtrain_android.fragments;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * A viewpager which can be configured to disallow swiping
 */
public class SwipeViewPager extends ViewPager {

    private boolean _isPagingEnabled = true;

    public SwipeViewPager(Context context) {
        super(context);
    }

    public SwipeViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        return _isPagingEnabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return _isPagingEnabled && super.onTouchEvent(event);
    }

    public void setPagingEnabled(boolean pagingEnabled) {
        _isPagingEnabled = pagingEnabled;
    }
}
