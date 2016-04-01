package com.franklinho.vidtrain_android.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.activities.ProfileActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by rahul on 3/31/16.
 */
public class UserContainerFragment extends Fragment {

    @Bind(R.id.tabHost) FragmentTabHost fragmentTabHost;
    String userId;

    public UserContainerFragment() {}

    public static UserContainerFragment newInstance(String userId) {
        UserContainerFragment userContainerFragment = new UserContainerFragment();
        Bundle args = new Bundle();
        args.putString(ProfileActivity.USER_ID, userId);
        userContainerFragment.setArguments(args);
        return userContainerFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_container_fragment, container, false);
        ButterKnife.bind(this, view);
        userId = getArguments().getString(ProfileActivity.USER_ID);
        fragmentTabHost.setup(getContext(), getFragmentManager(), R.id.tabContent);

        Bundle args = new Bundle();
        args.putString(ProfileActivity.USER_ID, userId);

        fragmentTabHost.addTab(fragmentTabHost.newTabSpec("meTab").setIndicator("Vidtrains"),
                UserCreationsFragment.class, args);
        fragmentTabHost.addTab(fragmentTabHost.newTabSpec("followTab").setIndicator("Following"),
                FollowingFragment.class, args);
        return view;
    }
}
