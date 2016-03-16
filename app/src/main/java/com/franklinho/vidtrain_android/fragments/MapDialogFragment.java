package com.franklinho.vidtrain_android.fragments;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.activities.VidTrainDetailActivity;
import com.franklinho.vidtrain_android.models.DynamicVideoPlayerView;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.franklinho.vidtrain_android.utilities.VideoPlayer;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by rahul on 3/11/16.
 */
public class MapDialogFragment extends DialogFragment {

    @Bind(R.id.vvPreview) DynamicVideoPlayerView vvPreview;
    @Bind(R.id.tvTitle) TextView titleTv;
    @Bind(R.id.tvVideoCount) TextView tvVideoCount;
    @Bind(R.id.tvTime) TextView tvTime;
    @Bind(R.id.tvLikeCount) TextView tvLikeCount;
    @Bind(R.id.ibtnLike) ImageButton ibtnLike;

    boolean liked = false;
    VidTrain vidTrain;
    File videoFile;

    public static final String VIDTRAIN_ID = "vidTrainId";

    public MapDialogFragment() {}

    public static MapDialogFragment newInstance(String vidTrainId) {
        MapDialogFragment fragment = new MapDialogFragment();
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
        final View v = inflater.inflate(R.layout.custom_info_window, container);
        ButterKnife.bind(this, v);
        vvPreview.setHeightRatio(1);
        final String vidTrainId = getArguments().getString(VIDTRAIN_ID);
        ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
        LocationManager lm = (LocationManager) getContext().getSystemService(
                Context.LOCATION_SERVICE);
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
                vvPreview.setHeightRatio(1);
                vvPreview.setVisibility(View.VISIBLE);
                final ParseFile parseFile = ((ParseFile) vidTrain.get("thumbnail"));
                parseFile.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] data, ParseException e) {
                        try {
                            videoFile = Utility.getOutputMediaFile(vidTrain.getObjectId());
                            FileOutputStream out = new FileOutputStream(videoFile);
                            out.write(data);
                            out.close();
                            vvPreview.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            });
                            VideoPlayer.playVideo(vvPreview, videoFile.getPath());
                        } catch (FileNotFoundException e1) {
                            e1.printStackTrace();
                            Log.d("TAG", "Error: " + e1.toString());
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                });
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

    @Override
    public void onResume() {
        super.onResume();
        if (videoFile != null) {
            VideoPlayer.playVideo(vvPreview, videoFile.getPath());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
