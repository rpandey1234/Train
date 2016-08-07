package com.trainapp.fragments;

import android.util.Log;
import android.view.View;

import com.trainapp.models.Unseen;
import com.trainapp.models.User;
import com.trainapp.models.VidTrain;
import com.trainapp.networking.VidtrainApplication;
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
    public void requestVidTrains(final int numItems) {
        if (numItems == 0) {
            _vidtrains.clear();
            _unseens.clear();
            _aVidtrains.notifyDataSetChanged();
        }

        ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.addDescendingOrder("updatedAt");
        query.include(VidTrain.COLLABORATORS);
        query.include(VidTrain.VIDEOS_KEY);
        query.setSkip(numItems);
        query.setLimit(PAGE_SIZE);
        query.findInBackground(new FindCallback<VidTrain>() {
            @Override
            public void done(final List<VidTrain> vidtrains, ParseException e) {
                _swipeContainer.setRefreshing(false);
                if (e != null) {
                    Log.e(VidtrainApplication.TAG, e.toString());
                }
                ParseQuery<Unseen> unseenQuery = ParseQuery.getQuery("Unseen");
                unseenQuery.whereEqualTo(Unseen.USER_KEY, User.getCurrentUser());
                unseenQuery.addDescendingOrder("updatedAt");
                unseenQuery.findInBackground(new FindCallback<Unseen>() {
                    @Override
                    public void done(List<Unseen> unseens, ParseException e) {
                        if (e != null) {
                            Log.d(VidtrainApplication.TAG, e.toString());
                        }
                        _unseens.addAll(unseens);
                        _vidtrains.addAll(vidtrains);
                        _aVidtrains.notifyDataSetChanged();
                        if (_vidtrains.size() == 0) {
                            _tvNoConversations.setVisibility(View.VISIBLE);
                        } else {
                            _tvNoConversations.setVisibility(View.GONE);
                        }
                    }
                });
                hideProgressBar();
            }
        });
    }
}
