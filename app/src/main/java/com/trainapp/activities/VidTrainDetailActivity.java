package com.trainapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.trainapp.BuildConfig;
import com.trainapp.R;
import com.trainapp.adapters.VideoFragmentPagerAdapter;
import com.trainapp.fragments.SwipeViewPager;
import com.trainapp.fragments.SwipeViewPager.NextVideoListener;
import com.trainapp.fragments.VideoPageFragment;
import com.trainapp.fragments.VideoPageFragment.PlaySoundListener;
import com.trainapp.fragments.VideoPageFragment.VideoFinishedListener;
import com.trainapp.fragments.VidtrainLandingFragment;
import com.trainapp.models.Unseen;
import com.trainapp.models.User;
import com.trainapp.models.VidTrain;
import com.trainapp.models.Video;
import com.trainapp.networking.VidtrainApplication;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class VidTrainDetailActivity extends FragmentActivity
        implements VideoFinishedListener, PlaySoundListener {

    @Bind(R.id.viewPager) SwipeViewPager _viewPager;

    public static final String VIDTRAIN_KEY = "vidTrain";
    private VidTrain _vidTrain;
    private int _lastPosition = -1;
    private VideoFragmentPagerAdapter _videoPagerAdapter;
    private boolean _shouldPlaySound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_vid_train_detail);
        ButterKnife.bind(this);
        _viewPager.setPagingEnabled(false);
        ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.whereEqualTo("objectId", getVidtrainId());
        query.include(VidTrain.USER_KEY);
        query.include(VidTrain.VIDEOS_KEY + "." + Video.USER_KEY);
        query.include(VidTrain.COLLABORATORS);
        query.getFirstInBackground(new GetCallback<VidTrain>() {
            @Override
            public void done(VidTrain object, ParseException e) {
                if (e != null) {
                    invalidVidtrain();
                    return;
                }
                _vidTrain = object;
                ParseQuery<Unseen> query = ParseQuery.getQuery("Unseen");
                query.whereEqualTo(Unseen.USER_KEY, ParseUser.getCurrentUser());
                query.whereEqualTo(Unseen.VIDTRAIN_KEY, _vidTrain);
                query.include(Unseen.VIDTRAIN_KEY + "." + Unseen.VIDEOS_KEY);
                query.include(Unseen.VIDEOS_KEY);
                query.findInBackground(new FindCallback<Unseen>() {
                    @Override
                    public void done(List<Unseen> unseenList, ParseException e) {
                        if (e != null) {
                            Log.e(VidtrainApplication.TAG, e.toString());
                            return;
                        }
                        int unseenIndex;
                        if (BuildConfig.VIEW_ALL_VIDEOS) {
                            unseenIndex = 0;
                        } else if (unseenList.isEmpty()) {
                            // This should not happen (only for older vidtrains)
                            unseenIndex = Unseen.ALL_SEEN_FLAG;
                        } else {
                            unseenIndex = unseenList.get(0).getUnseenIndex();
                            Log.d(VidtrainApplication.TAG, "go directly to index: " + unseenIndex);
                        }
                        layoutVidTrain(unseenIndex);
                    }
                });
            }
        });
    }

    public void invalidVidtrain() {
        Toast.makeText(this, R.string.invalid_train, Toast.LENGTH_SHORT).show();
        this.finish();
    }

    private String getVidtrainId() {
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            return uri.getQueryParameter(VIDTRAIN_KEY);
        }
        return intent.getStringExtra(VIDTRAIN_KEY);
    }

    private void layoutVidTrain(int position) {
        final List<Video> videos;
        if (position == Unseen.ALL_SEEN_FLAG) {
            // User has seen all the videos. They only get to see the final landing page.
            videos = new ArrayList<>();
        } else {
            // User can view the videos starting at this position.
            videos = _vidTrain.getVideos().subList(position, _vidTrain.getVideosCount());
        }
        _videoPagerAdapter = new VideoFragmentPagerAdapter(
                getSupportFragmentManager(), videos, _vidTrain, this);
        _viewPager.setNextVideoListener(new NextVideoListener() {
            @Override
            public void onNextVideo(int position) {
                if (position < videos.size()) {
                    VideoPageFragment fragment = _videoPagerAdapter.getFragment(position);
                    if (!fragment.isVideoPrepared()) {
                        Log.d(VidtrainApplication.TAG,
                                "can't auto-advance while current video is loading");
                        return;
                    }
                    goNextVideo(videos.get(position).getObjectId());
                }
            }
        });
        _viewPager.setAdapter(_videoPagerAdapter);
        final SimpleOnPageChangeListener pageChangeListener = new SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(final int position) {
                // Null checks are only needed for instant run
                VideoPageFragment lastFragment = _videoPagerAdapter.getFragment(_lastPosition);
                if (lastFragment != null) {
                    lastFragment.stopVideo();
                }
                VideoPageFragment fragment = _videoPagerAdapter.getFragment(position);
                if (fragment != null) {
                    fragment.playVideo();
                    fragment.setSound(_shouldPlaySound);
                }
                _lastPosition = position;
            }
        };
        _viewPager.addOnPageChangeListener(pageChangeListener);
        _viewPager.setCurrentItem(0);
        _viewPager.post(new Runnable() {
            @Override
            public void run() {
                pageChangeListener.onPageSelected(0);
            }
        });
    }

    @Override
    public void onVideoCompleted(String videoId) {
        goNextVideo(videoId);
    }

    private void goNextVideo(String currentVideoId) {
        if (BuildConfig.MARK_SEEN_VIDEOS) {
            Unseen.removeUnseen(_vidTrain, User.getCurrentUser(), currentVideoId);
        }
        // View pager takes care of not allowing OOB issues.
        _viewPager.setCurrentItem(_viewPager.getCurrentItem() + 1, true);
        VidtrainLandingFragment landingFragment = _videoPagerAdapter.getLandingFragment();
        if (landingFragment != null) {
            landingFragment.videoCompleted();
        }
    }

    @Override
    public void onBackPressed() {
        if (_videoPagerAdapter == null) {
            super.onBackPressed();
            return;
        }
        VidtrainLandingFragment landingFragment = _videoPagerAdapter.getLandingFragment();
        if (landingFragment != null && landingFragment.isVideoPlaying()) {
            landingFragment.videoFragmentClicked();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void setPlaySound(boolean shouldPlaySound) {
        _shouldPlaySound = shouldPlaySound;
    }

    @Override
    public boolean getPlaySound() {
        return _shouldPlaySound;
    }
}
