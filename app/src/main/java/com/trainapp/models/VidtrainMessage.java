package com.trainapp.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that contains a VideoModel as well as a list of seen and unseen users. This is used to
 * construct the views of the video messages in the VidtrainLandingFragment's recyclerview
 */
public class VidtrainMessage {
    private List<User> _unseenUsers;
    private List<User> _seenUsers;
    private VideoModel _videoModel;

    public VidtrainMessage(VideoModel videoModel) {
        _videoModel = videoModel;
        _seenUsers = new ArrayList<>();
        _unseenUsers = new ArrayList<>();
    }

    public VideoModel getVideoModel() {
        return _videoModel;
    }

    public List<User> getUnseenUsers() {
        return _unseenUsers;
    }

    public void addUnseenUsers(List<User> unseenUsers) {
        if (unseenUsers == null) {
            return;
        }
        _unseenUsers.addAll(unseenUsers);
    }

    public List<User> getSeenUsers() {
        return _seenUsers;
    }

    public void addSeenUsers(List<User> seenUsers) {
        if (seenUsers == null) {
            return;
        }
        _seenUsers.addAll(seenUsers);
    }
}
