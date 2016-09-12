package com.trainapp.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.trainapp.fragments.VideoPageFragment;
import com.trainapp.fragments.VidtrainLandingFragment;
import com.trainapp.models.VidTrain;
import com.trainapp.models.Video;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The fragment-backed pager which handles stopping/playing video, and adds
 * a summary page at the end from which to add videos.
 */
public class VideoFragmentPagerAdapter extends FragmentPagerAdapter {

    private VidTrain _vidTrain;
    private List<Video> _videos;
    private Map<Integer, VideoPageFragment> _fragmentMap;
    private VidtrainLandingFragment _landingFragment;
    private Context _context;

    public VideoFragmentPagerAdapter(
            FragmentManager fm, List<Video> videos, VidTrain vidTrain, Context context) {
        super(fm);
        _vidTrain = vidTrain;
        _videos = videos;
        _context = context;
        _fragmentMap = new HashMap<>();
    }

    @Override
    public Fragment getItem(int position) {
        if (position < _videos.size()) {
            return VideoPageFragment.newInstance(_videos.get(position));
        } else {
            return VidtrainLandingFragment.newInstance(_vidTrain, _context);
        }
    }

    @Override
    public int getCount() {
        return _videos.size() + 1;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object object = super.instantiateItem(container, position);
        if (position < _videos.size()) {
            _fragmentMap.put(position, (VideoPageFragment) object);
        } else {
            _landingFragment = (VidtrainLandingFragment) object;
        }
        return object;
    }

    /**
     * Need a custom unique identifier (rather than just position) since we remove videos once
     * they are viewed. If we don't have this, going back to landing fragment after watching unseen
     * videos causes a crash since instantiateItem will return a video fragment instead of the
     * landing fragment.
     */
    @Override
    public long getItemId(int position) {
        if (position < _videos.size()) {
            return _videos.get(position).getObjectId().hashCode();
        } else {
            return super.getItemId(position);
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

    public VidtrainLandingFragment getLandingFragment() {
        return _landingFragment;
    }
}
