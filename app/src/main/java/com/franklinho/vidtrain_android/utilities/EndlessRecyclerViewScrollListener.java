package com.franklinho.vidtrain_android.utilities;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

public abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {
    // The minimum amount of items to have below your current scroll position
    // before loading more.
    private int _visibleThreshold = 5;
    // The current offset index of data you have loaded
    private int _currentPage = 0;
    // The total number of items in the dataset after the last load
    private int _previousTotalItemCount = 0;
    // True if we are still waiting for the last set of data to load.
    private boolean _loading = true;
    // Sets the starting page index
    private int _startingPageIndex = 0;

    RecyclerView.LayoutManager _layoutManager;

    public EndlessRecyclerViewScrollListener(LinearLayoutManager layoutManager) {
        this._layoutManager = layoutManager;
    }

    public EndlessRecyclerViewScrollListener(GridLayoutManager layoutManager) {
        this._layoutManager = layoutManager;
        _visibleThreshold = _visibleThreshold * layoutManager.getSpanCount();
    }

    public EndlessRecyclerViewScrollListener(StaggeredGridLayoutManager layoutManager) {
        this._layoutManager = layoutManager;
        _visibleThreshold = _visibleThreshold * layoutManager.getSpanCount();
    }

    public int getLastVisibleItem(int[] lastVisibleItemPositions) {
        int maxSize = 0;
        for (int i = 0; i < lastVisibleItemPositions.length; i++) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i];
            }
            else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i];
            }
        }
        return maxSize;
    }

    // This happens many times a second during a scroll, so be wary of the code you place here.
    // We are given a few useful parameters to help us work out if we need to load some more data,
    // but first we check if we are waiting for the previous load to finish.
    @Override
    public void onScrolled(RecyclerView view, int dx, int dy) {
        int lastVisibleItemPosition = 0;
        int totalItemCount = _layoutManager.getItemCount();

        if (_layoutManager instanceof StaggeredGridLayoutManager) {
            int[] lastVisibleItemPositions = ((StaggeredGridLayoutManager) _layoutManager).findLastVisibleItemPositions(null);
            // get maximum element within the list
            lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions);
        } else if (_layoutManager instanceof LinearLayoutManager) {
            lastVisibleItemPosition = ((LinearLayoutManager) _layoutManager).findLastVisibleItemPosition();
        } else if (_layoutManager instanceof GridLayoutManager) {
            lastVisibleItemPosition = ((GridLayoutManager) _layoutManager).findLastVisibleItemPosition();
        }

        // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        if (totalItemCount < _previousTotalItemCount) {
            this._currentPage = this._startingPageIndex;
            this._previousTotalItemCount = totalItemCount;
            if (totalItemCount == 0) {
                this._loading = true;
            }
        }
        // If it’s still loading, we check to see if the dataset count has
        // changed, if so we conclude it has finished loading and update the current page
        // number and total item count.
        if (_loading && (totalItemCount > _previousTotalItemCount)) {
            _loading = false;
            _previousTotalItemCount = totalItemCount;
        }

        // If it isn’t currently loading, we check to see if we have breached
        // the _visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        // threshold should reflect how many total columns there are too
        if (!_loading && (lastVisibleItemPosition + _visibleThreshold) > totalItemCount) {
            _currentPage++;
            onLoadMore(_currentPage, totalItemCount);
            _loading = true;
        }
    }

    // Defines the process for actually loading more data based on page
    public abstract void onLoadMore(int page, int totalItemsCount);

}
