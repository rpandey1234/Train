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
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.utilities.EndlessRecyclerViewScrollListener;
import com.franklinho.vidtrain_android.utilities.VideoPlayer;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by rahul on 3/5/16.
 */
public class VidTrainListFragment extends Fragment {

    @Bind(R.id.rvVidTrains) public RecyclerView rvVidTrains;
    @Bind(R.id.swipeContainer) SwipeRefreshLayout swipeContainer;
    @Bind(R.id.pbProgessAction) View pbProgressAction;
    @Bind(R.id.tvNotFollowingLabel) TextView tvNotFollowingLabel;

    List<VidTrain> vidTrains;
    VidTrainArrayAdapter aVidTrains;
    LinearLayoutManager linearLayoutManager;

    public VidTrainListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vidTrains = new ArrayList<>();
        //Construct the adapter
        aVidTrains = new VidTrainArrayAdapter(vidTrains, getContext(), getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_conversations, container, false);
        ButterKnife.bind(this, v);

        linearLayoutManager = new LinearLayoutManager(getContext());
        rvVidTrains.setAdapter(aVidTrains);
        rvVidTrains.setLayoutManager(linearLayoutManager);
        swipeContainer.setColorSchemeResources(R.color.bluePrimary);
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

    public void requestVidTrains(final boolean newTimeline) {}

    @Override
    public void onResume() {
        super.onResume();
        aVidTrains.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        // we have to stop any playback in onStop
        VideoPlayer.getVideoPlayer().resetMediaPlayer();
    }

    public void showProgressBar() {
        // Show progress item
        pbProgressAction.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        // Hide progress item
        pbProgressAction.setVisibility(View.GONE);
    }
}
