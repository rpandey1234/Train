package com.franklinho.vidtrain_android.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.models.DynamicHeightImageView;
import com.franklinho.vidtrain_android.models.Video;
import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoPagerAdapter extends PagerAdapter {

    Context _context;
    LayoutInflater _layoutInflater;
    List<Video> _videos = new ArrayList<>();
    Map<Integer, View> _positionMap = new HashMap<>();

    public VideoPagerAdapter(Context context, List<Video> videos) {
        _context = context;
        _layoutInflater = LayoutInflater.from(_context);
        _videos = videos;
    }

    public View getView(int position) {
        return _positionMap.get(position);
    }

    // Returns the number of pages to be displayed in the ViewPager.
    @Override
    public int getCount() {
        return _videos.size();
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
        // Find and populate data into the page (i.e set the image)
        final DynamicHeightImageView ivThumbnail = (DynamicHeightImageView) itemView.findViewById(R.id.ivThumbnail);
        // Add the page to the container
        container.addView(itemView);
        ParseFile thumbnail = _videos.get(position).getThumbnail();
        thumbnail.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                if (e != null) {
                    Log.d(VidtrainApplication.TAG, e.toString());
                    return;
                }
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                ivThumbnail.setImageBitmap(bitmap);
            }
        });
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
