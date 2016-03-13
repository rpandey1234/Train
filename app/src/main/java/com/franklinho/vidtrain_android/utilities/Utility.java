package com.franklinho.vidtrain_android.utilities;

import android.text.format.DateUtils;

/**
 * Created by rahul on 3/12/16.
 */
public class Utility {

    /**
     * Gets the relative time from now for the time passed in
     */
    public static String getRelativeTime(long time) {
        return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(),
                DateUtils.SECOND_IN_MILLIS).toString();
    }
}
