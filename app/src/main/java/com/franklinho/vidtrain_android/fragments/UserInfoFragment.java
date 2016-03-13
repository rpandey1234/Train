package com.franklinho.vidtrain_android.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.activities.ProfileActivity;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by rahul on 3/5/16.
 */
public class UserInfoFragment extends Fragment {

    @Bind(R.id.ivProfileImage) ImageView ivProfileImage;
    @Bind(R.id.tvName) TextView tvName;
    @Bind(R.id.tvVideos) TextView tvVideos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater i, ViewGroup container, Bundle savedInstanceState) {
        View view = i.inflate(R.layout.fragment_user_info, container, false);
        ButterKnife.bind(this, view);
        String userId = getArguments().getString(ProfileActivity.USER_ID);
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", userId);
        query.include("videos");
        query.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.d(VidtrainApplication.TAG, e.toString());
                    return;
                }
                tvName.setText(User.getName(user));
                Glide.with(getContext()).load(User.getProfileImageUrl(user)).into(ivProfileImage);
                int videoCount = User.getVideoCount(user);
                String videosCreated = getResources().getQuantityString(R.plurals.videos_count,
                        videoCount, videoCount);
                tvVideos.setText(videosCreated);
            }
        });
        return view;
    }

    public static UserInfoFragment newInstance(String userId) {
        UserInfoFragment userInfoFragment = new UserInfoFragment();
        Bundle args = new Bundle();
        args.putString(ProfileActivity.USER_ID, userId);
        userInfoFragment.setArguments(args);
        return userInfoFragment;
    }
}
