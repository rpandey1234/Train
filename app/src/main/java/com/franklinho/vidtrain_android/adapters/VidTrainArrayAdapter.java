package com.franklinho.vidtrain_android.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.adapters.holders.VidTrainViewHolder;
import com.franklinho.vidtrain_android.models.DynamicHeightVideoPlayerManagerView;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
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

/**
 * Created by franklinho on 3/7/16.
 */
public class VidTrainArrayAdapter extends RecyclerView.Adapter<VidTrainViewHolder> {
    private List<VidTrain> mVidTrains;
    private Context context;



    public VidTrainArrayAdapter( List<VidTrain> vidTrains, Context context) {
        mVidTrains = vidTrains;
        this.context = context;
    }


    private VideoPlayerManager<MetaData> mVideoPlayerManager = new SingleVideoPlayerManager(new PlayerItemChangeListener() {
        @Override
        public void onPlayerItemChanged(MetaData metaData) {

        }
    });

    @Override
    public VidTrainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item_vidtrain, parent, false);
        VidTrainViewHolder viewHolder = new VidTrainViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(VidTrainViewHolder holder, int position) {
        VidTrainViewHolder vidTrainViewHolder = (VidTrainViewHolder) holder;
        configureVidTrainViewHolder( vidTrainViewHolder, position);

    }

    @Override
    public int getItemCount() {
        return mVidTrains.size();
//        return 5;
    }

    private void configureVidTrainViewHolder(final VidTrainViewHolder holder, int position) {
        final VidTrain vidTrain = mVidTrains.get(position);
        holder.vidTrain = vidTrain;

        final ImageView ivCollaborators = holder.ivCollaborators;
        final DynamicHeightVideoPlayerManagerView vvPreview = holder.vvPreview;
        ImageButton ibtnLike = holder.ibtnLike;
        TextView tvLikeCount = holder.tvLikeCount;
        TextView tvCommentCount = holder.tvCommentCount;

        ivCollaborators.setImageResource(0);

        vidTrain.getUser().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                String profileImageUrl = ((ParseUser) vidTrain.getUser()).getString("profileImageUrl");
                Glide.with(holder.context).load(profileImageUrl).into(ivCollaborators);
            }
        });


        vvPreview.setHeightRatio(1);

        vvPreview.setVisibility(View.VISIBLE);
//        vvPreview.addMediaPlayerListener(new SimpleMainThreadMediaPlayerListener() {
//            @Override
//            public void onVideoCompletionMainThread() {
//                vvPreview.start();
//            }
//        });
////
//        holder.vidTrain.mVideoPlayerManager = mVideoPlayerManager;
//        holder.vidTrain.mDirectUrl = ((ParseFile) vidTrain.get("thumbnail")).getUrl();

        final ParseFile parseFile = ((ParseFile) vidTrain.get("thumbnail"));
//        if (parseFile != null) {
//            mVideoPlayerManager.playNewVideo(null, vvPreview, parseFile.getUrl());
//        }


        parseFile.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                try {
//                                FileUtils.writeByteArrayToFile(getOutputMediaFile(vidTrain.getObjectId().toString()), data);

                    File videoFile = getOutputMediaFile(vidTrain.getObjectId().toString());
                    FileOutputStream out;

                    out = new FileOutputStream(videoFile);
                    out.write(data);
                    out.close();

                    vvPreview.addMediaPlayerListener(new SimpleMainThreadMediaPlayerListener() {
                        @Override
                        public void onVideoCompletionMainThread() {
                            Toast.makeText(holder.context, "Video has been prepared from:\n" + parseFile.getUrl().toString() + "Video has been saved to :\n" + getOutputMediaFile(vidTrain.getObjectId().toString()), Toast.LENGTH_LONG).show();
                            vvPreview.start();
                        }
                    });
                    mVideoPlayerManager.playNewVideo(null, vvPreview, getOutputMediaFile(vidTrain.getObjectId().toString()).getPath());
                } catch (FileNotFoundException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    Log.d("TAG", "Error: " + e1.toString());
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
//                try {
//                    FileUtils.writeByteArrayToFile(getOutputMediaFile(vidTrain.getObjectId().toString()), data);
//                    vvPreview.setDataSource(getOutputMediaFileUri(vidTrain.getObjectId()).toString());
////                    vvPreview.setDataSource(holder.context, getOutputMediaFileUri(vidTrain.getObjectId().toString()));
//                    vvPreview.setVolume(0, 0);
//                    vvPreview.setLooping(true);
//                    vvPreview.prepare(new MediaPlayer.OnPreparedListener() {
//                        @Override
//                        public void onPrepared(MediaPlayer mp) {
////                    vvPreview.start();
//                            Toast.makeText(holder.context, "Video has been prepared from:\n" + parseFile.getUrl().toString(), Toast.LENGTH_LONG).show();
//
//                        }
//                    });
//                } catch (IOException ioe) {
//                    Toast.makeText(holder.context, "IOException:" + ioe.toString(), Toast.LENGTH_LONG).show();
//                }
            }
        });

    }

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(String objectId)
    {
        return Uri.fromFile(getOutputMediaFile(objectId));
    }
    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(String objectId)
    {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES), "VidTrainApp");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("VidTrainApp", "failed to create directory");
                return null;
            }
        }
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "VID_CAPTURED" + objectId+ ".mp4");
        return mediaFile;
    }
}
