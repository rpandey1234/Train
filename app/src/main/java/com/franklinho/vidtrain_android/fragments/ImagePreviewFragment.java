package com.franklinho.vidtrain_android.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.flipboard.bottomsheet.commons.BottomSheetFragment;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.activities.HomeActivity;
import com.franklinho.vidtrain_android.activities.ProfileActivity;
import com.franklinho.vidtrain_android.activities.VidTrainDetailActivity;
import com.franklinho.vidtrain_android.adapters.ImagePagerAdapter;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.models.Video;
import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.makeramen.roundedimageview.RoundedImageView;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
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
public class ImagePreviewFragment extends BottomSheetFragment {

    @Bind(R.id.vpPreview) ViewPager vpPreview;
    @Bind(R.id.cpIndicator) CirclePageIndicator cpIndicator;
    @Bind(R.id.tvTitle) TextView titleTv;
//    @Bind(R.id.tvVideoCount) TextView tvVideoCount;
//    @Bind(R.id.tvTime) TextView tvTime;
    @Bind(R.id.tvLikeCount) TextView tvLikeCount;
    @Bind(R.id.ibtnLike) ImageButton ibtnLike;
    @Bind(R.id.btnWatchVideos)
    Button btnWatchVideos;
    @Bind(R.id.ivCollaborators)
    RoundedImageView ivCollaborators;
    File videoFile;

    boolean liked = false;
    VidTrain vidTrain;
    int currentPage = 0;

    public static final String VIDTRAIN_ID = "vidTrainId";

    public ImagePreviewFragment() {}

    public static ImagePreviewFragment newInstance(String vidTrainId) {
        ImagePreviewFragment fragment = new ImagePreviewFragment();
        Bundle args = new Bundle();
        args.putString(VIDTRAIN_ID, vidTrainId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_image_preview_bottomsheet, container);
        ButterKnife.bind(this, v);
//        v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                vpPreview.post(new Runnable() {
//                    public void run() {
//                        int width = v.getWidth();
////                        int height = width;
////                        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
////                        vpPreview.setLayoutParams(lp);
//                        ViewGroup.LayoutParams lp = vpPreview.getLayoutParams();
//                        lp.height = width;
//                        vpPreview.setLayoutParams(lp);
//                    }
//                });
//            }
//        });


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
                        showDetailActivity();
                    }
                });

                if (User.hasLikedVidtrain(ParseUser.getCurrentUser(), vidTrain.getObjectId())) {
                    liked = true;
                    ibtnLike.setImageResource(R.drawable.heart_icon_red);
                }

                tvLikeCount.setText(String.valueOf(vidTrain.getLikes()));

                titleTv.setText(vidTrain.getTitle());
                int videoCount = vidTrain.getVideosCount();
                String totalVideos = getResources().getQuantityString(R.plurals.videos_count,
                        videoCount, videoCount);
                btnWatchVideos.setText(String.format("View %s", totalVideos));
//                tvTime.setText(Utility.getRelativeTime(vidTrain.getCreatedAt().getTime()));

                final ParseUser user = vidTrain.getUser();
                user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        if (object == null) {
                            return;
                        }
                        String profileImageUrl = User.getProfileImageUrl((ParseUser) object);
                        if (getContext() != null) {
                            Glide.with(getContext()).load(profileImageUrl).into(ivCollaborators);
                        }
                    }
                });


                final List<Video> videos = vidTrain.getVideos();
                vpPreview.setAdapter(new ImagePagerAdapter(getContext(), videos));
                cpIndicator.setViewPager(vpPreview);
//                int dpRadius = (int) getResources().getDisplayMetrics().density * 3;
//                cpIndicator.setRadius(dpRadius);
//                int dpWidth = (int) getResources().getDisplayMetrics().density * 2;
//                cpIndicator.setStrokeWidth(dpWidth);
//                cpIndicator.invalidate();
//                cpIndicator.requestLayout();

                final int PROGRESS_INTERVAL = 750;
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
        tvLikeCount.setText(String.valueOf(vidTrain.getLikes()));
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btnWatchVideos)
    public void onWatchVideosButtonClicked(View v) {
        showDetailActivity();
    }

    public void showDetailActivity() {
        if (vidTrain != null) {
            Intent i = new Intent(getContext(), VidTrainDetailActivity.class);
            i.putExtra(VidTrainDetailActivity.VIDTRAIN_KEY, vidTrain.getObjectId());
            android.support.v4.util.Pair<View, String> p1 = android.support.v4.util.Pair.create((View) ivCollaborators, "collaboratorImage");
            android.support.v4.util.Pair<View, String> p2 = android.support.v4.util.Pair.create((View) vpPreview, "viewer");
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity() ,p1 , p2);
            getContext().startActivity(i, options.toBundle());
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.ivCollaborators)
    public void onCollaboratorClicked(View view) {
        if (vidTrain != null) {
            ParseUser user = vidTrain.getUser();
            Intent intent = new Intent(getContext(), ProfileActivity.class);
            intent.putExtra(ProfileActivity.USER_ID, user.getObjectId());
            android.support.v4.util.Pair<View, String> p1 = android.support.v4.util.Pair.create((View) ivCollaborators, "collaboratorImage");
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), p1);
            getContext().startActivity(intent, options.toBundle());
        }
    }

    @Override
    public void onPause() {
        HomeActivity homeActivity = (HomeActivity) getActivity();
        homeActivity.enterReveal();
        super.onPause();
    }
}
