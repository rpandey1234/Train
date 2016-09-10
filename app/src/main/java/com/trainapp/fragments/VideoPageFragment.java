package com.trainapp.fragments;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.trainapp.R;
import com.trainapp.models.Video;
import com.trainapp.models.VideoModel;
import com.trainapp.utilities.Utility;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragment which holds a single video from a vidtrain.
 */
public class VideoPageFragment extends Fragment {

    @Bind(R.id.ivThumbnail) ImageView _ivThumbnail;
    @Bind(R.id.videoView) VideoView _videoView;
    @Bind(R.id.progressBar) ProgressBar _progressBar;
    @Bind(R.id.tvMessage) TextView _tvMessage;
    @Bind(R.id.ivAuthor) ImageView _ivAuthor;
    @Bind(R.id.tvTime) TextView _tvTime;
    @Bind(R.id.videoInformation) RelativeLayout _videoInformation;
    @Bind(R.id.timer) View _timerView;
    @Bind(R.id.btnSound) Button _btnSound;

    public static final String VIDEO_URL = "VIDEO_URL";
    public static final String VIDEO_THUMBNAIL_URL = "VIDEO_THUMBNAIL_URL";
    public static final String VIDEO_TIME = "VIDEO_TIME";
    public static final String VIDEO_USER_URL = "VIDEO_USER_URL";
    public static final String VIDEO_ID = "VIDEO_ID";
    public static final String VIDEO_MESSAGE = "VIDEO_MESSAGE";
    private static final String FROM_LANDING_FRAGMENT = "FROM_LANDING_FRAGMENT";
    public static final int UPDATE_FREQUENCY = 50;
    public static final int ADVANCE_DELAY = 500;

    private String _videoUrl;
    private String _videoThumbnailUrl;
    private VideoFinishedListener _videoListener;
    private PlaySoundListener _soundListener;
    private String _videoTime;
    private String _userUrl;
    private String _videoId;
    private String _message;
    private Handler _handler = new Handler();
    private Runnable _runnableCode;
    private int _width;
    private boolean _isVideoPrepared;
    private boolean _fromLandingFragment;
    private MediaPlayer _mediaPlayer;

    public static VideoPageFragment newInstance(Video video) {
        VideoPageFragment videoPageFragment = new VideoPageFragment();
        Bundle args = new Bundle();
        args.putString(VIDEO_URL, video.getVideoFile().getUrl());
        args.putString(VIDEO_THUMBNAIL_URL, video.getThumbnail().getUrl());
        args.putString(VIDEO_TIME, Utility.getRelativeTime(video.getCreatedAt().getTime()));
        args.putString(VIDEO_USER_URL, video.getUser().getProfileImageUrl());
        args.putString(VIDEO_ID, video.getObjectId());
        args.putString(VIDEO_MESSAGE, video.getMessage());
        args.putBoolean(FROM_LANDING_FRAGMENT, false);
        videoPageFragment.setArguments(args);
        return videoPageFragment;
    }

    public static VideoPageFragment newInstance(VideoModel videoModel) {
        VideoPageFragment videoPageFragment = new VideoPageFragment();
        Bundle args = new Bundle();
        args.putString(VIDEO_URL, videoModel.getVideoUrl());
        args.putString(VIDEO_THUMBNAIL_URL, videoModel.getThumbnailUrl());
        args.putString(VIDEO_TIME, Utility.getRelativeTime(videoModel.getCreatedAtTime()));
        args.putString(VIDEO_USER_URL, videoModel.getUserUrl());
        args.putString(VIDEO_ID, videoModel.getVideoId());
        args.putString(VIDEO_MESSAGE, videoModel.getMessage());
        args.putBoolean(FROM_LANDING_FRAGMENT, true);
        videoPageFragment.setArguments(args);
        return videoPageFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            _videoId = arguments.getString(VIDEO_ID);
            _videoUrl = arguments.getString(VIDEO_URL);
            _videoThumbnailUrl = arguments.getString(VIDEO_THUMBNAIL_URL);
            _videoTime = arguments.getString(VIDEO_TIME);
            _userUrl = arguments.getString(VIDEO_USER_URL);
            _message = arguments.getString(VIDEO_MESSAGE);
            _fromLandingFragment = arguments.getBoolean(FROM_LANDING_FRAGMENT);
            _soundListener.setPlaySound(_fromLandingFragment || _soundListener.getPlaySound());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.vidtrain_pager, container, false);
        ButterKnife.bind(this, v);
        Glide.with(getContext()).load(_videoThumbnailUrl).into(_ivThumbnail);
        _videoView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        _videoView.setZOrderMediaOverlay(true);

