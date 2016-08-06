package com.trainapp.utilities;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Video.Thumbnails;
import android.text.format.DateUtils;
import android.util.Log;
import com.google.common.io.Files;

import com.facebook.GraphResponse;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.trainapp.models.User;
import com.trainapp.models.VidTrain;
import com.trainapp.networking.VidtrainApplication;

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
    public static File getOutputMediaFile(String objectId) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES), "VidTrainApp");
        if (!mediaStorageDir.exists()) {
            // create directory if it doesn't exist
            if (!mediaStorageDir.mkdirs()) {
                Log.d(VidtrainApplication.TAG, "failed to create directory");
                mediaStorageDir.mkdir();
                return mediaStorageDir;
            }
        }
        return new File(
                mediaStorageDir.getPath() + File.separator + "VID_CAPTURED" + objectId + ".mp4");
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

    public static ParseFile createParseFile(String path) {
        if (path == null) {
            return null;
        }
        try {
            byte[] videoFileData = Files.toByteArray(new File(path));
            return new ParseFile(FILENAME, videoFileData);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean deleteFile(String path) {
        if (path == null) {
            return false;
        }
        File file = new File(path);
        boolean deleted = file.delete();
        Log.d(VidtrainApplication.TAG, deleted ? "local video deleted" : "local video not deleted");
        return deleted;
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

    /**
     *  Equality checks on ParseObject fails, so we need this helper method :(
     *  Returns the index of the contained object, or -1 if not found.
     */
    public static int indexOf(List<? extends ParseObject> objects, ParseObject object) {
        if (objects == null || object == null) {
            return -1;
        }
        for (int i = 0; i < objects.size(); i++) {
            if (object.getObjectId().equals(objects.get(i).getObjectId())) {
                return i;
            }
        }
        return -1;
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

    public static void sendNotification(VidTrain vidtrain) {
        List<User> collaborators = vidtrain.getCollaborators();
        for (User user : collaborators) {
            if (!user.getObjectId().equals(User.getCurrentUser().getObjectId())) {
                Utility.sendNotification(user, vidtrain);
            }
        }
    }

    public static void sendNotification(User user, VidTrain vidtrain) {
        JSONObject data = new JSONObject();
        try {
            data.put("alert", User.getCurrentUser().getName() + " sent you a Train!");
            data.put("title", "Train");
            data.put("vidTrain", vidtrain.getObjectId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery()
                .whereEqualTo("user", user.getObjectId());
        ParsePush push = new ParsePush();
        push.setQuery(pushQuery);
        push.setData(data);
        push.sendInBackground();
    }
}
