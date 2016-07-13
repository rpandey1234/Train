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

import com.facebook.GraphResponse;
import com.franklinho.vidtrain_android.models.User;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.parse.ParseFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        if (!mediaStorageDir.exists()) {
            // if directory already created, then do nothing
            // else create directory
            if (!mediaStorageDir.mkdirs()) {
                Log.d(VidtrainApplication.TAG, "failed to create directory");
                mediaStorageDir.mkdir();
                return mediaStorageDir;
//                return null;
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

    public static ParseFile createParseFileFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return new ParseFile("thumbnail.bmp", byteArray);
    }

    public static Bitmap getImageBitmap(String filePath) {
        return ThumbnailUtils.createVideoThumbnail(filePath, Thumbnails.MINI_KIND);
    }

    // Equality check on ParseUser fails, so we need this helper method :(
    public static boolean contains(List<User> users, User user) {
        if (users == null || user == null) {
            return false;
        }
        for (User pUser : users) {
            if (user.getObjectId().equals(pUser.getObjectId())) {
                return true;
            }
        }
        return false;
    }

    public static List<VidTrain> filterVisibleVidtrains(List<VidTrain> vidtrains) {
        List<VidTrain> visibleVidtrains = new ArrayList<>();
        User user = User.getCurrentUser();
        for (VidTrain vidtrain : vidtrains) {
            if (contains(vidtrain.getCollaborators(), user)) {
                visibleVidtrains.add(vidtrain);
            }
        }
        return visibleVidtrains;
    }

    public static List<String> getFacebookFriends(GraphResponse response, String key) {
        List<String> friends = new ArrayList<>();
        JSONObject jsonObject = response.getJSONObject();
        try {
            // TODO: need to account for paging
            JSONArray data = jsonObject.getJSONArray("data");
            for (int i = 0; i < data.length(); i++) {
                JSONObject friendData = data.getJSONObject(i);
                friends.add(friendData.getString(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return friends;
    }

    public static List<String> getCandidateUsers(List<String> names, String startsWith) {
        String lowerCasePrefix = startsWith.toLowerCase();
        List<String> candidates = new ArrayList<>();
        for (String name : names) {
            if (name.toLowerCase().startsWith(lowerCasePrefix)) {
                candidates.add(name);
            }
        }
        return candidates;
    }
}
