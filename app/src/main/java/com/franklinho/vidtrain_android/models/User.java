package com.franklinho.vidtrain_android.models;

import com.facebook.GraphResponse;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by franklinho on 3/1/16.
 */
@ParseClassName("User")
public class User extends ParseObject implements Serializable {

    public static final String FB_PROFILE_PIC_FORMAT
            = "http://graph.facebook.com/%s/picture?height=160&width=160";

    public static final String NAME = "name";
    public static final String USER_NAME = "username";
    public static final String PASSWORD = "password";
    public static final String EMAIL = "email";
    public static final String FBID = "fbid";
    public static final String LIKES = "likes";
    public static final String FOLLOWING = "following";

    public User(){}

    public void setName(String name) {
        put(NAME, name);
    }

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
            user.put("name", name);
            user.put("fbid", fbid);
            user.put("fbLink", link);
            user.put("profileImageUrl", profileImageUrl);
            user.setEmail(email);
            user.saveInBackground();
            // TODO: save friend data
            JSONObject friends = jsonObject.getJSONObject("friends");
        } catch (JSONException e) {
            System.out.println("Failed parsing facebook response: " + e.toString());
            e.printStackTrace();
        }
    }

    public static List<ParseUser> getFollowing(ParseUser user) {
        return user.getList(FOLLOWING);
    }

    public static boolean isFollowing(ParseUser user, ParseUser candidate) {
        List<ParseUser> following = getFollowing(user);
        if (following == null) {
            return false;
        }
        for (int i = 0; i < following.size(); i++) {
            ParseUser candidateUser = following.get(i);
            if (candidate.getObjectId().equals(candidateUser.getObjectId())) {
                return true;
            }
        }
        return false;
    }

    public static String getName(ParseUser user) {
        return user.getString(NAME);
    }

    public static String getProfileImageUrl(ParseUser user) {
        return user.getString("profileImageUrl");
    }

    public static List<Video> getVideos(ParseUser user) {
        return user.getList("videos");
    }

    public static List<VidTrain> getVidTrains(ParseUser user) {
        return user.getList("vidtrains");
    }

    public static List<VidTrain> maybeInitAndAdd(ParseUser user, VidTrain vidTrain) {
        List<VidTrain> vidTrains = getVidTrains(user);
        if (vidTrains == null) {
            vidTrains = new ArrayList<>();
        }
        if (!vidTrains.contains(vidTrain)) {
            vidTrains.add(vidTrain);
        }
        return vidTrains;
    }

    public static List<Video> maybeInitAndAdd(ParseUser user, Video video) {
        List<Video> videos = getVideos(user);
        if (videos == null) {
            videos = new ArrayList<>();
        }
        if (!videos.contains(video)) {
            videos.add(video);
        }
        return videos;
    }

    public static List<ParseUser> maybeInitAndAdd(ParseUser user, ParseUser other) {
        List<ParseUser> following = getFollowing(user);
        if (following == null) {
            following = new ArrayList<>();
        }
        if (!following.contains(other)) {
            following.add(other);
        }
        return following;
    }

    public static Map<String,Boolean> getLikes(ParseUser user) {
        return user.getMap(LIKES);
    }

    public static Map<String,Boolean> postLike(ParseUser user, String objectId) {
        Map<String, Boolean> userLikes = getLikes(user);
        if (userLikes == null) {
            userLikes = new HashMap<>();
        }
        userLikes.put(objectId, true);
        user.put(LIKES, userLikes);
        user.saveInBackground();

        return userLikes;
    }

    public static Map<String,Boolean> postUnlike(ParseUser user, String objectId) {
        Map<String,Boolean> userLikes = getLikes(user);
        if (userLikes == null) {
            userLikes = new HashMap<>();
        }
        userLikes.put(objectId, false);
        user.put(LIKES, userLikes);
        user.saveInBackground();
        return userLikes;
    }

    public static boolean hasLikedVidtrain(ParseUser user, String vidtrainId) {
        Map<String, Boolean> userLikes = getLikes(user);
        return userLikes != null && Boolean.TRUE.equals(userLikes.get(vidtrainId));
    }

    public static List<ParseUser> unfollow(ParseUser currentUser, ParseUser profileUser) {
        List<ParseUser> following = getFollowing(currentUser);
        for (int i = 0; i < following.size(); i++) {
            ParseUser user = following.get(i);
            if (user.getObjectId().equals(profileUser.getObjectId())) {
                following.remove(i);
                break;
            }
        }
        return following;
    }
}
