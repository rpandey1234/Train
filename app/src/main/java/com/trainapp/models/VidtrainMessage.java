package com.trainapp.models;

import java.util.List;

/**
 * Class that contains a VideoModel as well as a list of seen and unseen users. This is used to construct the views of the video messages in the VidtrainLandingFragment's recyclerview
 */
public class VidtrainMessage {
    private List<User> _unSeenUsers;
    private List<User> _seenUsers;

    public VideoModel getVideoModel() {
        return _videoModel;
    }

    public void setVideoModel(VideoModel videoModel) {
        _videoModel = videoModel;
    }

    private VideoModel _videoModel;

    public VidtrainMessage(VideoModel videoModel) {
        _videoModel = videoModel;
    }

    public List<User> getUnseenUsers() {
        return _unSeenUsers;
    }

    public void setUnseenUsers(List<User> unseenUsers) {
        this._unSeenUsers = unseenUsers;
    }

    public List<User> getSeenUsers() {
        return _seenUsers;
    }

    public void setSeenUsers(List<User> seenUsers) {
        this._seenUsers = seenUsers;
    }
}
