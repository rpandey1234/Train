package com.trainapp.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.Serializable;

@ParseClassName("Video")
public class Video extends ParseObject implements Serializable {
    public final static long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;
    public static final String USER_KEY = "user";
    public static final String VIDEO_FILE_KEY = "videoFile";
    public static final String VIDTRAIN_KEY = "vidTrain";
    public static final String THUMBNAIL_KEY = "thumbnail";

    public final static long TIME_TO_EXPIRE = MILLIS_PER_DAY;

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

    public void setThumbnail(ParseFile thumbnail) {
        put(THUMBNAIL_KEY, thumbnail);
    }

    public ParseFile getThumbnail() {
        return getParseFile(THUMBNAIL_KEY);
    }

    public boolean hasVideoExpired() {
        return System.currentTimeMillis() - getCreatedAt().getTime() < TIME_TO_EXPIRE;
    }
}
