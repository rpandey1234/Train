package com.franklinho.vidtrain_android.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.franklinho.vidtrain_android.activities.ProfileActivity;
import com.franklinho.vidtrain_android.fragments.FollowingFragment;
import com.franklinho.vidtrain_android.fragments.MapFragment;
import com.franklinho.vidtrain_android.fragments.PopularFragment;
import com.franklinho.vidtrain_android.fragments.UserCreationsFragment;

/**
 * Created by rahul on 3/31/16.
 */
public class ProfileFragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {

    private String tabTitles[] = new String[]{"Vidtrains", "Following"};
    private Context context;
    private String userId;

    public ProfileFragmentPagerAdapter(FragmentManager fm, Context context, String userId) {
        super(fm);
        this.context = context;
        this.userId = userId;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return UserCreationsFragment.newInstance(userId);
        } else {
            return FollowingFragment.newInstance(userId);
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }
}
