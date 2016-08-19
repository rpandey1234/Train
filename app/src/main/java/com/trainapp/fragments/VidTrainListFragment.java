package com.trainapp.fragments;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trainapp.R;
import com.trainapp.adapters.VidTrainArrayAdapter;
import com.trainapp.models.Unseen;
import com.trainapp.models.VidTrain;
import com.trainapp.utilities.EndlessRecyclerViewScrollListener;
import com.trainapp.utilities.Utility;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class VidTrainListFragment extends Fragment {

    @Bind(R.id.rvVidTrains) public RecyclerView _rvVidTrains;
    @Bind(R.id.swipeContainer) SwipeRefreshLayout _swipeContainer;
    @Bind(R.id.progressBar) View _pbProgressAction;
    @Bind(R.id.tvNoConversations) TextView _tvNoConversations;

    List<VidTrain> _vidtrains;
    List<Unseen> _unseens;
    VidTrainArrayAdapter _aVidtrains;
    LinearLayoutManager _linearLayoutManager;

    public VidTrainListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _vidtrains = new ArrayList<>();
        _unseens = new ArrayList<>();
        _aVidtrains = new VidTrainArrayAdapter(_vidtrains, _unseens, getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View v = inflater.inflate(R.layout.fragment_conversations, container, false);
        ButterKnife.bind(this, v);

        _linearLayoutManager = new LinearLayoutManager(getContext());
        _rvVidTrains.setAdapter(_aVidtrains);
        _rvVidTrains.setLayoutManager(_linearLayoutManager);
        _rvVidTrains.setHasFixedSize(true);
        _swipeContainer.setColorSchemeResources(R.color.colorPrimary);
        showProgressBar();

        _rvVidTrains.addOnScrollListener(
                new EndlessRecyclerViewScrollListener(_linearLayoutManager) {
                    @Override
                    public void onLoadMore(int page, int totalItemsCount) {
                        requestVidTrains(totalItemsCount);
                    }
                });

        _swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestVidTrains(0);
            }
        });
        requestVidTrains(0);
        clearNotifications();
        return v;
    }

    public void requestVidTrains(final int numItems) {}

    public void showProgressBar() {
        _pbProgressAction.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        _pbProgressAction.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // This is needed to show the most up-to-date info with seen information.
        // TODO: this causes us to lose scroll position. 
        requestVidTrains(0);
    }

    private void clearNotifications() {
        Utility.setBadgeCount(getContext(), 0);
        NotificationManager notificationManager =
                (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }
}
