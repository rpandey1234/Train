package com.franklinho.vidtrain_android.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by franklinho on 3/1/16.
 */
@ParseClassName("User")
public class User extends ParseObject {
    public String name;
    public String username;
    long userId;
    long videoCount;
    String email;
    List<VidTrain> vidTrains;
    List<Video> videos;
    long likes;
    List<User> friends;
    List<User> following;
    String profileImageUrl;



}
