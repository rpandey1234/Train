package com.trainapp.models;

import java.util.List;

/**
 * Created by franklinho on 8/24/16.
 */
public class VidtrainMessage {
    private List<User> _unSeenUsers;
    private List<User> _seenUsers;

    public VideoModel get_videoModel() {
        return _videoModel;
    }

    public void set_videoModel(VideoModel _videoModel) {
        this._videoModel = _videoModel;
    }

    private VideoModel _videoModel;

    public VidtrainMessage(VideoModel videoModel) {
        _videoModel = videoModel;
    }

    public List<User> get_unSeenUsers() {
        return _unSeenUsers;
    }

    public void set_unSeenUsers(List<User> _unSeenUsers) {
        this._unSeenUsers = _unSeenUsers;
    }

    public List<User> get_seenUsers() {
        return _seenUsers;
    }

    public void set_seenUsers(List<User> _seenUsers) {
        this._seenUsers = _seenUsers;
    }
}
