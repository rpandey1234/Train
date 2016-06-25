package com.franklinho.vidtrain_android.fragments;

import com.franklinho.vidtrain_android.models.VidTrain;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;

public class ConversationsFragment extends VidTrainListFragment {

    public static final int NUM_CONVERSATIONS_INITIAL = 10;

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
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE
        );
        query.addDescendingOrder("createdAt");
        query.include("collaborators");
        query.include("videos");
        query.setSkip(currentSize);
        query.setLimit(NUM_CONVERSATIONS_INITIAL);
        query.findInBackground(new FindCallback<VidTrain>() {
            @Override
            public void done(List<VidTrain> objects, ParseException e) {
                _swipeContainer.setRefreshing(false);
                if (e == null) {
                    _vidTrains.addAll(objects);
                    if (newTimeline) {
                        _aVidTrains.notifyDataSetChanged();
                    } else {
                        // TODO: should be objects.size()
                        _aVidTrains.notifyItemRangeInserted(currentSize, _vidTrains.size() - 1);
                    }
                }
                hideProgressBar();
            }
        });
    }
}
