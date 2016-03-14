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
import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
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
    @Bind(R.id.tvVidtrains) TextView tvVidtrains;
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
        query.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                tvName.setText(User.getName(user));
                Glide.with(getContext()).load(User.getProfileImageUrl(user)).into(ivProfileImage);
                queryUserCounts(user);
            }
        });
        return view;
    }

    private void queryUserCounts(ParseUser user) {
        ParseQuery<ParseObject> vidTrainQuery = ParseQuery.getQuery("VidTrain");
        vidTrainQuery.whereEqualTo("user", user);
        vidTrainQuery.countInBackground(new CountCallback() {
            @Override
            public void done(int count, ParseException e) {
                String vidtrainsCreated = getResources().getQuantityString(
                        R.plurals.vidtrains_count, count, count);
                tvVidtrains.setText(vidtrainsCreated);
            }
        });

        ParseQuery<ParseObject> videoQuery = ParseQuery.getQuery("Video");
        videoQuery.whereEqualTo("user", user);
        videoQuery.countInBackground(new CountCallback() {
            @Override
            public void done(int count, ParseException e) {
                String videosCreated = getResources().getQuantityString(
                        R.plurals.profile_videos_count, count, count);
                tvVideos.setText(videosCreated);
            }
        });
    }

    public static UserInfoFragment newInstance(String userId) {
        UserInfoFragment userInfoFragment = new UserInfoFragment();
        Bundle args = new Bundle();
        args.putString(ProfileActivity.USER_ID, userId);
        userInfoFragment.setArguments(args);
        return userInfoFragment;
    }
}
