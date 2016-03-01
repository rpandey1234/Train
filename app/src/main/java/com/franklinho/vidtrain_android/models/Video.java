package com.franklinho.vidtrain_android.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

/**
 * Created by franklinho on 3/1/16.
 */
@ParseClassName("Video")
public class Video extends ParseObject {
    User user;
    long likes;
    String videoPath;
    VidTrain vidTrain;
    ParseFile videoFile;
}
