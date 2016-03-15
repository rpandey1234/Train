package com.franklinho.vidtrain_android.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.models.DynamicVideoPlayerView;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.franklinho.vidtrain_android.utilities.VideoPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by franklinho on 3/15/16.
 */
public class VideoPagerAdapter extends PagerAdapter {

    Context mContext;
    LayoutInflater mLayoutInflater;
    List<File> videoFiles = new ArrayList<>();
    public List<View> pagerViews = new ArrayList<>();

    public VideoPagerAdapter(Context context, List<File> videoFiles) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
        this.videoFiles = videoFiles;
    }

    // Returns the number of pages to be displayed in the ViewPager.
    @Override
    public int getCount() {
        return videoFiles.size();
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
        View itemView = mLayoutInflater.inflate(R.layout.pager_item_video, container, false);
        // Find and populate data into the page (i.e set the image)
        final ImageView ivThumbnail = (ImageView) itemView.findViewById(R.id.ivThumbnail);
        final DynamicVideoPlayerView vvPreview = (DynamicVideoPlayerView) itemView.findViewById(R.id.vvPreview);
        // ...
        // Add the page to the container
        container.addView(itemView);
        final File videoFile = videoFiles.get(position);
        ivThumbnail.setImageBitmap(Utility.getImageBitmap(videoFile.getPath()));
        ivThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivThumbnail.setVisibility(View.GONE);
                VideoPlayer.playVideo(vvPreview, videoFile.getPath());

            }
        });

        // Return the page
        pagerViews.add(itemView);
        return itemView;
    }

    // Removes the page from the container for the given position.
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

}