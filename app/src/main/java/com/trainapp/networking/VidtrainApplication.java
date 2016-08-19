package com.trainapp.networking;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.trainapp.models.Unseen;
import com.trainapp.models.User;
import com.trainapp.models.VidTrain;
import com.trainapp.models.Video;

/**
 * Database: mongodb://vidtrain:vidtrain@ds017514.mlab.com:17514/vidtrain
 */
public class VidtrainApplication extends Application {

    public static final String TAG = "Vidtrain";

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Video.class);
        ParseObject.registerSubclass(User.class);
        ParseObject.registerSubclass(VidTrain.class);
        ParseObject.registerSubclass(Unseen.class);

        //Normal Parse Configuration
        FacebookSdk.sdkInitialize(getApplicationContext());
        Parse.initialize(this, "0y0WMVmGrDXEfgMgVNzA32ryMuM2gdanfMhH0NMY",
                "MnKZ0GQhxkAblowrw4xVzftapFBT27yeEt4RKd7b");
        ParseFacebookUtils.initialize(getApplicationContext());
    }
}
