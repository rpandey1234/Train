package com.franklinho.vidtrain_android.fragments;

import com.franklinho.vidtrain_android.models.VidTrain;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by rahul on 3/5/16.
 */
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
            vidTrains.clear();
            currentSize = 0;
        } else {
            currentSize = vidTrains.size();
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
}
