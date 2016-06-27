package com.franklinho.vidtrain_android.models;

import android.util.Log;
import com.google.android.gms.maps.model.LatLng;

import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.w3c.dom.Comment;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ParseClassName("VidTrain")
public class VidTrain extends ParseObject implements Serializable {

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
    public static final String LIKES_KEY = "likeCount";

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

    public boolean getWritePrivacy() {
        return getBoolean(WRITE_PRIVACY_KEY);
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

    public User getUser() {
        return (User) getParseUser(USER_KEY);
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

    public void setCollaborators(List<User> collaborators) {
        put(COLLABORATORS, collaborators);
    }

    public List<User> getCollaborators() {
        return (List<User>) get(COLLABORATORS);
    }

    public List<Video> maybeInitAndAdd(Video video) {
        List<Video> videos = getList(VIDEOS_KEY);
        if (videos == null) {
            videos = new ArrayList<>();
        }
        videos.add(video);
        return videos;
    }

    /**
     * Get a list of locally stored Files from this vidtrain. This should be called from
     * a background thread.
     */
    public List<File> getVideoFiles() {
        List<Video> videos = getVideos();
        List<File> localVideoFiles = new ArrayList<>();
        for (Video video : videos) {
            try {
                video.fetchIfNeeded();
                final byte[] data = video.getVideoFile().getData();
                File localVideoFile = Utility.getOutputMediaFile(video.getObjectId());
                Utility.writeToFile(data, localVideoFile);
                localVideoFiles.add(localVideoFile);
            } catch (ParseException parseException) {
                Log.d(VidtrainApplication.TAG, parseException.toString());
            }
        }
        return localVideoFiles;
    }



    public void setLikes(int likeCount) {
        put(LIKES_KEY, likeCount);
        saveInBackground();
    }

    public int getLikes() {
        return getInt(LIKES_KEY);
    }

    public void setRankingValue(float rankingValue) {
        put("rankingValue", rankingValue);
    }


//    public List<File> getVideoFiles() {
//        final List<Video> videos = getVideos();
//        final List<File> localVideoFiles = new ArrayList<>();
//        for (final Video video : videos) {
//            video.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
//                @Override
//                public void done(ParseObject object, ParseException e) {
//                    if (e == null) {
//                        video.getVideoFile().getDataInBackground(new GetDataCallback() {
//                            @Override
//                            public void done(byte[] data, ParseException e) {
//                                File localVideoFile = Utility.getOutputMediaFile(video.getObjectId());
//                                Utility.writeToFile(data, localVideoFile);
//                                localVideoFiles.add(localVideoFile);
//                            }
//                        });
//
//                    } else {
//                        Log.d(VidtrainApplication.TAG, e.toString());
//                    }
//
//                }
//            });
//        }
//        return localVideoFiles;
//    }
}
