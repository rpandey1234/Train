package com.trainapp.models;

import android.content.res.Resources;
import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.trainapp.networking.VidtrainApplication;
import com.trainapp.utilities.Utility;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ParseClassName("VidTrain")
public class VidTrain extends ParseObject implements Serializable {

    public static final String USER_KEY = "user";
    public static final String TITLE_KEY = "title";
    public static final String VIDEOS_KEY = "videos";
    public static final String COLLABORATORS = "collaborators";
    public static final String COLLABORATOR_COUNT = "collaboratorCount";

    public static ParseQuery<VidTrain> getQuery() {
        return ParseQuery.getQuery("VidTrain");
    }

    public void setUser(ParseUser user) {
        put(USER_KEY, user);
    }

    public void setTitle(String title) {
        put(TITLE_KEY, title);
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

    public Video getLatestVideo() {
        if (getVideos().isEmpty()) {
            return null;
        }
        return getVideos().get(getVideosCount() - 1);
    }

    public User getUser() {
        return (User) getParseUser(USER_KEY);
    }

    public void setCollaborators(List<User> collaborators) {
        put(COLLABORATORS, collaborators);
    }

    public void setCollaboratorCount(int count) {
        put(COLLABORATOR_COUNT, count);
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

    public String getGeneratedTitle(Resources resources) {
        String title;
        List<User> collaborators = getCollaborators();
        if (collaborators.size() == 1) {
            // a self-conversation (no others are involved)
            title = collaborators.get(0).getName();
        } else {
            // remove current user from the collaborators list
            Utility.remove(collaborators, User.getCurrentUser().getObjectId());
            title = Utility.generateTitle(collaborators, resources);
        }
        return title;
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
                ParseFile videoFile = video.getVideoFile();
                final byte[] data = videoFile.getData();
                File localVideoFile = Utility.getOutputMediaFile(video.getObjectId());
                Utility.writeToFile(data, localVideoFile);
                localVideoFiles.add(localVideoFile);
            } catch (ParseException parseException) {
                Log.d(VidtrainApplication.TAG, parseException.toString());
            }
        }
        return localVideoFiles;
    }
}
