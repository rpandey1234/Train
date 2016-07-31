package com.trainapp.utilities;

import com.trainapp.models.User;

import java.util.List;

/**
 * An interface which will be passed into the facebook Graph call, the client
 * should implement the desired behavior in the appropriate methods.
 */
public interface FriendLoaderCallback {

    void setUsers(List<User> users);
}
