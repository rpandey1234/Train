package com.trainapp.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore.Video.Thumbnails;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Surface;
import android.view.WindowManager;
import com.google.common.base.Joiner;
import com.google.common.io.Files;

import com.crashlytics.android.Crashlytics;
import com.facebook.GraphResponse;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.trainapp.R;
import com.trainapp.activities.VidTrainDetailActivity;
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
    public static final String MESSAGE_EXTRA_INTENT = "MESSAGE_EXTRA";
    public static final int VIDEO_CAPTURE = 101;
    public static final String PREFS_NAME = "com.trainapp";
    public static final String BADGE_COUNT = "BADGE_COUNT";

    /**
     * Gets the relative time from now for the time passed in
     */
    public static String getRelativeTime(long time) {
        return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(),
                DateUtils.SECOND_IN_MILLIS).toString();
    }

    /**
     * Create a File for saving an image or video
     */
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
     * Equality checks on ParseObject fails, so we need this helper method :(
     * Returns the index of the contained object, or -1 if not found.
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

    public static boolean remove(List<? extends ParseObject> objects, String objectId) {
        int index = indexOf(objects, objectId);
        if (index != -1) {
            objects.remove(index);
            return true;
        }
        return false;
    }

    public static List<String> getFacebookFriends(GraphResponse response, String key) {
        List<String> friends = new ArrayList<>();
        JSONObject jsonObject = response.getJSONObject();
        if (jsonObject == null) {
            Crashlytics.log("Null response from Facebook");
            if (response.getError() != null) {
                Crashlytics.setString("GraphResponse", response.getError().getErrorMessage());
            }
            return friends;
        }
        try {
            // TODO: need to account for paging
            JSONArray data = jsonObject.getJSONArray("data");
            for (int i = 0; i < data.length(); i++) {
                JSONObject friendData = data.getJSONObject(i);
                friends.add(friendData.getString(key));
            }
        } catch (JSONException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
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
            data.put("alert", User.getCurrentUser().getName());
            data.put("title", context.getString(R.string.app_name));
            data.put("badge", "Increment");
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
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
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

    public static void goVidtrainDetail(Context context, String vidtrainId) {
        Intent i = new Intent(context, VidTrainDetailActivity.class);
        i.putExtra(VidTrainDetailActivity.VIDTRAIN_KEY, vidtrainId);
        context.startActivity(i);
    }

    public static void updateBadgeCount(Context context, int count) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count);
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);
        context.sendBroadcast(intent);
    }

    public static String getLauncherClassName(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                return resolveInfo.activityInfo.name;
            }
        }
        return null;
    }

    public static int getBadgeCount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(BADGE_COUNT, 0);
    }

    public static void setBadgeCount(Context context, int count) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putInt(BADGE_COUNT, count);
        editor.apply();
        updateBadgeCount(context, count);
    }

    public static String generateTitle(List<User> users, Resources resources) {
        return generateTitle(users, -1, resources);
    }

    public static String generateTitle(List<User> users, int limit, Resources resources) {
        if (users == null) {
            return null;
        }
        List<String> usernames = new ArrayList<>();
        for (User user : users) {
            usernames.add(user.getName());
        }
        return formatNames(usernames, limit, resources);
    }

    /**
     * Given a list of names, return a comma separated string of those names.
     * formatNames(["A, "B, "C"], 5, resources) ==> "A, B, C"
     * formatNames(["A", "B", "C"], 2, resources) ==> "A, B, 1 other"
     * @param limit the amount of names to show in the list, the remaining will be shown
     * as "x other(s)" at the end of the list
     */
    public static String formatNames(List<String> names, int limit, Resources resources) {
        if (names == null) {
            return null;
        }
        if (limit != -1 && names.size() > limit) {
            int numLeft = names.size() - limit;
            names = names.subList(0, limit);
            names.add(resources.getQuantityString(R.plurals.others_plurals, numLeft, numLeft));
        }
        return Joiner.on(", ").join(names);
    }

    public static int dpToPixels(Resources resources, int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }
}
