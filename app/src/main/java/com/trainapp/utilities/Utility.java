package com.trainapp.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore.Video.Thumbnails;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;
import com.google.common.io.Files;

import com.facebook.GraphResponse;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.trainapp.R;
import com.trainapp.activities.VideoCaptureActivity;
import com.trainapp.models.User;
import com.trainapp.models.VidTrain;
import com.trainapp.models.Video;
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
    public static final String UNIQUE_ID_INTENT = "UNIQUE_ID";
    public static final int VIDEO_CAPTURE = 101;

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
        return new File(mediaStorageDir.getPath() + File.separator + "VID" + objectId + ".mp4");
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
        if (object == null) {
            return -1;
        }
        return indexOf(objects, object.getObjectId());
    }

    public static int indexOf(List<? extends ParseObject> objects, String objectId) {
        if (objects == null || objectId == null) {
            return -1;
        }
        for (int i = 0; i < objects.size(); i++) {
            if (objectId.equals(objects.get(i).getObjectId())) {
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

    public static void sendNotification(VidTrain vidtrain, Context context) {
        List<User> collaborators = vidtrain.getCollaborators();
        for (User user : collaborators) {
            if (!user.getObjectId().equals(User.getCurrentUser().getObjectId())) {
                Utility.sendNotification(user, vidtrain, context);
            }
        }
    }

    public static void sendNotification(User user, VidTrain vidtrain, Context context) {
        JSONObject data = new JSONObject();
        try {
            String notficationText = context.getString(R.string.sent_notification_text,
                    User.getCurrentUser().getName());
            data.put("alert", notficationText);
            data.put("title", context.getString(R.string.app_name));
            data.put(Video.VIDTRAIN_KEY, vidtrain.getObjectId());
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

    // Reference: https://developer.android.com/reference/android/hardware/Camera.html#setDisplayOrientation(int)
    public static void setCameraDisplayOrientation(WindowManager windowManager, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        int rotation = windowManager.getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        // always seems to be 0
        Log.d(VidtrainApplication.TAG, "degrees: " + degrees);

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
            // Hack so that the resulting video is straight.
            result = (result + 180) % 360;
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        Log.d(VidtrainApplication.TAG, "result: " + result);
        VideoCaptureActivity.orientation = result;
        camera.setDisplayOrientation(result);
    }
}
