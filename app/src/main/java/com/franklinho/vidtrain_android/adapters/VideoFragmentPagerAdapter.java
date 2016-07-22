package com.franklinho.vidtrain_android.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.franklinho.vidtrain_android.fragments.VideoPageFragment;
import com.franklinho.vidtrain_android.fragments.VidtrainLandingFragment;
import com.franklinho.vidtrain_android.models.VidTrain;
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
    private VidTrain _vidTrain;
    private List<Video> _videos = new ArrayList<>();
    private Map<Integer, VideoPageFragment> _fragmentMap;

    public VideoFragmentPagerAdapter(FragmentManager fm, Context context, List<Video> videos,
            VidTrain vidTrain) {
        super(fm);
        _context = context;
        _vidTrain = vidTrain;
                _videos = videos;
        _fragmentMap = new HashMap<>();
    }

    @Override
    public Fragment getItem(int position) {
        if (position < _videos.size()) {
            return VideoPageFragment.newInstance(_videos.get(position));
        } else {
            return VidtrainLandingFragment.newInstance(_vidTrain);
        }
    }

    @Override
    public int getCount() {
        return _videos.size() + 1;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (position < _videos.size()) {
            VideoPageFragment fragment = (VideoPageFragment) super.instantiateItem(container, position);
            _fragmentMap.put(position, fragment);
            return fragment;
        } else {
            return super.instantiateItem(container, position);
        }
    }

    // Need to get a reference to the fragment
    // http://stackoverflow.com/questions/14035090/
    public VideoPageFragment getFragment(int position) {
        if (position < _videos.size()) {
            return _fragmentMap.get(position);
        } else {
            return null;
        }
    }
}
