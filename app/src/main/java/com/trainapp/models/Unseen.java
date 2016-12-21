package com.trainapp.models;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.trainapp.BuildConfig;
import com.trainapp.networking.VidtrainApplication;
import com.trainapp.utilities.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * This object stores the vidtrains (and videos in that) which a user has not seen.
 * Eventually we should make this part of the user object, updating it via Cloud Code.
 */
@ParseClassName("Unseen")
public class Unseen extends ParseObject {

    public static final String USER_KEY = "user";
    public static final String VIDTRAIN_KEY = "vidTrain";
    public static final String VIDEOS_KEY = "videos";

    public static final int ALL_SEEN_FLAG = -1;

    public static ParseQuery<Unseen> getQuery() {
        return ParseQuery.getQuery("Unseen");
    }

    public static void addUnseen(VidTrain vidtrain) {
        List<User> collaborators = vidtrain.getCollaborators();
        for (User user : collaborators) {
            addUnseen(vidtrain, user);
        }
    }

    public static void addUnseen(final VidTrain vidtrain, final User user) {
        final boolean isCurrentUser = user.getObjectId().equals(
                User.getCurrentUser().getObjectId());
        final Video latestVideo = vidtrain.getLatestVideo();
        // 1. Check if this user/vidtrain already exists
        ParseQuery<Unseen> query = getQuery();
        query.whereEqualTo(USER_KEY, user);
        query.whereEqualTo(VIDTRAIN_KEY, vidtrain);
        query.findInBackground(new FindCallback<Unseen>() {
            @Override
            public void done(List<Unseen> unseenList, ParseException e) {
                if (e != null) {
                    Log.e(VidtrainApplication.TAG, e.toString());
                    return;
                }
                if (unseenList.isEmpty()) {
                    // 1. Unseen object does not exist. Create it.
                    Unseen unseen = new Unseen();
                    unseen.put(USER_KEY, user);
                    unseen.put(VIDTRAIN_KEY, vidtrain);
                    List<Video> unseenVideos = new ArrayList<>();
                    if (!isCurrentUser || BuildConfig.ADD_UNSEEN_FOR_OWN_VIDEO) {
                        unseenVideos.add(latestVideo);
                    }
                    unseen.put(VIDEOS_KEY, unseenVideos);
                    unseen.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(VidtrainApplication.TAG, "Unable to save Unseen object");
                                return;
                            }
                            Log.d(VidtrainApplication.TAG,
                                    "Saved unseen object! For train " + vidtrain.getObjectId());
                        }
                    });
                    return;
                }
                // Unseen list should have size 1
                Log.d(VidtrainApplication.TAG,
                        "Found user/vidtrain combo, size should be 1. It is: " + unseenList.size());
                Unseen unseen = unseenList.get(0);
                // 2. Relevant unseen already exists.
                // Add the last video of the vidtrain to the unseen videos list
                List<Video> unseenVideos = unseen.getUnseenVideos();
                if (!isCurrentUser || BuildConfig.ADD_UNSEEN_FOR_OWN_VIDEO) {
                    unseenVideos.add(latestVideo);
                }
                unseen.put(VIDEOS_KEY, unseenVideos);
                unseen.saveInBackground();
            }
        });
    }

    public static void removeUnseen(VidTrain vidtrain, User user, final String videoId) {
        ParseQuery<Unseen> query = Unseen.getQuery();
        query.whereEqualTo(USER_KEY, user);
        query.whereEqualTo(VIDTRAIN_KEY, vidtrain);
        query.getFirstInBackground(new GetCallback<Unseen>() {
            @Override
            public void done(Unseen unseen, ParseException e) {
                if (e != null) {
                    Log.e(VidtrainApplication.TAG,
                            "Could not find unseen object (or error occurred), legacy vidtrain? "
                                    + e.toString());
                    return;
                }
                List<Video> unseenVideos = unseen.getUnseenVideos();
                int location = Utility.indexOf(unseenVideos, videoId);
                if (location == -1) {
                    Log.d(VidtrainApplication.TAG, "Unseen video was not in list");
                    return;
                }
                unseenVideos.remove(location);
                unseen.put(VIDEOS_KEY, unseenVideos);
                unseen.saveInBackground();
            }
        });
    }

    public static Unseen getUnseenWithVidtrain(List<Unseen> unseenList, VidTrain vidtrain) {
        for (Unseen unseen : unseenList) {
            if (unseen.getVidtrain().getObjectId().equals(vidtrain.getObjectId())) {
                return unseen;
            }
        }
        return null;
    }

    public List<Video> getUnseenVideos() {
        return getList(VIDEOS_KEY);
    }

    public VidTrain getVidtrain() {
        return (VidTrain) get(VIDTRAIN_KEY);
    }

    public User getUser() {
        return (User) get(USER_KEY);
    }

    /**
     * Get the index of the first video the user has not seen in this vidtrain. If the user has
     * seen all videos, return a sentinel
     */
    public int getUnseenIndex() {
        List<Video> unseenVideos = getUnseenVideos();
        if (unseenVideos.isEmpty()) {
            return ALL_SEEN_FLAG;
        }
        List<Video> allVideos = getVidtrain().getVideos();
        int unseenIndex = Utility.indexOf(allVideos, unseenVideos.get(0));
        if (unseenIndex == -1) {
            // Data corruption
            Log.e(VidtrainApplication.TAG,
                    "IllegalStateException: could not find unseen video in list of Train videos");
            return allVideos.size() - 1;
        }
        return unseenIndex;
    }
}
