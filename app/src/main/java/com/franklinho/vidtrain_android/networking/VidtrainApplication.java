package com.franklinho.vidtrain_android.networking;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.franklinho.vidtrain_android.models.Unseen;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.models.Video;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;

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
