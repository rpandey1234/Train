package com.franklinho.vidtrain_android.utilities;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.activities.LogInActivity;
import com.franklinho.vidtrain_android.activities.MainActivity;
import com.franklinho.vidtrain_android.activities.VidTrainDetailActivity;
import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

public class PushMessageBroadcast extends ParsePushBroadcastReceiver {
    @Override
    public void onPushOpen(Context context, Intent intent) {
        ParseAnalytics.trackAppOpenedInBackground(intent);

       if (ParseUser.getCurrentUser() != null) {
           String parseData = intent.getExtras().getString( "com.parse.Data" );
           //Here is data you sent
           Log.i("ParsePush", parseData);


           String vidTrainId = getVidtrainFromData(parseData);
           //String userId = getUserIdFromData(parseData);


           if (!vidTrainId.equals("")){
               Intent i = new Intent(context, VidTrainDetailActivity.class);
               i.putExtra(VidTrainDetailActivity.VIDTRAIN_KEY, vidTrainId);
               i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               context.startActivity(i);
           } else {
               Intent i = new Intent(context, MainActivity.class);
               i.putExtras(intent.getExtras());
               i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               context.startActivity(i);
           }
       } else {
           Intent i = new Intent(context, LogInActivity.class);
           i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
           context.startActivity(i);
       }

    }

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        Notification notification = super.getNotification(context, intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification.color = context.getResources().getColor(R.color.white);
        }
        return notification;
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


    private String getVidtrainFromData(String jsonData) {
// Parse JSON Data
        try {
            System.out.println("JSON Data ["+jsonData+"]");
            JSONObject obj = new JSONObject(jsonData);

            return obj.getString("vidTrain");
        }
        catch(JSONException jse) {
            jse.printStackTrace();
        }

        return "";
    }

    private String getUserIdFromData(String jsonData) {
// Parse JSON Data
        try {
            System.out.println("JSON Data ["+jsonData+"]");
            JSONObject obj = new JSONObject(jsonData);

            return obj.getString("userId");
        }
        catch(JSONException jse) {
            jse.printStackTrace();
        }

        return "";
    }
}
