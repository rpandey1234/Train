package com.franklinho.vidtrain_android.fragments;

import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;

public class ConversationsFragment extends VidTrainListFragment {

    public static final int PAGE_SIZE = 10;
    // If true, this will fetch all vidtrains regardless of whether the viewer is involved in it
    public static final boolean DISREGARD_PRIVACY = true;

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
                if (e == null) {
                    List<VidTrain> visibleVidtrains = objects;
                    if (!DISREGARD_PRIVACY) {
                        // TODO: really should be using ACLs for this
                        visibleVidtrains = Utility.filterVisibleVidtrains(objects);
                    }
                    _vidTrains.addAll(visibleVidtrains);
                    _aVidTrains.notifyDataSetChanged();
                }
                hideProgressBar();
            }
        });
    }
}
