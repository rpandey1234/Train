package com.franklinho.vidtrain_android.adapters;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.franklinho.vidtrain_android.fragments.VideoPageFragment;
import com.franklinho.vidtrain_android.models.Video;

import java.util.ArrayList;
import java.util.List;

/**
 * The fragment-backed pager which handles stopping/playing video.
 */
public class VideoFragmentPagerAdapter extends FragmentPagerAdapter {

    private Context _context;
    private List<Video> _videos = new ArrayList<>();

    public VideoFragmentPagerAdapter(FragmentManager fm, Context context, List<Video> videos) {
        super(fm);
        _context = context;
        _videos = videos;
    }

    @Override
    public VideoPageFragment getItem(int position) {
        return VideoPageFragment.newInstance(_videos.get(position));
    }

    @Override
    public int getCount() {
        return _videos.size();
    }
}
