package com.franklinho.vidtrain_android.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by franklinho on 3/1/16.
 */
@ParseClassName("Comment")
public class Comment extends ParseObject {
    User user;
    String text;
    Video video;
    VidTrain vidTrain;
    long likes;
}
