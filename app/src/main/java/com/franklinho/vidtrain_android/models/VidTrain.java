package com.franklinho.vidtrain_android.models;

import android.graphics.Rect;

import com.google.android.gms.maps.model.LatLng;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by franklinho on 3/1/16.
 */
@ParseClassName("VidTrain")
public class VidTrain extends ParseObject implements Serializable {

    User user;
    List<User> collaborators;
    String title;
    String description;
    List<Video> videos;
    List<com.franklinho.vidtrain_android.models.Comment> comments;
    Enum readPrivacy;
    Enum writePrivacy;
    ParseGeoPoint ll;

    public String mDirectUrl;
    private final Rect mCurrentViewRect = new Rect();


    public static final String USER_KEY = "user";
    public static final String TITLE_KEY = "title";
    public static final String DESCRIPTION_KEY = "description";
    public static final String VIDEOS_KEY = "videos";
    public static final String COMMENTS_KEY = "comments";
    public static final String READ_PRIVACY_KEY = "readPrivacy";
    public static final String WRITE_PRIVACY_KEY = "writePrivacy";
    public static final String LL_KEY = "ll";
    public static final String THUMBNAIL_KEY = "thumbnail";
    public static final String COLLABORATORS = "collaborators";

    public VidTrain() {}

    public void setUser(ParseUser user) {
        put(USER_KEY, user);
    }

    public void setTitle(String title) {
        put(TITLE_KEY, title);
    }

    public void setDescription(String description) {
        put(DESCRIPTION_KEY, description);
    }

    public void setVideos(List<Video> videos) {
        put(VIDEOS_KEY, videos);
    }

    public List<Video> getVideos() {
        return getList(VIDEOS_KEY);
    }

    public int getVideosCount() {
        return getVideos().size();
    }

    public void setComments(List<Comment> comments) {
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

    public void setLatestVideo(ParseFile file) {
        put(THUMBNAIL_KEY, file);
    }

    public ParseFile getLatestVideo() {
        return getParseFile(THUMBNAIL_KEY);
    }

    public ParseUser getUser() {
        return (ParseUser) get(USER_KEY);
    }

    public LatLng getLatLong() {
        ParseGeoPoint parseGeoPoint = getParseGeoPoint(LL_KEY);
        double latitude = parseGeoPoint.getLatitude();
        double longitude = parseGeoPoint.getLongitude();
        return new LatLng(latitude, longitude);
    }

    public String getTitle() {
        return getString(TITLE_KEY);
    }

    public void setCollaborators(List<ParseUser> collaborators) {
        put(COLLABORATORS, collaborators);
    }

    public List<ParseUser> getCollaborators() {
        return getList(COLLABORATORS);
    }

    public List<Video> maybeInitAndAdd(Video video) {
        List<Video> videos = getList(VIDEOS_KEY);
        if (videos == null) {
            videos = new ArrayList<>();
        }
        videos.add(video);
        return videos;
    }
}
