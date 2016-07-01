package com.franklinho.vidtrain_android.fragments;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.models.DynamicHeightImageView;
import com.franklinho.vidtrain_android.models.DynamicVideoView;
import com.franklinho.vidtrain_android.models.Video;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Fragment which holds a single video from a vidtrain.
 */
public class VideoPageFragment extends Fragment {

    @Bind(R.id.ivThumbnail) DynamicHeightImageView _ivThumbnail;
    @Bind(R.id.vvPreview) DynamicVideoView _videoView;

    private String _videoUrl;
    private String _videoThumbnailUrl;
    private VideoFinishedListener _listener;

    public static VideoPageFragment newInstance(Video video) {
        VideoPageFragment videoPageFragment = new VideoPageFragment();
        Bundle args = new Bundle();
        args.putString("videoUrl", video.getVideoFile().getUrl());
        args.putString("videoThumbnailUrl", video.getThumbnail().getUrl());
        videoPageFragment.setArguments(args);
        return videoPageFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            _videoUrl = arguments.getString("videoUrl");
            _videoThumbnailUrl = arguments.getString("videoThumbnailUrl");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pager_item_video, container, false);
        ButterKnife.bind(this, v);
        Glide.with(getContext()).load(_videoThumbnailUrl).into(_ivThumbnail);
        _videoView.setVideoPath(_videoUrl);
        _videoView.setHeightRatio(1);
        _videoView.setZOrderOnTop(true);
        _videoView.requestFocus();
        return v;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getView() == null) {
            return;
        }
        if (isVisibleToUser) {
            _videoView.setVisibility(View.VISIBLE);
            _ivThumbnail.setVisibility(View.GONE);
            _videoView.start();
            _videoView.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    _listener.onVideoCompleted();
                }
            });
        } else {
            _videoView.stopPlayback();
            _ivThumbnail.setVisibility(View.VISIBLE);
            _videoView.setVisibility(View.GONE);
        }
    }

    public interface VideoFinishedListener {

        void onVideoCompleted();
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
