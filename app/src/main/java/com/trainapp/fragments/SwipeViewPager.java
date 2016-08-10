package com.trainapp.fragments;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * A viewpager which can be configured to disallow swiping, and callback when a user has
 * finished their tap.
 * Reference for detecting click/swipe:
 * http://stackoverflow.com/questions/9965695/how-to-distinguish-between-move-and-click-in-ontouchevent
 */
public class SwipeViewPager extends ViewPager {

    /**
     * Max allowed distance to move during a "click", in DP.
     */
    private static final int MAX_CLICK_DISTANCE = 15;

    private boolean _isPagingEnabled = true;
    private NextVideoListener _nextVideoListener;
    private float _pressedX;
    private float _pressedY;
    private boolean _stayedWithinClickDistance;
    private Context _context;

    public SwipeViewPager(Context context) {
        super(context);
        init(context);
    }

    public SwipeViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        _context = context;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!_isPagingEnabled) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    _pressedX = event.getX();
                    _pressedY = event.getY();
                    _stayedWithinClickDistance = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (_stayedWithinClickDistance && distance(_pressedX, _pressedY, event.getX(),
                            event.getY()) > MAX_CLICK_DISTANCE) {
                        _stayedWithinClickDistance = false;
                    }
                    // Return false here so view pager does not move the current page
                    return false;
                case MotionEvent.ACTION_UP:
                    if (_nextVideoListener != null && _stayedWithinClickDistance) {
                        _nextVideoListener.onNextVideo(getCurrentItem());
                    }
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    public void setPagingEnabled(boolean pagingEnabled) {
        _isPagingEnabled = pagingEnabled;
    }

    private float distance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        float distanceInPx = (float) Math.sqrt(dx * dx + dy * dy);
        return pxToDp(distanceInPx);
    }

    private float pxToDp(float px) {
        return px / _context.getResources().getDisplayMetrics().density;
    }

    public void setNextVideoListener(NextVideoListener nextVideoListener) {
        _nextVideoListener = nextVideoListener;
    }

    public interface NextVideoListener {

        void onNextVideo(final int position);
    }
}
