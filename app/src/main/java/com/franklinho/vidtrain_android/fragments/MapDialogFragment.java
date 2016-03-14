package com.franklinho.vidtrain_android.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.models.DynamicVideoPlayerView;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.franklinho.vidtrain_android.utilities.Utility;
import com.franklinho.vidtrain_android.utilities.VideoPlayer;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by rahul on 3/11/16.
 */
public class MapDialogFragment extends DialogFragment {

    @Bind(R.id.vvPreview) DynamicVideoPlayerView vvPreview;
    @Bind(R.id.tvTitle) TextView titleTv;
    @Bind(R.id.tvVideoCount) TextView tvVideoCount;
    @Bind(R.id.tvTime) TextView tvTime;

    public static final String VIDTRAIN_ID = "vidTrainId";

    public MapDialogFragment() {}

    public static MapDialogFragment newInstance(String vidTrainId) {
        MapDialogFragment frag = new MapDialogFragment();
        Bundle args = new Bundle();
        args.putString(VIDTRAIN_ID, vidTrainId);
        frag.setArguments(args);
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View v = inflater.inflate(R.layout.custom_info_window, container);
        ButterKnife.bind(this, v);
        final String vidTrainId = getArguments().getString(VIDTRAIN_ID);
        ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        query.whereEqualTo("objectId", vidTrainId);
        query.getFirstInBackground(new GetCallback<VidTrain>() {
            @Override
            public void done(final VidTrain vidTrain, ParseException e) {
                if (e != null) {
                    Log.d(VidtrainApplication.TAG, e.toString());
                    return;
                }
                titleTv.setText(vidTrain.getTitle());
                int videoCount = vidTrain.getVideosCount();
                String totalVideos = getResources().getQuantityString(R.plurals.videos_count,
                        videoCount, videoCount);
                tvVideoCount.setText(totalVideos);
                tvTime.setText(Utility.getRelativeTime(vidTrain.getUpdatedAt().getTime()));
                vvPreview.setHeightRatio(1);
                vvPreview.setVisibility(View.VISIBLE);
                final ParseFile parseFile = ((ParseFile) vidTrain.get("thumbnail"));
                parseFile.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] data, ParseException e) {
                        try {
                            File videoFile = Utility.getOutputMediaFile(
                                    vidTrain.getObjectId());
                            FileOutputStream out = new FileOutputStream(videoFile);
                            out.write(data);
                            out.close();
                            vvPreview.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            });
                            VideoPlayer.getVideoPlayer().playNewVideo(null, vvPreview, videoFile.getPath());
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
}