        _tvTime.setText(_videoTime);
        if (!TextUtils.isEmpty(_message)) {
            _tvMessage.setText(_message);
            _tvMessage.setVisibility(View.VISIBLE);
        }
        Glide.with(getContext()).load(_userUrl).into(_ivAuthor);

        setSound(_soundListener.getPlaySound());
        _isVideoPrepared = false;
        _videoView.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer mp) {
                _mediaPlayer = mp;
                setSound(_soundListener.getPlaySound());
                _ivThumbnail.setVisibility(View.GONE);
                _progressBar.setVisibility(View.GONE);
                // Wait some time before indicating that video is prepared, so user does not
                // accidentally click to advance
                _handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        _isVideoPrepared = true;
                    }
                }, ADVANCE_DELAY);
                final int duration = _mediaPlayer.getDuration();
                _runnableCode = new Runnable() {
                    @Override
                    public void run() {
                        _handler.postDelayed(_runnableCode, UPDATE_FREQUENCY);
                        double fraction = _mediaPlayer.getCurrentPosition() / (double) duration;
                        LayoutParams layoutParams = _timerView.getLayoutParams();
                        layoutParams.width = (int) Math.round(fraction * _width);
                        _timerView.setLayoutParams(layoutParams);
                    }
                };
                _handler.post(_runnableCode);
            }
        });
        _videoView.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                _videoListener.onVideoCompleted(_videoId);
                _handler.removeCallbacks(_runnableCode);
                _videoView.stopPlayback();
            }
        });
        return v;
    }

    public void setSound(boolean shouldPlaySound) {
        // Icon reference: https://thenounproject.com/search/?q=sound&i=369924,
        // https://thenounproject.com/search/?q=sound&i=369926
        Drawable drawable = ContextCompat.getDrawable(getContext(),
                shouldPlaySound ? R.drawable.sound_on : R.drawable.sound_off);
        if (_btnSound != null) {
            _btnSound.setBackground(drawable);
        }
        if (_mediaPlayer == null) {
            return;
        }
        int volume = shouldPlaySound ? 1 : 0;
        _mediaPlayer.setVolume(volume, volume);
    }

    @OnClick(R.id.btnSound)
    public void onSoundButtonClicked() {
        _soundListener.setPlaySound(!_soundListener.getPlaySound());
        setSound(_soundListener.getPlaySound());
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                _width = view.getWidth();
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        if (_fromLandingFragment) {
            playVideo();
        }
    }

    public void playVideo() {
        if (getView() == null) {
            throw new IllegalStateException("calling playVideo when view is not ready!");
        }
        _videoView.setVisibility(View.VISIBLE);
        _videoView.setVideoPath(_videoUrl);
        _videoView.start();
    }

    public void stopVideo() {
        if (getView() == null) {
            throw new IllegalStateException("calling stopPlayback when view is not ready!");
        }
        _handler.removeCallbacks(_runnableCode);
        _videoView.stopPlayback();
        _ivThumbnail.setVisibility(View.VISIBLE);
        _videoView.setVisibility(View.GONE);
    }

    public boolean isVideoPrepared() {
        return _isVideoPrepared;
    }

    public interface VideoFinishedListener {

        void onVideoCompleted(String videoId);
    }

    public interface PlaySoundListener {

        void setPlaySound(boolean shouldPlaySound);

        boolean getPlaySound();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof VideoFinishedListener) {
            _videoListener = (VideoFinishedListener) context;
        } else {
            throw new ClassCastException(
                    context.toString() + " must implement VideoPageFragment.VideoFinishedListener");
        }
        if (context instanceof PlaySoundListener) {
            _soundListener = (PlaySoundListener) context;
        } else {
            throw new ClassCastException(
                    context.toString() + " must implement VideoPageFragment.PlaySoundListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        _videoListener = null;
        _soundListener = null;
    }
}
