package com.franklinho.vidtrain_android.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.models.DynamicHeightImageView;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.franklinho.vidtrain_android.utilities.VideoIconPagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoPagerUrlAdapter extends PagerAdapter {

    Context _context;
    LayoutInflater _layoutInflater;
    List<String> _videoUrls = new ArrayList<>();
    Map<Integer, View> _positionMap = new HashMap<>();

    public VideoPagerUrlAdapter(Context context, List<String> videoFiles) {
        _context = context;
        _layoutInflater = LayoutInflater.from(_context);
        _videoUrls = videoFiles;
    }

    public View getView(int position) {
        return _positionMap.get(position);
    }

    // Returns the number of pages to be displayed in the ViewPager.
    @Override
    public int getCount() {
        return _videoUrls.size();
    }

    // Returns true if a particular object (page) is from a particular page
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    // This method should create the page for the given position passed to it as an argument.
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // Inflate the layout for the page
        View itemView = _layoutInflater.inflate(R.layout.pager_item_video, container, false);
        if (position == 0) {
            // no left padding
            // TODO(rahul): set margin instead of 0 padding so we show the background
            itemView.setPadding(0, itemView.getPaddingTop(), itemView.getPaddingRight(),
                    itemView.getPaddingBottom());
        }
        if (position == getCount() - 1) {
            // no right padding
            itemView.setPadding(itemView.getPaddingLeft(), itemView.getPaddingTop(), 0,
                    itemView.getPaddingBottom());
        }
        // Find and populate data into the page (i.e set the image)
        DynamicHeightImageView ivThumbnail = (DynamicHeightImageView) itemView.findViewById(R.id.ivThumbnail);
        // Add the page to the container
        container.addView(itemView);
        ivThumbnail.setImageDrawable(_context.getDrawable(R.drawable.icon_vidtrain));
        _positionMap.put(position, itemView);
        // Return the page
        return itemView;
    }

    // Removes the page from the container for the given position.
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
