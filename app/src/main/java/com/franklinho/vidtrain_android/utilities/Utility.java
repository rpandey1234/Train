package com.franklinho.vidtrain_android.utilities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.Thumbnails;
import android.text.format.DateUtils;
import android.util.Log;
import com.google.common.io.Files;

import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by rahul on 3/12/16.
 */
public class Utility {

    public static final String FILENAME = "video.mp4";

    /**
     * Gets the relative time from now for the time passed in
     */
    public static String getRelativeTime(long time) {
        return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(),
                DateUtils.SECOND_IN_MILLIS).toString();
    }

    /** Create a File for saving an image or video */
    public static File getOutputMediaFile(String objectId)
    {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES), "VidTrainApp");
        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                Log.d(VidtrainApplication.TAG, "failed to create directory");
                return null;
            }
        }
        return new File(mediaStorageDir.getPath() + File.separator + "VID_CAPTURED" + objectId + ".mp4");
    }

    /** Create a file Uri for saving an image or video */
    public static Uri getOutputMediaFileUri()
    {
        return Uri.fromFile(getOutputMediaFile());
    }

    /** Create a File for saving an image or video */
    public static File getOutputMediaFile()
    {
        return getOutputMediaFile("");
    }

    public static void writeToFile(byte[] data, File file) {
        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(data);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Intent getVideoIntent() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getOutputMediaFileUri());
        return intent;
    }

    public static ParseFile createParseFile(String path) {
        try {
            byte[] videoFileData = Files.toByteArray(new File(path));
            return new ParseFile(FILENAME, videoFileData);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getImageBitmap(String filePath) {
        return ThumbnailUtils.createVideoThumbnail(filePath, Thumbnails.MINI_KIND);
    }

    // Equality check on ParseUser fails, so we need this helper method :(
    public static boolean contains(List<ParseUser> users, ParseUser user) {
        for (ParseUser pUser : users) {
            if (user.getObjectId().equals(pUser.getObjectId())) {
                return true;
            }
        }
        return false;
    }
}
