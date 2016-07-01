package com.franklinho.vidtrain_android.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;

import com.franklinho.vidtrain_android.fragments.VideoPageFragment;
import com.franklinho.vidtrain_android.models.Video;

import java.util.ArrayList;
import java.util.List;

/**
 * The fragment-backed pager which handles stopping/playing video.
 */
public class VideoFragmentPagerAdapter extends FragmentPagerAdapter {

    Context _context;
    LayoutInflater _layoutInflater;
    List<Video> _videos = new ArrayList<>();

    public VideoFragmentPagerAdapter(FragmentManager fm,
            Context context, List<Video> videos) {
        super(fm);
        _context = context;
        _layoutInflater = LayoutInflater.from(_context);
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
