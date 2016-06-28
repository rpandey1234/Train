package com.franklinho.vidtrain_android.utilities;

import com.franklinho.vidtrain_android.models.DynamicVideoPlayerView;
import com.volokh.danylo.video_player_manager.PlayerMessageState;
import com.volokh.danylo.video_player_manager.manager.PlayerItemChangeListener;
import com.volokh.danylo.video_player_manager.manager.SingleVideoPlayerManager;
import com.volokh.danylo.video_player_manager.meta.MetaData;

public class VideoPlayer {
    private static SingleVideoPlayerManager videoPlayerManager;

    public static void playVideo(DynamicVideoPlayerView videoPlayer, String videoUrl) {
        videoPlayerManager.playNewVideo(null, videoPlayer, videoUrl);
    }

    public static void makeNewVideoPlayer() {
        videoPlayerManager = new SingleVideoPlayerManager(new PlayerItemChangeListener() {
            @Override
            public void onPlayerItemChanged(MetaData metaData) {}
        });
    }

    public static void resetMediaPlayer() {
        videoPlayerManager.resetMediaPlayer();
    }

    public static PlayerMessageState getState() {
        return videoPlayerManager.getCurrentPlayerState();
    }

    public static void stop() {
        videoPlayerManager.stopAnyPlayback();
    }

    public static void start(DynamicVideoPlayerView videoPlayerView) {
        videoPlayerView.start();
    }
}
