package com.franklinho.vidtrain_android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.franklinho.vidtrain_android.activities.ProfileActivity;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by rahul on 3/13/16.
 */
public class UserCreationsFragment extends VidTrainListFragment {

    String userId;

    public static UserCreationsFragment newInstance(String userId) {
        UserCreationsFragment userCreationsFragment = new UserCreationsFragment();
        Bundle args = new Bundle();
        args.putString(ProfileActivity.USER_ID, userId);
        userCreationsFragment.setArguments(args);
        return userCreationsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        userId = getArguments().getString(ProfileActivity.USER_ID);
        requestVidTrains(true);
        return v;
    }

    @Override
    public void requestVidTrains(final boolean newTimeline) {
        super.requestVidTrains(newTimeline);
        final int currentSize;
        if (newTimeline) {
            vidTrains.clear();
            currentSize = 0;
        } else {
            currentSize = vidTrains.size();
        }

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", userId);
        query.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                queryUserVidtrains(newTimeline, currentSize, user);
            }
        });
    }

    private void queryUserVidtrains(
            final boolean newTimeline, final int currentSize, ParseUser user) {
        ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.addDescendingOrder("createdAt");
        query.setSkip(currentSize);
        query.whereEqualTo("user", user);
        query.setLimit(5);
        query.findInBackground(new FindCallback<VidTrain>() {
            @Override
            public void done(List<VidTrain> objects, ParseException e) {
                swipeContainer.setRefreshing(false);
                if (e == null) {
                    vidTrains.addAll(objects);
                    if (newTimeline) {
                        aVidTrains.notifyDataSetChanged();
                    } else {
                        aVidTrains.notifyItemRangeInserted(currentSize, vidTrains.size() - 1);
                    }
                }
            }
        });
    }
}
