package com.franklinho.vidtrain_android.models;

import com.facebook.GraphResponse;
import com.parse.ParseClassName;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ParseClassName("_User")
public class User extends ParseUser implements Serializable {

    public static final String FB_PROFILE_PIC_FORMAT
            = "http://graph.facebook.com/%s/picture?height=160&width=160";

    public static final String NAME = "name";
    public static final String USER_NAME = "username";
    public static final String PASSWORD = "password";
    public static final String EMAIL = "email";
    public static final String FBID = "fbid";
    public static final String LIKES = "likes";
    public static final String FOLLOWING = "following";
    public static final String VIDEOS = "videos";
    public static final String VIDTRAINS = "vidtrains";
    public static final String PROFILE_IMAGE_URL = "profileImageUrl";
    public static final String FB_LINK = "fbLink";

    public static void updateFacebookData(ParseUser user, GraphResponse response) {
        if (user == null) {
            return;
        }
        try {
            JSONObject jsonObject = response.getJSONObject();
            String name = jsonObject.getString("name");
            String fbid = jsonObject.getString("id");
            String link = jsonObject.getString("link");
            String email = jsonObject.getString("email");
            String profileImageUrl = String.format(FB_PROFILE_PIC_FORMAT, fbid);
            user.put(NAME, name);
            user.put(FBID, fbid);
            user.put(FB_LINK, link);
            user.put(PROFILE_IMAGE_URL, profileImageUrl);
            user.setEmail(email);
            user.saveInBackground();
            // TODO: save friend data
            JSONObject friends = jsonObject.getJSONObject("friends");
        } catch (JSONException e) {
            System.out.println("Failed parsing facebook response: " + e.toString());
            e.printStackTrace();
        }
    }

    public String getName() {
        return getString(NAME);
    }

    public String getProfileImageUrl() {
        return getString(PROFILE_IMAGE_URL);
    }

    public List<Video> getVideos() {
        return getList(VIDEOS);
    }

    public List<VidTrain> getVidTrains() {
        return getList(VIDTRAINS);
    }

    public List<VidTrain> maybeInitAndAdd(VidTrain vidTrain) {
        List<VidTrain> vidTrains = getVidTrains();
        if (vidTrains == null) {
            vidTrains = new ArrayList<>();
        }
        if (!vidTrains.contains(vidTrain)) {
            vidTrains.add(vidTrain);
        }
        return vidTrains;
    }

    public List<Video> maybeInitAndAdd(Video video) {
        List<Video> videos = getVideos();
        if (videos == null) {
            videos = new ArrayList<>();
        }
        if (!videos.contains(video)) {
            videos.add(video);
        }
        return videos;
    }

    public static User getCurrentUser() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        return (User) currentUser;
    }
}
