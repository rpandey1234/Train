package com.franklinho.vidtrain_android.fragments;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.utilities.EndlessRecyclerViewScrollListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by rahul on 3/5/16.
 */
public class FollowingFragment extends VidTrainListFragment {

    List<ParseUser> followingList;
    public static FollowingFragment newInstance() {
        return new FollowingFragment();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        showProgressBar();
        followingList = (List<ParseUser>) ParseUser.getCurrentUser().get("following");

        linearLayoutManager = new LinearLayoutManager(getContext());
        rvVidTrains.setLayoutManager(linearLayoutManager);
        rvVidTrains.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                requestVidTrains(false);
            }
        });

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestVidTrains(true);
            }
        });
        requestVidTrains(true);
        return v;
    }

    @Override
    public void requestVidTrains(final boolean newTimeline) {
        super.requestVidTrains(newTimeline);
        if (followingList != null && followingList.size() != 0) {
            tvNotFollowingLabel.setVisibility(View.GONE);
            final int currentSize;
            if (newTimeline) {
                vidTrains.clear();
                currentSize = 0;
            } else {
                currentSize = vidTrains.size();
            }

            ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
            query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE
            );

            query.whereContainedIn("user", followingList);
            query.orderByDescending("rankingValue");
            query.addDescendingOrder("createdAt");
            query.setSkip(currentSize);
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
                            // TODO: should be objects.size()
                            aVidTrains.notifyItemRangeInserted(currentSize, vidTrains.size() - 1);
                        }
                    }
                    hideProgressBar();
                }
            });
        } else {
            hideProgressBar();
            tvNotFollowingLabel.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        aVidTrains.notifyItemRangeChanged(0, vidTrains.size());
    }
}
