package com.tajos.graedeviewer.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class GraedeViewerUtil {

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, @NonNull Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, @NonNull Context context){
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * Show a toast message in a short period of time
     * @param cn The context application
     * @param msg The message to be toast
     */
    public static void toastShort(Context cn, String msg) {
        Toast.makeText(cn, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show a toast message in a long period of time
     * @param cn The context application
     * @param msg The message to be toast
     */
    public static void toastLong(Context cn, String msg) {
        Toast.makeText(cn, msg, Toast.LENGTH_LONG).show();
    }
}
