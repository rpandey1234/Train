package com.franklinho.vidtrain_android.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.adapters.VidTrainArrayAdapter;
import com.franklinho.vidtrain_android.models.DynamicHeightVideoPlayerManagerView;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.models.Video;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.volokh.danylo.video_player_manager.manager.PlayerItemChangeListener;
import com.volokh.danylo.video_player_manager.manager.SingleVideoPlayerManager;
import com.volokh.danylo.video_player_manager.manager.VideoPlayerManager;
import com.volokh.danylo.video_player_manager.meta.MetaData;
import com.volokh.danylo.video_player_manager.ui.SimpleMainThreadMediaPlayerListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by rahul on 3/11/16.
 */
public class MapDialogFragment extends DialogFragment {

    @Bind(R.id.vvPreview) DynamicHeightVideoPlayerManagerView vvPreview;
    @Bind(R.id.tv_info_window_title) TextView titleTv;

    VidTrain vidTrain;
    private VideoPlayerManager<MetaData>
            mVideoPlayerManager = new SingleVideoPlayerManager(new PlayerItemChangeListener() {
        @Override
        public void onPlayerItemChanged(MetaData metaData) {

        }
    });

    public MapDialogFragment() {}

    public static MapDialogFragment newInstance(String vidTrainId) {
        MapDialogFragment frag = new MapDialogFragment();
        Bundle args = new Bundle();
        args.putString("vidTrainId", vidTrainId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View v = inflater.inflate(R.layout.custom_info_window, container);
        ButterKnife.bind(this, v);
        final String vidTrainId = getArguments().getString("vidTrainId");
        ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        query.whereEqualTo("objectId", vidTrainId);;

        query.setLimit(1);
        query.findInBackground(new FindCallback<VidTrain>() {
            @Override
            public void done(List<VidTrain> objects, ParseException e) {
                if (e == null) {
                    vidTrain = objects.get(0);
                    titleTv.setText(vidTrain.getTitle());
                    String countString = String.format(getString(R.string.video_count),
                            vidTrain.getVideosCount());
//                    tvVideoCount.setText(countString);

                    vidTrain.getUser().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject object, ParseException e) {
                            String profileImageUrl = ((ParseUser) vidTrain.getUser()).getString(
                                    "profileImageUrl");
//                            Glide.with(getBaseContext()).load(profileImageUrl).into(
//                                    ivCollaborators);
                        }
                    });

                    vvPreview.setHeightRatio(1);
                    vvPreview.setVisibility(View.VISIBLE);
                    final ParseFile parseFile = ((ParseFile) vidTrain.get("thumbnail"));
                    parseFile.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] data, ParseException e) {
                            try {
                                File videoFile = VidTrainArrayAdapter.getOutputMediaFile(
                                        vidTrain.getObjectId().toString());
                                FileOutputStream out;

                                out = new FileOutputStream(videoFile);
                                out.write(data);
                                out.close();
                                vvPreview.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                });
                                mVideoPlayerManager.playNewVideo(null, vvPreview,
                                        VidTrainArrayAdapter.getOutputMediaFile(
                                                vidTrain.getObjectId().toString()).getPath());
                            } catch (FileNotFoundException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                                Log.d("TAG", "Error: " + e1.toString());
                            } catch (IOException ioe) {
                                ioe.printStackTrace();
                            }
                        }
                    });
                } else {
//                    invalidVidTrain();
                }
            }
        });


        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
}
