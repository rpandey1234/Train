package com.franklinho.vidtrain_android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.franklinho.vidtrain_android.models.VidTrain;
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
            currentSize  = vidTrains.size();
        }

        ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
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
                        aVidTrains.notifyItemRangeInserted(currentSize, vidTrains.size() - 1);
                    }
                }
            }
        });

    }
}
