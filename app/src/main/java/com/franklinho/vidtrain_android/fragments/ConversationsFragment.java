package com.franklinho.vidtrain_android.fragments;

import android.util.Log;

import com.franklinho.vidtrain_android.BuildConfig;
import com.franklinho.vidtrain_android.models.Unseen;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;

public class ConversationsFragment extends VidTrainListFragment {

    public static final int PAGE_SIZE = 10;

    public static ConversationsFragment newInstance() {
        return new ConversationsFragment();
    }

    @Override
    public void requestVidTrains(final boolean newTimeline) {
        super.requestVidTrains(newTimeline);
        final int currentSize;
        if (newTimeline) {
            _vidTrains.clear();
            currentSize = 0;
        } else {
            currentSize = _vidTrains.size();
        }

        ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.addDescendingOrder("updatedAt");
        query.include("collaborators");
        query.include("videos");
        query.setSkip(currentSize);
        query.setLimit(PAGE_SIZE);
        query.findInBackground(new FindCallback<VidTrain>() {
            @Override
            public void done(List<VidTrain> objects, ParseException e) {
                _swipeContainer.setRefreshing(false);
                if (e != null) {
                    Log.e(VidtrainApplication.TAG, e.toString());
                }
                List<VidTrain> visibleVidtrains = objects;
                if (!BuildConfig.DISREGARD_PRIVACY) {
                    // TODO: really should be using ACLs for this
                    visibleVidtrains = Utility.filterVisibleVidtrains(objects);
                }
                ParseQuery<Unseen> unseenQuery = ParseQuery.getQuery("Unseen");
                unseenQuery.whereEqualTo("user", User.getCurrentUser());
                unseenQuery.addDescendingOrder("updatedAt");
                final List<VidTrain> finalVisibleVidtrains = visibleVidtrains;
                unseenQuery.findInBackground(new FindCallback<Unseen>() {
                    @Override
                    public void done(List<Unseen> unseens, ParseException e) {
                        if (e != null) {
                            Log.d(VidtrainApplication.TAG, e.toString());
                        }
                        _unseenList.addAll(unseens);
                        _vidTrains.addAll(finalVisibleVidtrains);
                        _aVidTrains.notifyDataSetChanged();
                    }
                });
                hideProgressBar();
            }
        });
    }
}
