/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.franklinho.vidtrain_android.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v13.app.FragmentCompat;
import android.view.View;
import android.widget.Toast;

import com.franklinho.vidtrain_android.activities.CreationDetailActivity;

public class NewVidTrainFragment extends Camera2VideoFragment
        implements View.OnClickListener, FragmentCompat.OnRequestPermissionsResultCallback {

    public static NewVidTrainFragment newInstance() {
        return new NewVidTrainFragment();
    }

    @Override
    public void stopRecordingVideo() {
        super.stopRecordingVideo();
        Activity activity = getActivity();
        if (null != activity) {
            Toast.makeText(activity, "Video saved: " + getVideoFile(activity),
                    Toast.LENGTH_SHORT).show();
            activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(getVideoFile(activity))));
            Intent i = new Intent(activity, CreationDetailActivity.class);
            i.putExtra("videoPath", getVideoFile(activity).getPath());
            activity.startActivity(i);
        }
    }
}
