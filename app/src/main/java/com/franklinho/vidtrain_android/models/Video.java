package com.franklinho.vidtrain_android.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by franklinho on 3/1/16.
 */
@ParseClassName("Video")
public class Video extends ParseObject {
    public static final String USER_KEY = "user";
    public static final String LIKES_KEY = "likeCount";
    public static final String VIDEO_FILE_KEY = "videoFile";
    public static final String VIDTRAIN_KEY = "vidTrain";

    User user;
    long likes;
    VidTrain vidTrain;
    ParseFile videoFile;

    public void setUser(ParseUser user) {
        put(USER_KEY, user);
    }

    public void setVideoFile(ParseFile file) {
        put(VIDEO_FILE_KEY, file);
    }

    public void setVidTrain(ParseObject vidTrain) {
        put(VIDTRAIN_KEY, vidTrain);
    }
    public void setLikes(int likeCount) {
        put(LIKES_KEY, likeCount);
    }
}
