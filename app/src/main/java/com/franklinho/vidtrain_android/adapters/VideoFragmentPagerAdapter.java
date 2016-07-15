package com.franklinho.vidtrain_android.adapters;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.franklinho.vidtrain_android.fragments.VideoPageFragment;
import com.franklinho.vidtrain_android.models.Video;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The fragment-backed pager which handles stopping/playing video.
 */
public class VideoFragmentPagerAdapter extends FragmentPagerAdapter {

    private Context _context;
    private List<Video> _videos = new ArrayList<>();
    private Map<Integer, VideoPageFragment> _fragmentMap;

    public VideoFragmentPagerAdapter(FragmentManager fm, Context context, List<Video> videos) {
        super(fm);
        _context = context;
        _videos = videos;
        _fragmentMap = new HashMap<>();
    }

    @Override
    public VideoPageFragment getItem(int position) {
        return VideoPageFragment.newInstance(_videos.get(position));
    }

    @Override
    public int getCount() {
        return _videos.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        VideoPageFragment videoPageFragment = (VideoPageFragment) super.instantiateItem(container,
                position);
        _fragmentMap.put(position, videoPageFragment);
        return videoPageFragment;
    }

    // Need to get a reference to the fragment
    // http://stackoverflow.com/questions/14035090/
    public VideoPageFragment getFragment(int position) {
        return _fragmentMap.get(position);
    }
}
