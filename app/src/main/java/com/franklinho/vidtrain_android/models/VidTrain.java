package com.franklinho.vidtrain_android.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by franklinho on 3/1/16.
 */
@ParseClassName("VidTrain")
public class VidTrain extends ParseObject {

    User user;
    List<User> collaborators;
    String title;
    String description;
    List<Video> videos;
    List<com.franklinho.vidtrain_android.models.Comment> comments;
    Enum readPrivacy;
    Enum writePrivacy;
    ParseGeoPoint ll;


    public static final String USER_KEY = "user";
    public static final String TITLE_KEY = "title";
    public static final String DESCRIPTION_KEY = "description";
    public static final String VIDEOS_KEY = "videos";
    public static final String COMMENTS_KEY = "comments";
    public static final String READ_PRIVACY_KEY = "readPrivacy";
    public static final String WRITE_PRIVACY_KEY = "writePrivacy";
    public static final String LL_KEY = "ll";
    public static final String THUMBNAIL_KEY = "thumbnail";


    public void setUser(ParseUser user) {
        put(USER_KEY, user);
    }

    public void setTitle(String title) {
        put(TITLE_KEY, title);
    }

    public void setDescription(String description) {
        put(DESCRIPTION_KEY, description);
    }

    public void setVideos(ArrayList<Video> videos) {
        put(VIDEOS_KEY, videos);
    }

    public void setComments(ArrayList<Comment> comments) {
        put(COMMENTS_KEY, comments);
    }
    public void setReadPrivacy(Boolean readPrivacy) {
        put(READ_PRIVACY_KEY, readPrivacy);
    }
    public void setWritePrivacy(Boolean writePrivacy) {
        put(WRITE_PRIVACY_KEY, writePrivacy);
    }

    public void setLL(ParseGeoPoint geoPoint) {
        put(LL_KEY, geoPoint);
    }

    public void setThumbnailFile(ParseFile file) {
        put(THUMBNAIL_KEY, file);
    }

    public ParseUser getUser() {
        return (ParseUser) get(USER_KEY);
    }

}
