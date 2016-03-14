package com.franklinho.vidtrain_android.utilities;

import com.franklinho.vidtrain_android.models.DynamicVideoPlayerView;
import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.volokh.danylo.video_player_manager.manager.PlayerItemChangeListener;
import com.volokh.danylo.video_player_manager.manager.SingleVideoPlayerManager;
import com.volokh.danylo.video_player_manager.meta.MetaData;

/**
 * Created by rahul on 3/13/16.
 */
public class VideoPlayer {
    private static SingleVideoPlayerManager videoPlayerManager;

    public VideoPlayer() {
        videoPlayerManager = new SingleVideoPlayerManager(new PlayerItemChangeListener() {
            @Override
            public void onPlayerItemChanged(MetaData metaData) {

            }
        });
    }

    public static SingleVideoPlayerManager getVideoPlayer() {
        return videoPlayerManager;
    }

    public static void playVideo(DynamicVideoPlayerView videoPlayer, String videoUrl) {
        final SingleVideoPlayerManager player = VideoPlayer.getVideoPlayer();
        player.playNewVideo(null, videoPlayer, videoUrl);
    }
}
