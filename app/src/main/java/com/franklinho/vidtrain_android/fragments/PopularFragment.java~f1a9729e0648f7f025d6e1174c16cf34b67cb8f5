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
        PopularFragment popularFragment = new PopularFragment();
        return popularFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        requestVidTrains(true, 0);
        return v;
    }

    public void requestVidTrains(final boolean newTimeline, int skip) {
        final int currentSize = vidTrains.size();
        ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
        query.addDescendingOrder("createdAt");
        query.setSkip(skip);
        query.findInBackground(new FindCallback<VidTrain>() {
            @Override
            public void done(List<VidTrain> objects, ParseException e) {
                if (e == null) {
                    vidTrains.addAll(objects);
                    if (newTimeline == false) {
                        aVidTrains.notifyItemRangeInserted(currentSize, vidTrains.size() - 1);
                    } else {
                        aVidTrains.notifyDataSetChanged();
                    }
                }
            }
        });
    }
}
