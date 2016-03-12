package com.franklinho.vidtrain_android.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.models.DynamicHeightVideoPlayerManagerView;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.volokh.danylo.video_player_manager.manager.PlayerItemChangeListener;
import com.volokh.danylo.video_player_manager.manager.SingleVideoPlayerManager;
import com.volokh.danylo.video_player_manager.manager.VideoPlayerManager;
import com.volokh.danylo.video_player_manager.meta.MetaData;
import com.volokh.danylo.video_player_manager.ui.SimpleMainThreadMediaPlayerListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Created by rahul on 3/11/16.
 */
public class CustomWindowAdapter implements InfoWindowAdapter {
    LayoutInflater mInflater;
    Map<String, VidTrain> mVidTrainMap;

    private VideoPlayerManager<MetaData> mVideoPlayerManager;

    public CustomWindowAdapter(LayoutInflater layoutInflater, Map<String, VidTrain> vidTrainsMap,
            VideoPlayerManager<MetaData> videoPlayerManager) {
        mInflater = layoutInflater;
        mVidTrainMap = vidTrainsMap;
        mVideoPlayerManager = videoPlayerManager;
    }

    @Override
    public View getInfoContents(Marker marker) {
        String snippet = marker.getSnippet();
        final VidTrain vidTrain = mVidTrainMap.get(snippet);

        final ParseFile parseFile = ((ParseFile) vidTrain.get("thumbnail"));

        // Getting view from the layout file
        View v = mInflater.inflate(R.layout.custom_info_window, null);
        // Populate fields
        TextView title = (TextView) v.findViewById(R.id.tv_info_window_title);
        title.setText(marker.getTitle());

        final DynamicHeightVideoPlayerManagerView vvPreview
                = (DynamicHeightVideoPlayerManagerView) v.findViewById(R.id.vvPreview);

        vvPreview.setHeightRatio(1);
        Log.d("Vidtrain", "hello outside");
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
//                    vvPreview.setOnClickListener(new OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Log.d("Vidtrain", "hello " + v);
//                            mVideoPlayerManager.playNewVideo(
//                                    null,
//                                    vvPreview,
//                                    VidTrainArrayAdapter.getOutputMediaFile(
//                                            vidTrain.getObjectId().toString()).getPath());
//                        }
//                    });
                    Log.d("Vidtrain", "hello after file loaded");
                    mVideoPlayerManager.playNewVideo(
                            null,
                            vvPreview,
                            VidTrainArrayAdapter.getOutputMediaFile(
                                    vidTrain.getObjectId().toString()).getPath());

                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    Log.d("TAG", "Error: " + e1.toString());
                }
            }
        });

        // Return info window contents
        return v;
    }

    // This changes the frame of the info window; returning null uses the default frame.
    // This is just the border and arrow surrounding the contents specified above
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }
}
