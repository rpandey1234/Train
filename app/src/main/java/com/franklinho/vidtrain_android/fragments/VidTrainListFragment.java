package com.franklinho.vidtrain_android.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    @Bind(R.id.pbProgessAction) View pbProgessAction;

    List<VidTrain> vidTrains;
    VidTrainArrayAdapter aVidTrains;
    LinearLayoutManager linearLayoutManager;

//    private final ListItemsVisibilityCalculator mVideoVisibilityCalculator =
//            new SingleListViewItemActiveCalculator(new DefaultSingleItemCalculatorCallback(), vidTrains);
//    private ItemsPositionGetter mItemsPositionGetter;
//    private int mScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

    public VidTrainListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vidTrains = new ArrayList<>();
        //Construct the adapter
        aVidTrains = new VidTrainArrayAdapter(vidTrains, getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_popular, container, false);
        ButterKnife.bind(this, v);

        linearLayoutManager = new LinearLayoutManager(getContext());
        rvVidTrains.setAdapter(aVidTrains);
//        mItemsPositionGetter = new RecyclerViewItemPositionGetter(linearLayoutManager, rvVidTrains);
//        rvVidTrains.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
//                mScrollState = scrollState;
//                if (scrollState == RecyclerView.SCROLL_STATE_IDLE && !vidTrains.isEmpty()) {
//
//                    mVideoVisibilityCalculator.onScrollStateIdle(
//                            mItemsPositionGetter,
//                            linearLayoutManager.findFirstVisibleItemPosition(),
//                            linearLayoutManager.findLastVisibleItemPosition());
//                }
//            }
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                if (!vidTrains.isEmpty()) {
//                    mVideoVisibilityCalculator.onScroll(
//                            mItemsPositionGetter,
//                            linearLayoutManager.findFirstVisibleItemPosition(),
//                            linearLayoutManager.findLastVisibleItemPosition() - linearLayoutManager.findFirstVisibleItemPosition() + 1,
//                            mScrollState);
//                }
//            }
//
//        });

        swipeContainer.setColorSchemeResources(R.color.bluePrimary);
        rvVidTrains.setLayoutManager(linearLayoutManager);


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

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvVidTrains.setAdapter(aVidTrains);
        rvVidTrains.setLayoutManager(linearLayoutManager);

        return v;
    }

    public void requestVidTrains(final boolean newTimeline) {

    }

    @Override
    public void onResume() {
        super.onResume();
//        if(!vidTrains.isEmpty()){
//            // need to call this method from list view handler in order to have filled list
//
//            rvVidTrains.post(new Runnable() {
//                @Override
//                public void run() {
//
//                    mVideoVisibilityCalculator.onScrollStateIdle(
//                            mItemsPositionGetter,
//                            linearLayoutManager.findFirstVisibleItemPosition(),
//                            linearLayoutManager.findLastVisibleItemPosition());
//
//                }
//            });
//        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // we have to stop any playback in onStop
        VideoPlayer.getVideoPlayer().resetMediaPlayer();
    }

    public void showProgressBar() {
        // Show progress item
        pbProgessAction.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        // Hide progress item
        pbProgessAction.setVisibility(View.GONE);
    }
}
