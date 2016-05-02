package com.franklinho.vidtrain_android.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.Video;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by franklinho on 3/15/16.
 */
public class ImagePagerAdapter extends PagerAdapter {

    Activity mActivity;
    Context mContext;
    LayoutInflater mLayoutInflater;
    List<Video> videos = new ArrayList<>();

    public ImagePagerAdapter(Context context, List<Video> videos, Activity activity) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
        this.videos = videos;
        mActivity = activity;
    }

    // Returns the number of pages to be displayed in the ViewPager.
    @Override
    public int getCount() {
        return videos.size();
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
        View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);
        // Find and populate data into the page (i.e set the image)
        final ImageView imageView = (ImageView) itemView.findViewById(R.id.ivPagerImage);
        // ...
        // Add the page to the container
        container.addView(itemView);
        final Video video = videos.get(position);
        video.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (video.getThumbnail() != null && mContext != null) {
                    Glide.with(mContext).load(video.getThumbnail().getUrl()).into(imageView);
                }
            }
        });

        // Return the page
        return itemView;
    }

    public void setUserImageAtPosition(int position, final ImageView ivCollaborators) {
        final Video video = videos.get(position);
        video.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                video.getUser().fetchInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject user, ParseException e) {
                        final String profileImageUrl = User.getProfileImageUrl((ParseUser) user);
                        if (profileImageUrl != null && mContext != null) {
                            Glide.with(mContext).load(profileImageUrl).into(ivCollaborators);
                        }
                    }
                });
            }
        });
    }

    // Removes the page from the container for the given position.
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}