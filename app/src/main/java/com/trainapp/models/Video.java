package com.trainapp.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.Date;

@ParseClassName("Video")
public class Video extends ParseObject implements Serializable {
    public final static long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;
    public static final String USER_KEY = "user";
    public static final String VIDEO_FILE_KEY = "videoFile";
    public static final String VIDTRAIN_KEY = "vidTrain";
    public static final String THUMBNAIL_KEY = "thumbnail";
    public static final String MESSAGE_KEY = "message";

    public final static long TIME_TO_EXPIRE = 7 * MILLIS_PER_DAY;

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

    public boolean isVideoExpired() {
        Date createdAt = getCreatedAt();
        if (createdAt == null) {
            // createdAt is null- could be for newly created objects?
            return true;
        }
        return System.currentTimeMillis() - createdAt.getTime() > TIME_TO_EXPIRE;
    }

    public void setMessage(String message) {
        put(MESSAGE_KEY, message);
    }

    public String getMessage() {
        return getString(MESSAGE_KEY);
    }
}
