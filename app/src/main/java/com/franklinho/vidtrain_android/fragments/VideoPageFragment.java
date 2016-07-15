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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.models.DynamicHeightImageView;
import com.franklinho.vidtrain_android.models.DynamicVideoView;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.Video;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Fragment which holds a single video from a vidtrain.
 */
public class VideoPageFragment extends Fragment {

    @Bind(R.id.ivThumbnail) DynamicHeightImageView _ivThumbnail;
    @Bind(R.id.vvPreview) DynamicVideoView _videoView;
    @Bind(R.id.ivAuthor) ImageView _ivAuthor;
    @Bind(R.id.tvAuthor) TextView _tvAuthor;
    @Bind(R.id.tvTime) TextView _tvTime;

    private String _videoUrl;
    private String _videoThumbnailUrl;
    private VideoFinishedListener _listener;
    private String _videoUserId;
    private String _videoTime;
    private User _user;

    public static VideoPageFragment newInstance(Video video) {
        VideoPageFragment videoPageFragment = new VideoPageFragment();
        Bundle args = new Bundle();
        args.putString("videoUrl", video.getVideoFile().getUrl());
        args.putString("videoThumbnailUrl", video.getThumbnail().getUrl());
        args.putString("videoTime", Utility.getRelativeTime(video.getCreatedAt().getTime()));
        args.putString("videoUserId", video.getUser().getObjectId());
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
            _videoTime = arguments.getString("videoTime");
            _videoUserId = arguments.getString("videoUserId");
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
        _videoView.setHeightRatio(0.9);
        _videoView.setZOrderOnTop(true);
        _videoView.requestFocus();

        _tvTime.setText(_videoTime);
        ParseUser.getQuery()
                .whereEqualTo("objectId", _videoUserId)
                .getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser object, ParseException e) {
                _user = (User) object;
                Glide.with(getContext()).load(_user.getProfileImageUrl()).into(_ivAuthor);
                _tvAuthor.setText(_user.getName());
            }
        });

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
                _listener.onVideoCompleted();
            }
        });
    }

    public void stopVideo() {
        if (getView() == null) {
            throw new IllegalStateException("calling playVideo when view is not ready!");
        }
        _videoView.stopPlayback();
        _ivThumbnail.setVisibility(View.VISIBLE);
        _videoView.setVisibility(View.GONE);
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
