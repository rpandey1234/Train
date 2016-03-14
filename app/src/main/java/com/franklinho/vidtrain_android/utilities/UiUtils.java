package com.franklinho.vidtrain_android.utilities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by VRAJA03 on 3/13/2016.
 */

public final class UiUtils {
    private UiUtils() {
        throw new AssertionError();
    }

    /**
     * Converts Density Independent Pixels (DIPs) into pixels.
     *
     * @param context reference to the application's context
     * @param dips    desired DIPs to be converted into pixels
     * @return pixels
     */
    public static int dipsToPixels(final Context context, final int dips) {
        int pixels = dips;

        pixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dips, context.getResources().getDisplayMetrics());

        return pixels;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        float dp = px / (context.getResources().getDisplayMetrics().densityDpi / 160f);
        return dp;
    }

    /**
     * Enable or disable all the {@code Views} within the provided
     * {@code ViewGroup}.
     *
     * @param targetViewGroup {@code ViewGroup} to enable/disable
     * @param enable          true to enable all view within the {@code targetViewGroup},
     *                        else, false
     */
    public static void enableViewGroup(ViewGroup targetViewGroup,
                                       final boolean enable) {
        for (int childIndex = 0; childIndex < targetViewGroup.getChildCount(); childIndex++) {
            View child = targetViewGroup.getChildAt(childIndex);
            child.setEnabled(enable);

            if (child instanceof ViewGroup) {
                enableViewGroup((ViewGroup) child, enable);
            }
        }
    }

    /**
     * Set {@code Menu} visibility.
     *
     * @param menu    target menu
     * @param visible true to make menu visible, else, false
     */
    public static void setMenuItemsVisility(Menu menu, final boolean visible) {
        for (int index = 0; index < menu.size(); index++) {
            menu.getItem(index).setVisible(visible);
        }
    }

    /**
     * Compatibility utility method for setting an alpha on a view
     *
     * @param targetView
     *            view where alpha is to be set
     * @param alpha
     *            value between 0.0f and 1.0f, with 0.0f being completely
     *            transparent
     */

    /**
     * Retrieve the MenuItem's location on the screen.
     *
     * @param activityContainer Activity where the MenuItem is
     * @param menuItem          MenuItem
     * @return position location on screen
     */
    public static int[] getMenuItemLocation(Activity activityContainer,
                                            final int menuItemId) {
        int[] position = new int[2];

        View inflatedMenu = activityContainer.findViewById(menuItemId);
        if (inflatedMenu != null) {
            inflatedMenu.getLocationOnScreen(position);
        }

        return position;
    }

    /**
     * Cleans/erases color in the Bitmap's pixels using the RGB integer values.
     *
     * @param targetBitmap the Bitmap to be modified
     * @param intRGB       RGB integer values
     * @return modified Bitmap
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public static Bitmap eraseColor(Bitmap targetBitmap, int intRGB) {
        int width = targetBitmap.getWidth();
        int height = targetBitmap.getHeight();
        Bitmap b = targetBitmap.copy(Bitmap.Config.ARGB_8888, true);
        b.setHasAlpha(true);

        int[] pixels = new int[width * height];
        targetBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < width * height; i++) {
            if (pixels[i] == intRGB) {
                pixels[i] = 0;
            }
        }

        b.setPixels(pixels, 0, width, 0, 0, width, height);

        return b;
    }

    public static void setViewGroupVisibility(View view, int visibility) {
        view.setVisibility(visibility);
        if (view instanceof ViewGroup) {
            for (int childIndex = 0; childIndex < ((ViewGroup) view).getChildCount(); childIndex++) {
                View child = ((ViewGroup) view).getChildAt(childIndex);
                child.setVisibility(visibility);
                if (child instanceof ViewGroup) {
                    setViewGroupVisibility((ViewGroup) child, visibility);
                }
            }
        }
    }


}
