package com.trainapp.networking;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.stetho.Stetho;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.interceptors.ParseStethoInterceptor;
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

        Stetho.initializeWithDefaults(this);

        FacebookSdk.sdkInitialize(getApplicationContext());
        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId("0y0WMVmGrDXEfgMgVNzA32ryMuM2gdanfMhH0NMY")
                .clientKey(null)
                .addNetworkInterceptor(new ParseStethoInterceptor())
//                .server("http://localhost:1337/parse/").build());
                .server("http://trainvideo.herokuapp.com/parse/").build());
        ParseFacebookUtils.initialize(getApplicationContext());
    }
}
