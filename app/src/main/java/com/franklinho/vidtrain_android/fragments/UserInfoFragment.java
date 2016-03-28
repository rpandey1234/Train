package com.franklinho.vidtrain_android.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.activities.ProfileActivity;
import com.franklinho.vidtrain_android.models.User;
import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by rahul on 3/5/16.
 */
public class UserInfoFragment extends Fragment {

    @Bind(R.id.ivProfileImage) ImageView ivProfileImage;
    @Bind(R.id.tvName) TextView tvName;
    @Bind(R.id.tvVidtrains) TextView tvVidtrains;
    @Bind(R.id.tvVideos) TextView tvVideos;
    @Bind(R.id.btnFollow) Button btnFollow;

    private ParseUser currentUser;
    private ParseUser profileUser;
    private boolean isFollowing;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater i, ViewGroup container, Bundle savedInstanceState) {
        View view = i.inflate(R.layout.fragment_user_info, container, false);
        ButterKnife.bind(this, view);
        currentUser = ParseUser.getCurrentUser();
        final String currentUserId = currentUser.getObjectId();
        final String userId = getArguments().getString(ProfileActivity.USER_ID);
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", userId);
        query.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                tvName.setText(User.getName(user));
                Glide.with(getContext()).load(User.getProfileImageUrl(user)).into(ivProfileImage);
                queryUserCounts(user);
                profileUser = user;
                if (!currentUserId.equals(userId)) {
                    btnFollow.setVisibility(View.VISIBLE);
                    isFollowing = User.isFollowing(currentUser, profileUser);
                    if (isFollowing) {
                        btnFollow.setText(R.string.unfollow);
                    } else {
                        btnFollow.setText(R.string.follow);
                    }
                }
            }
        });

        return view;
    }

    @OnClick(R.id.btnFollow)
    public void onFollowClicked(View view) {
        List<ParseUser> updatedFollowingList = isFollowing ? User.unfollow(currentUser, profileUser)
                : User.maybeInitAndAdd(currentUser, profileUser);
        currentUser.put("following", updatedFollowingList);
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                isFollowing = !isFollowing;
                // TODO: animate this
                btnFollow.setText(isFollowing ? R.string.unfollow : R.string.follow);
                sendFollowedNotification(profileUser);

            }
        });
    }

    private void queryUserCounts(ParseUser user) {
        ParseQuery<ParseObject> vidTrainQuery = ParseQuery.getQuery("VidTrain");
        vidTrainQuery.whereEqualTo("user", user);
        // TODO(rahul): handle case when user quickly taps on profile, then away --> could crash
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

    public void sendFollowedNotification(ParseUser user) {
        ParseQuery pushQuery = ParseInstallation.getQuery();
        pushQuery.whereEqualTo("user", user.getObjectId());
        String currentUserName = currentUser.getString("name");
        String alertString = currentUserName + " has just followed you";
        JSONObject data = new JSONObject();

        try {
            data.put("alert", alertString);
            data.put("title", "VidTrain");
            data.put("userId", currentUser.getObjectId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ParsePush push = new ParsePush();
        push.setQuery(pushQuery);
        push.setData(data);
        push.sendInBackground();

    }
}
