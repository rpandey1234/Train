package com.franklinho.vidtrain_android.networking;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.franklinho.vidtrain_android.models.Comment;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.models.Video;
import com.franklinho.vidtrain_android.utilities.VideoPlayer;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;

/**
 * Created by franklinho on 3/1/16.
 */
public class VidtrainApplication extends Application {

    public static final String TAG = "Vidtrain";
    public static VideoPlayer sVideoPlayer;

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Comment.class);
        ParseObject.registerSubclass(Video.class);
        ParseObject.registerSubclass(User.class);
        ParseObject.registerSubclass(VidTrain.class);

        // set applicationId and server based on the values in the Heroku settings.
        // any network interceptors must be added with the Configuration Builder given this syntax
        //Heroku Configuration
//        Parse.initialize(new Parse.Configuration.Builder(this)
//                .applicationId("vidtrain") // should correspond to APP_ID env variable
//                .addNetworkInterceptor(new ParseLogInterceptor())
//                .server("https://vidtrain.herokuapp.com/parse/").build());

        //Normal Parse Configuration
        FacebookSdk.sdkInitialize(getApplicationContext());
        Parse.initialize(this, "0y0WMVmGrDXEfgMgVNzA32ryMuM2gdanfMhH0NMY",
                "MnKZ0GQhxkAblowrw4xVzftapFBT27yeEt4RKd7b");
        ParseFacebookUtils.initialize(getApplicationContext());
        sVideoPlayer = new VideoPlayer();
    }
}
