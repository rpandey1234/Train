package com.franklinho.vidtrain_android.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.adapters.VideoFragmentPagerAdapter;
import com.franklinho.vidtrain_android.fragments.SwipeViewPager;
import com.franklinho.vidtrain_android.fragments.SwipeViewPager.NextVideoListener;
import com.franklinho.vidtrain_android.fragments.VideoPageFragment;
import com.franklinho.vidtrain_android.fragments.VideoPageFragment.VideoFinishedListener;
import com.franklinho.vidtrain_android.models.Unseen;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.models.Video;
import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class VidTrainDetailActivity extends FragmentActivity implements VideoFinishedListener {

    @Bind(R.id.viewPager) SwipeViewPager _viewPager;

    private static final boolean MARK_SEEN_VIDEOS = true;
    public static final String VIDTRAIN_KEY = "vidTrain";
    private VidTrain _vidTrain;
    private int _lastPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_vid_train_detail);
        ButterKnife.bind(this);
        ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.whereEqualTo("objectId", getVidtrainId());
        query.include("user");
        query.include("videos.user");
        query.include("collaborators");
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
                query.include("vidTrain.videos");
                query.include(Unseen.VIDEOS_KEY);
                query.findInBackground(new FindCallback<Unseen>() {
                    @Override
                    public void done(List<Unseen> unseenList, ParseException e) {
                        if (e != null) {
                            Log.e(VidtrainApplication.TAG, e.toString());
                            return;
                        }
                        int unseenIndex;
                        if (unseenList.isEmpty()) {
                            // This should not happen (only for older vidtrains)
                            unseenIndex = -1;
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
        Toast.makeText(this, "This Vidtrain is invalid", Toast.LENGTH_SHORT).show();
        this.finish();
    }

    private String getVidtrainId() {
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            return uri.getQueryParameter(VIDTRAIN_KEY);
        } else {
            return intent.getStringExtra(VIDTRAIN_KEY);
        }
    }

    void layoutVidTrain(int initialIndex) {
        final boolean shouldPlayVideos;
        List<Video> videos = _vidTrain.getVideos();
        if (initialIndex == -1) {
            // The user has seen all the videos. They can only swipe through the pics now, start
            // them at the last video
            initialIndex = _vidTrain.getVideosCount() - 1;
            shouldPlayVideos = false;
        } else {
            // Reduce the size of videos list to only the unseen ones, and start the viewpager
            // at the beginning of this new list
            videos = _vidTrain.getVideos().subList(initialIndex, _vidTrain.getVideosCount());
            initialIndex = 0;
            shouldPlayVideos = true;
            _viewPager.setPagingEnabled(false);
            final List<Video> finalVideos = videos;
            _viewPager.setNextVideoListener(new NextVideoListener() {
                @Override
                public void onNextVideo(int position) {
                    if (MARK_SEEN_VIDEOS) {
                        Unseen.removeUnseen(
                                _vidTrain, User.getCurrentUser(), finalVideos.get(position));
                    }
                    _viewPager.setCurrentItem(_viewPager.getCurrentItem() + 1, true);
                }
            });
        }
        final VideoFragmentPagerAdapter _videoFragmentPagerAdapter = new VideoFragmentPagerAdapter(
                getSupportFragmentManager(), getBaseContext(), videos, _vidTrain);
        _viewPager.setAdapter(_videoFragmentPagerAdapter);
        final SimpleOnPageChangeListener pageChangeListener = new SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(final int position) {
                if (shouldPlayVideos) {
                    // Null checks are only needed for instant run
                    VideoPageFragment lastFragment =
                            _videoFragmentPagerAdapter.getFragment(_lastPosition);
                    if (lastFragment != null) {
                        lastFragment.stopVideo();
                    }
                    VideoPageFragment fragment = _videoFragmentPagerAdapter.getFragment(position);
                    if (fragment != null) {
                        fragment.playVideo();
                    }
                }
                _lastPosition = position;
            }
        };
        _viewPager.addOnPageChangeListener(pageChangeListener);
        _viewPager.setCurrentItem(initialIndex);
        final int finalInitialIndex = initialIndex;
        _viewPager.post(new Runnable() {
            @Override
            public void run() {
                pageChangeListener.onPageSelected(finalInitialIndex);
            }
        });
    }

    @Override
    public void onVideoCompleted(Video video) {
        if (MARK_SEEN_VIDEOS) {
            Unseen.removeUnseen(_vidTrain, User.getCurrentUser(), video);
        }
        // View pager takes care of not allowing OOB issues.
        _viewPager.setCurrentItem(_viewPager.getCurrentItem() + 1, true);
    }
}
