package com.franklinho.vidtrain_android.models;

import android.graphics.Rect;
import android.view.View;

import com.franklinho.vidtrain_android.adapters.holders.VidTrainViewHolder;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.volokh.danylo.video_player_manager.manager.VideoItem;
import com.volokh.danylo.video_player_manager.manager.VideoPlayerManager;
import com.volokh.danylo.video_player_manager.meta.CurrentItemMetaData;
import com.volokh.danylo.video_player_manager.meta.MetaData;
import com.volokh.danylo.video_player_manager.ui.VideoPlayerView;
import com.volokh.danylo.visibility_utils.items.ListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by franklinho on 3/1/16.
 */
@ParseClassName("VidTrain")
public class VidTrain extends ParseObject implements VideoItem, ListItem {

    User user;
    List<User> collaborators;
    String title;
    String description;
    List<Video> videos;
    List<com.franklinho.vidtrain_android.models.Comment> comments;
    Enum readPrivacy;
    Enum writePrivacy;
    ParseGeoPoint ll;

    public String mDirectUrl;
    private final Rect mCurrentViewRect = new Rect();

    public VideoPlayerManager<MetaData> mVideoPlayerManager;

    public static final String USER_KEY = "user";
    public static final String TITLE_KEY = "title";
    public static final String DESCRIPTION_KEY = "description";
    public static final String VIDEOS_KEY = "videos";
    public static final String COMMENTS_KEY = "comments";
    public static final String READ_PRIVACY_KEY = "readPrivacy";
    public static final String WRITE_PRIVACY_KEY = "writePrivacy";
    public static final String LL_KEY = "ll";
    public static final String THUMBNAIL_KEY = "thumbnail";


    public void setUser(ParseUser user) {
        put(USER_KEY, user);
    }

    public void setTitle(String title) {
        put(TITLE_KEY, title);
    }

    public void setDescription(String description) {
        put(DESCRIPTION_KEY, description);
    }

    public void setVideos(ArrayList<Video> videos) {
        put(VIDEOS_KEY, videos);
    }

    public void setComments(ArrayList<Comment> comments) {
        put(COMMENTS_KEY, comments);
    }
    public void setReadPrivacy(Boolean readPrivacy) {
        put(READ_PRIVACY_KEY, readPrivacy);
    }
    public void setWritePrivacy(Boolean writePrivacy) {
        put(WRITE_PRIVACY_KEY, writePrivacy);
    }

    public void setLL(ParseGeoPoint geoPoint) {
        put(LL_KEY, geoPoint);
    }

    public void setThumbnailFile(ParseFile file) {
        put(THUMBNAIL_KEY, file);
    }

    public ParseUser getUser() {
        return (ParseUser) get(USER_KEY);
    }

    @Override
    public int getVisibilityPercents(View currentView) {

        int percents = 100;

        currentView.getLocalVisibleRect(mCurrentViewRect);

        int height = currentView.getHeight();

        if(viewIsPartiallyHiddenTop()){
            // view is partially hidden behind the top edge
            percents = (height - mCurrentViewRect.top) * 100 / height;
        } else if(viewIsPartiallyHiddenBottom(height)){
            percents = mCurrentViewRect.bottom * 100 / height;
        }


        return percents;
    }
    private boolean viewIsPartiallyHiddenBottom(int height) {
        return mCurrentViewRect.bottom > 0 && mCurrentViewRect.bottom < height;
    }

    private boolean viewIsPartiallyHiddenTop() {
        return mCurrentViewRect.top > 0;
    }

    @Override
    public void setActive(View newActiveView, int newActiveViewPosition) {
        VidTrainViewHolder viewHolder = (VidTrainViewHolder) newActiveView.getTag();
        playNewVideo(new CurrentItemMetaData(newActiveViewPosition, newActiveView), viewHolder.vvPreview, mVideoPlayerManager);

    }

    @Override
    public void deactivate(View currentView, int position) {
        stopPlayback(mVideoPlayerManager);
    }

    @Override
    public void playNewVideo(MetaData currentItemMetaData, VideoPlayerView player, VideoPlayerManager<MetaData> videoPlayerManager) {
        videoPlayerManager.playNewVideo(currentItemMetaData, player, mDirectUrl);
    }

    @Override
    public void stopPlayback(VideoPlayerManager videoPlayerManager) {
        videoPlayerManager.stopAnyPlayback();
    }
}
