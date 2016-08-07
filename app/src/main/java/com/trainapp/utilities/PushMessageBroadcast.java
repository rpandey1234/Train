package com.trainapp.utilities;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.trainapp.R;
import com.trainapp.activities.LogInActivity;
import com.trainapp.activities.MainActivity;
import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

public class PushMessageBroadcast extends ParsePushBroadcastReceiver {
    @Override
    public void onPushOpen(Context context, Intent intent) {
        ParseAnalytics.trackAppOpenedInBackground(intent);
        Intent i;
        if (ParseUser.getCurrentUser() != null) {
            String parseData = intent.getExtras().getString("com.parse.Data");
            // Here is data you sent
            Log.i("ParsePush", parseData);
            i = new Intent(context, MainActivity.class);
            i.putExtras(intent.getExtras());
        } else {
            i = new Intent(context, LogInActivity.class);
        }
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        Notification notification = super.getNotification(context, intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification.color = context.getResources().getColor(android.R.color.white);
        }
        return notification;
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        super.onPushDismiss(context, intent);
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        // Here You can handle push before appearing into status e.g if you want to stop it.
        super.onPushReceive(context, intent);
    }

    private String getVidtrainFromData(String jsonData) {
        // Parse JSON Data
        try {
            JSONObject obj = new JSONObject(jsonData);
            return obj.getString("vidTrain");
        } catch(JSONException jse) {
            jse.printStackTrace();
        }
        return null;
    }
}
