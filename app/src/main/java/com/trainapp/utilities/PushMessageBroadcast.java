package com.trainapp.utilities;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;
import com.trainapp.activities.LogInActivity;
import com.trainapp.activities.MainActivity;
import com.trainapp.networking.VidtrainApplication;

public class PushMessageBroadcast extends ParsePushBroadcastReceiver {
    @Override
    public void onPushOpen(Context context, Intent intent) {
        ParseAnalytics.trackAppOpenedInBackground(intent);
        Intent i;
        if (ParseUser.getCurrentUser() != null) {
            String parseData = intent.getExtras().getString("com.parse.Data");
            // Here is data you sent
            Log.i(VidtrainApplication.TAG, parseData);
            i = new Intent(context, MainActivity.class);
            i.putExtras(intent.getExtras());
            Utility.setBadgeCount(context, 0);
        } else {
            i = new Intent(context, LogInActivity.class);
        }
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        Utility.setBadgeCount(context, Utility.getBadgeCount(context) + 1);
        // could change notification color here, but fails on Nexus devices
        return super.getNotification(context, intent);
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        // Here You can handle push before appearing into status e.g if you want to stop it.
        super.onPushReceive(context, intent);
    }
}
