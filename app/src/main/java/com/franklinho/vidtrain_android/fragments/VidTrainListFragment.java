package com.franklinho.vidtrain_android.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.adapters.VidTrainArrayAdapter;
import com.franklinho.vidtrain_android.models.Unseen;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.utilities.EndlessRecyclerViewScrollListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class VidTrainListFragment extends Fragment {

    @Bind(R.id.rvVidTrains) public RecyclerView _rvVidTrains;
    @Bind(R.id.swipeContainer) SwipeRefreshLayout _swipeContainer;
    @Bind(R.id.progressBar) View _pbProgressAction;
    @Bind(R.id.tvNotFollowingLabel) TextView _tvNotFollowingLabel;

    List<VidTrain> _vidTrains;
    List<Unseen> _unseens;
    VidTrainArrayAdapter _aVidTrains;
    LinearLayoutManager _linearLayoutManager;

    public VidTrainListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _vidTrains = new ArrayList<>();
        _unseens = new ArrayList<>();
        _aVidTrains = new VidTrainArrayAdapter(_vidTrains, _unseens, getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View v = inflater.inflate(R.layout.fragment_conversations, container, false);
        ButterKnife.bind(this, v);

        _linearLayoutManager = new LinearLayoutManager(getContext());
        _rvVidTrains.setAdapter(_aVidTrains);
        _rvVidTrains.setLayoutManager(_linearLayoutManager);
        _rvVidTrains.setHasFixedSize(true);
        _swipeContainer.setColorSchemeResources(R.color.bluePrimary);
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
        return v;
    }

    public void requestVidTrains(final int numItems) {}

    @Override
    public void onResume() {
        super.onResume();
        _aVidTrains.notifyDataSetChanged();
    }

    public void showProgressBar() {
        _pbProgressAction.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        _pbProgressAction.setVisibility(View.GONE);
    }
}
