package com.trainapp.ui;

import android.annotation.TargetApi;
import android.os.Build;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.Fade;
import android.transition.TransitionSet;

/**
 * Created by franklinho on 8/29/16.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ExpandFadeTransition extends TransitionSet {
    public ExpandFadeTransition() {
        setDuration(3000);
        setOrdering(ORDERING_TOGETHER);
        addTransition(new ChangeBounds()).
                addTransition(new ChangeImageTransform());
    }
}