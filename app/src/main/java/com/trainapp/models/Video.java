package com.trainapp.models;

import android.util.Log;

import com.trainapp.networking.VidtrainApplication;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.Serializable;

@ParseClassName("Video")
public class Video extends ParseObject implements Serializable {
    public static final String USER_KEY = "user";
    public static final String LIKES_KEY = "likeCount";
    public static final String VIDEO_FILE_KEY = "videoFile";
    public static final String VIDTRAIN_KEY = "vidTrain";
    public static final String THUMBNAIL_KEY = "thumbnail";

    public void setUser(ParseUser user) {
        put(USER_KEY, user);
    }

    public User getUser() {
        return (User) getParseUser(USER_KEY);
    }

    public void setVideoFile(ParseFile file) {
        put(VIDEO_FILE_KEY, file);
    }

    public ParseFile getVideoFile() {
        return getParseFile(VIDEO_FILE_KEY);
    }

    public void setVidTrain(ParseObject vidTrain) {
        put(VIDTRAIN_KEY, vidTrain);
    }

    public void setLikes(int likeCount) {
        put(LIKES_KEY, likeCount);
    }

    public void setThumbnail(ParseFile thumbnail) {
        put(THUMBNAIL_KEY, thumbnail);
    }

    public ParseFile getThumbnail() {
        // TODO: this check is a hack to prevent random crash:
        // java.lang.IllegalStateException: ParseObject has no data for 'thumbnail'.
        // Call fetchIfNeeded() to get the data.
        if (containsKey(THUMBNAIL_KEY)) {
            return getParseFile(THUMBNAIL_KEY);
        }
        Log.d(VidtrainApplication.TAG, "just saved a crash! rejoice");
        return null;
    }
}
