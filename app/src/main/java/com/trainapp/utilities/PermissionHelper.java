package com.trainapp.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.trainapp.networking.VidtrainApplication;

/**
 * A helper class to manage permissions
 */
public class PermissionHelper {

    public static final int REQUEST_VIDEO = 0;
    public static final String[] PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    public static boolean allPermissionsAlreadyGranted(Activity activity) {
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                    == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    public static boolean allPermissionsGranted(String[] permissions, int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                Log.d(VidtrainApplication.TAG, "Permission " + permissions[i] + " not granted");
                return false;
            }
        }
        return true;
    }

    public static boolean shouldShowRequestPermissionRationale(Activity activity) {
        for (String permission : PERMISSIONS) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Requests the permissions for recording video.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     *
     * Note this won't trigger the callback if called from a fragment.
     */
    public static void requestVideoPermission(final Activity activity, @Nullable View layout) {
        Log.i(VidtrainApplication.TAG, "Permissions have NOT been granted. Requesting permission.");
        if (shouldShowRequestPermissionRationale(activity)) {
            // Provide an additional rationale to the user if the permission was not granted
            // (e.g. if the user has previously denied the permission.)
            Log.i(VidtrainApplication.TAG, "Displaying rationale to provide additional context.");
            if (layout != null) {
                Snackbar.make(layout, "We need some permissions to make a video!",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("ok", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(activity, PERMISSIONS,
                                        REQUEST_VIDEO);
                            }
                        }).show();
            }
        } else {
            // Camera permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(activity, PERMISSIONS, REQUEST_VIDEO);
        }
    }
}
