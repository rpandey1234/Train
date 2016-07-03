/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (C) 2012 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.franklinho.vidtrain_android.utilities;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

import com.franklinho.vidtrain_android.R;
import com.viewpagerindicator.PageIndicator;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * This widget implements the dynamic action bar tab behavior that can change
 * across different configurations or circumstances.
 */
public class VideoPageIndicator extends HorizontalScrollView implements PageIndicator {
    public final IcsLinearLayout _iconsLayout;

    private ViewPager _viewPager;
    private OnPageChangeListener _listener;
    private Runnable _iconSelector;
    private int _selectedIndex;

    public VideoPageIndicator(Context context) {
        this(context, null);
    }

    public VideoPageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        setHorizontalScrollBarEnabled(false);

        _iconsLayout = new IcsLinearLayout(context, com.viewpagerindicator.R.attr.vpiIconPageIndicatorStyle);
        addView(_iconsLayout, new LayoutParams(WRAP_CONTENT, MATCH_PARENT, Gravity.CENTER));
    }

    private void animateToIcon(final int position) {
        final View iconView = _iconsLayout.getChildAt(position);

        for (int i = 0; i < _iconsLayout.getChildCount(); i++) {
            View opacityView
                    = _iconsLayout.getChildAt(i);
            if (i != _selectedIndex) {
                opacityView.setAlpha(0.50f);
                opacityView.setBackgroundColor(Color.WHITE);
            } else {
                opacityView.setAlpha(1.0f);
                opacityView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.bluePrimary));
            }
        }


        if (_iconSelector != null) {
            removeCallbacks(_iconSelector);
        }
        _iconSelector = new Runnable() {
            public void run() {
                final int scrollPos = iconView.getLeft() - (getWidth() - iconView.getWidth()) / 2;
                smoothScrollTo(scrollPos, 0);
                _iconSelector = null;
            }
        };
        post(_iconSelector);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (_iconSelector != null) {
            // Re-post the selector we saved
            post(_iconSelector);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (_iconSelector != null) {
            removeCallbacks(_iconSelector);
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        if (_listener != null) {
            _listener.onPageScrollStateChanged(arg0);
        }
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        if (_listener != null) {
            _listener.onPageScrolled(arg0, arg1, arg2);
        }
    }

    @Override
    public void onPageSelected(int arg0) {
        setCurrentItem(arg0);
        if (_listener != null) {
            _listener.onPageSelected(arg0);
        }
    }

    @Override
    public void setViewPager(ViewPager view) {
        if (_viewPager == view) {
            return;
        }
        if (_viewPager != null) {
            _viewPager.setOnPageChangeListener(null);
        }
        PagerAdapter adapter = view.getAdapter();
        if (adapter == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        _viewPager = view;
        view.setOnPageChangeListener(this);
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        _iconsLayout.removeAllViews();
        VideoIconPagerAdapter iconAdapter = (VideoIconPagerAdapter) _viewPager.getAdapter();
        int count = iconAdapter.getCount();
        for (int i = 0; i < count; i++) {
            ImageView view = new ImageView(getContext(), null, com.viewpagerindicator.R.attr.vpiIconPageIndicatorStyle);
            view.setPadding(4, 4, 4, 4);


            int dpOfImage = (int) getResources().getDisplayMetrics().density * 40;
            view.setAdjustViewBounds(true);
            view.setMaxHeight(dpOfImage);
            view.setMaxWidth(dpOfImage);
            if (i != _selectedIndex) {
                view.setAlpha(0.50f);
                view.setBackgroundColor(Color.WHITE);
            } else {
                view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.bluePrimary));
            }
            view.setImageBitmap(iconAdapter.getIconBitMap(i));
            _iconsLayout.addView(view);
        }
        if (_selectedIndex > count) {
            _selectedIndex = count - 1;
        }
        setCurrentItem(_selectedIndex);
        requestLayout();
    }

    @Override
    public void setViewPager(ViewPager view, int initialPosition) {
        setViewPager(view);
        setCurrentItem(initialPosition);
    }

    @Override
    public void setCurrentItem(int item) {
        if (_viewPager == null) {
            throw new IllegalStateException("ViewPager has not been bound.");
        }
        _selectedIndex = item;
        _viewPager.setCurrentItem(item);

        int tabCount = _iconsLayout.getChildCount();
        for (int i = 0; i < tabCount; i++) {
            View child = _iconsLayout.getChildAt(i);
            boolean isSelected = (i == item);
            child.setSelected(isSelected);
            if (isSelected) {
                animateToIcon(item);
            }
        }
    }

    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        _listener = listener;
    }
}
