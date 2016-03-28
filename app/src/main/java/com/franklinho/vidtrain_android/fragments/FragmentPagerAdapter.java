package com.franklinho.vidtrain_android.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {
    private String tabTitles[] = new String[]{"Map", "Popular", "Following"};
    private Context context;

    public FragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return MapFragment.newInstance();
        } else if (position == 1){
            return PopularFragment.newInstance();
        } else {
            return FollowingFragment.newInstance();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
