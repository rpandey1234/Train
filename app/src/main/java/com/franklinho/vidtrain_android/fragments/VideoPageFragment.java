package com.franklinho.vidtrain_android.fragments;

import android.content.Context;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.models.Video;
import com.franklinho.vidtrain_android.utilities.Utility;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Fragment which holds a single video from a vidtrain.
 */
public class VideoPageFragment extends Fragment {

    @Bind(R.id.ivThumbnail) ImageView _ivThumbnail;
    @Bind(R.id.videoView) VideoView _videoView;
    @Bind(R.id.ivAuthor) ImageView _ivAuthor;
    @Bind(R.id.tvTime) TextView _tvTime;
    @Bind(R.id.videoInformation) RelativeLayout _videoInformation;

    public static final String VIDEO_URL = "VIDEO_URL";
    public static final String VIDEO_THUMBNAIL_URL = "VIDEO_THUMBNAIL_URL";
    public static final String VIDEO_TIME = "VIDEO_TIME";
    public static final String VIDEO_USER_URL = "VIDEO_USER_URL";
    public static final String VIDEO = "VIDEO";

    private String _videoUrl;
    private String _videoThumbnailUrl;
    private VideoFinishedListener _listener;
    private String _videoTime;
    private String _userUrl;
    private Video _video;

    public static VideoPageFragment newInstance(Video video) {
        VideoPageFragment videoPageFragment = new VideoPageFragment();
        Bundle args = new Bundle();
        args.putString(VIDEO_URL, video.getVideoFile().getUrl());
        args.putString(VIDEO_THUMBNAIL_URL, video.getThumbnail().getUrl());
        args.putString(VIDEO_TIME, Utility.getRelativeTime(video.getCreatedAt().getTime()));
        args.putString(VIDEO_USER_URL, video.getUser().getProfileImageUrl());
        args.putSerializable(VIDEO, video);
        videoPageFragment.setArguments(args);
        return videoPageFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            _video = (Video) arguments.getSerializable(VIDEO);
            _videoUrl = arguments.getString(VIDEO_URL);
            _videoThumbnailUrl = arguments.getString(VIDEO_THUMBNAIL_URL);
            _videoTime = arguments.getString(VIDEO_TIME);
            _userUrl = arguments.getString(VIDEO_USER_URL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.vidtrain_pager, container, false);
        ButterKnife.bind(this, v);
        Glide.with(getContext()).load(_videoThumbnailUrl).into(_ivThumbnail);
        _videoView.setVideoPath(_videoUrl);
        _videoView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        _videoView.setZOrderMediaOverlay(true);

        _tvTime.setText(_videoTime);
        Glide.with(getContext()).load(_userUrl).into(_ivAuthor);
        return v;
    }

    public void playVideo() {
        if (getView() == null) {
            throw new IllegalStateException("calling playVideo when view is not ready!");
        }
        _videoView.setVisibility(View.VISIBLE);
        _ivThumbnail.setVisibility(View.GONE);
        _videoView.start();
        _videoView.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                _listener.onVideoCompleted(_video);
            }
        });
    }

    public void stopVideo() {
        if (getView() == null) {
            throw new IllegalStateException("calling stopPlayback when view is not ready!");
        }
        _videoView.stopPlayback();
        _ivThumbnail.setVisibility(View.VISIBLE);
        _videoView.setVisibility(View.GONE);
    }

    public interface VideoFinishedListener {

        // TODO: may need to pass in video id here instead, since parse objects internals are not
        // passed in through serializable.
        void onVideoCompleted(Video video);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof VideoFinishedListener) {
            _listener = (VideoFinishedListener) context;
        } else {
            throw new ClassCastException(
                    context.toString() + " must implement VideoPageFragment.VideoFinishedListener");
        }
    }
}
