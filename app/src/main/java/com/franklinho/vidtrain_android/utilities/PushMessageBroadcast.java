package com.franklinho.vidtrain_android.utilities;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.franklinho.vidtrain_android.activities.HomeActivity;
import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by franklinho on 3/28/16.
 */
public class PushMessageBroadcast extends ParsePushBroadcastReceiver {
    @Override
    public void onPushOpen(Context context, Intent intent) {
        ParseAnalytics.trackAppOpenedInBackground(intent);

        //Here is data you sent
        Log.i("ParsePush", intent.getExtras().getString( "com.parse.Data" ));

        Intent i = new Intent(context, HomeActivity.class);
        i.putExtras(intent.getExtras());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        // TODO Auto-generated method stub
        return super.getNotification(context, intent);
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        // TODO Auto-generated method stub
        super.onPushDismiss(context, intent);
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        //here You can handle push before appearing into status e.g if you want to stop it.
        super.onPushReceive(context, intent);

    }


    private String getData(String jsonData) {
// Parse JSON Data
        try {
            System.out.println("JSON Data ["+jsonData+"]");
            JSONObject obj = new JSONObject(jsonData);

            return obj.getString("message");
        }
        catch(JSONException jse) {
            jse.printStackTrace();
        }

        return "";
    }
}
