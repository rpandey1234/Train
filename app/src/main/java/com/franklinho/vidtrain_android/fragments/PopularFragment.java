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

import java.util.List;

/**
 * Created by rahul on 3/5/16.
 */
public class PopularFragment extends VidTrainListFragment {

    public static PopularFragment newInstance() {
        return new PopularFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        showProgressBar();

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
        query.orderByDescending("rankingValue");
        query.addDescendingOrder("createdAt");
        query.include("collaborators");
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
    }


    @Override
    public void onResume() {
        super.onResume();
        aVidTrains.notifyItemRangeChanged(0, vidTrains.size());
    }
}
