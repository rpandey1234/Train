package com.franklinho.vidtrain_android.models;

import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequest.GraphJSONObjectCallback;
import com.facebook.GraphResponse;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

/**
 * Created by franklinho on 3/1/16.
 */
@ParseClassName("User")
public class User extends ParseObject implements Serializable {

    public static final String FB_PROFILE_PIC_FORMAT
            = "http://graph.facebook.com/%s/picture?height=160&width=160";

    public String name;
    public String username;
    String fbid;
    long userId;
    long videoCount;
    String email;
    List<VidTrain> vidTrains;
    List<Video> videos;
    long likes;
    List<User> friends;
    List<User> following;
    String profileImageUrl;

    public static final String NAME = "name";
    public static final String USER_NAME = "username";
    public static final String PASSWORD = "password";
    public static final String EMAIL = "email";
    public static final String FBID = "fbid";


    public User(){

    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        put(NAME, name);
    }

    public String getFbid() {
        return fbid;
    }

    public void setFbid(String fbid) {
        put(FBID, fbid);
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
}
