package com.franklinho.vidtrain_android.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.activities.VidTrainDetailActivity;
import com.franklinho.vidtrain_android.adapters.ImagePagerAdapter;
import com.franklinho.vidtrain_android.models.DynamicHeightViewPager;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.models.Video;
import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.viewpagerindicator.CirclePageIndicator;

import java.io.File;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by rahul on 3/11/16.
 */
public class ImagePreviewDialogFragment extends DialogFragment {

    @Bind(R.id.vpPreview)
    DynamicHeightViewPager vpPreview;
    @Bind(R.id.cpIndicator)
    CirclePageIndicator cpIndicator;
    @Bind(R.id.tvTitle) TextView titleTv;
    @Bind(R.id.tvVideoCount) TextView tvVideoCount;
    @Bind(R.id.tvTime) TextView tvTime;
    @Bind(R.id.tvLikeCount) TextView tvLikeCount;
    @Bind(R.id.ibtnLike) ImageButton ibtnLike;
    File videoFile;

    boolean liked = false;
    VidTrain vidTrain;
    int currentPage = 0;

    public static final String VIDTRAIN_ID = "vidTrainId";

    public ImagePreviewDialogFragment() {}

    public static ImagePreviewDialogFragment newInstance(String vidTrainId) {
        ImagePreviewDialogFragment fragment = new ImagePreviewDialogFragment();
        Bundle args = new Bundle();
        args.putString(VIDTRAIN_ID, vidTrainId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        final View v = inflater.inflate(R.layout.fragment_image_preview, container);
        ButterKnife.bind(this, v);




        v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                vpPreview.post(new Runnable() {
                    public void run() {
                        int width = v.getWidth();
//                        int height = width;
//                        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
//                        vpPreview.setLayoutParams(lp);
                        ViewGroup.LayoutParams lp = vpPreview.getLayoutParams();
                        lp.height = width;
                        vpPreview.setLayoutParams(lp);
                    }
                });
            }
        });


//        vpPreview.setHeightRatio(1);
        final String vidTrainId = getArguments().getString(VIDTRAIN_ID);
        ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.whereEqualTo("objectId", vidTrainId);
        query.getFirstInBackground(new GetCallback<VidTrain>() {
            @Override
            public void done(final VidTrain returnedVidTrain, ParseException e) {
                vidTrain = returnedVidTrain;
                if (e != null) {
                    Log.d(VidtrainApplication.TAG, e.toString());
                    return;
                }
                v.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getContext(), VidTrainDetailActivity.class);
                        i.putExtra(VidTrainDetailActivity.VIDTRAIN_KEY, vidTrain.getObjectId());
                        getContext().startActivity(i);
                    }
                });

                if (User.hasLikedVidtrain(ParseUser.getCurrentUser(), vidTrain.getObjectId())) {
                    liked = true;
                    ibtnLike.setImageResource(R.drawable.heart_icon_red);
                }

                tvLikeCount.setText(getContext().getResources().getQuantityString(R.plurals.likes_count,
                        vidTrain.getLikes(), vidTrain.getLikes()));

                titleTv.setText(vidTrain.getTitle());
                int videoCount = vidTrain.getVideosCount();
                String totalVideos = getResources().getQuantityString(R.plurals.videos_count,
                        videoCount, videoCount);
                tvVideoCount.setText(totalVideos);
                tvTime.setText(Utility.getRelativeTime(vidTrain.getCreatedAt().getTime()));

                final List<Video> videos = vidTrain.getVideos();
                vpPreview.setAdapter(new ImagePagerAdapter(getContext(), videos));
                cpIndicator.setViewPager(vpPreview);



                final int PROGRESS_INTERVAL = 1000;
                final Handler mHandler = new Handler();

                Runnable mImageProgressRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (currentPage < videos.size()) {
                            vpPreview.setCurrentItem(currentPage++, true);
                        }
                        mHandler.postDelayed(this, PROGRESS_INTERVAL);
                    }
                };

                mHandler.postDelayed(mImageProgressRunnable, PROGRESS_INTERVAL);
            }
        });
        return v;
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.ibtnLike)
    public void onVidTrainLiked(View view) {
        final Animation animScale = AnimationUtils.loadAnimation(getContext(), R.anim.anim_scale);
        if (liked) {
            User.postUnlike(ParseUser.getCurrentUser(), vidTrain.getObjectId());
            liked = false;
            ibtnLike.setImageResource(R.drawable.heart_icon);
            int currentLikeCount = vidTrain.getLikes();
            if (currentLikeCount > 0) {
                vidTrain.setLikes(currentLikeCount - 1);
            } else {
                vidTrain.setLikes(0);
            }
        } else {
            User.postLike(ParseUser.getCurrentUser(), vidTrain.getObjectId());
            liked = true;
            ibtnLike.setImageResource(R.drawable.heart_icon_red);
            vidTrain.setLikes(vidTrain.getLikes() + 1);
        }
        view.startAnimation(animScale);
        tvLikeCount.setText(getResources().getQuantityString(R.plurals.likes_count,
                vidTrain.getLikes(), vidTrain.getLikes()));
    }




}
