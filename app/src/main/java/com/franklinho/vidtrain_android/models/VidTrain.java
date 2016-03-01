package com.franklinho.vidtrain_android.models;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by franklinho on 3/1/16.
 */
@ParseClassName("VidTrain")
public class VidTrain extends ParseObject {
    User creator;
    List<User> collaborators;
    String title;
    String description;
    List<Video> videos;
    List<com.franklinho.vidtrain_android.models.Comment> comments;
    Enum readPrivacy;
    Enum writePrivacy;
    ParseGeoPoint ll;
}
