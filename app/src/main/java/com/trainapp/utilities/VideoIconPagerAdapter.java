package com.trainapp.utilities;

import android.graphics.Bitmap;

public interface VideoIconPagerAdapter {
    /**
     * Get icon representing the page at {@code index} in the adapter.
     */
    Bitmap getIconBitMap(int index);

    // From PagerAdapter
    int getCount();
}
