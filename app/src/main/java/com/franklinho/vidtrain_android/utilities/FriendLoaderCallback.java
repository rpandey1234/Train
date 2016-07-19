package com.franklinho.vidtrain_android.utilities;

import com.franklinho.vidtrain_android.models.User;

import java.util.List;

/**
 * An interface which will be passed into the facebook Graph call, the client
 * should implement the desired behavior in the appropriate methods.
 */
public interface FriendLoaderCallback {

    void setUsers(List<User> users);
}
