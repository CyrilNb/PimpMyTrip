package fr.univtln.cniobechoudayer.pimpmytrip.utils;

import android.widget.Toast;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.app.Activity;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import fr.univtln.cniobechoudayer.pimpmytrip.R;

import static android.support.design.widget.Snackbar.*;

/**
 * Helper class to provide some useful tools
 * Created by Cyril Niobé on 21/11/2017.
 */

public class Utils {

    public static int convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int dp = (int) (px / ((int)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

    /**
     * Displaying a snackbar with a specific message
     *
     * @param message message to display
     */
    public static void displayErrorMessage(Context context, Activity activity, View view, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Snackbar snackbar = make(view, message, LENGTH_LONG);

            // Changing action button text color
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(context.getResources().getColor(R.color.colorPrimary));
            snackbar.show();
        }
        else{
            Toast.makeText(activity,message, Toast.LENGTH_SHORT).show();
        }
    }

    public static String formatedTripDistance(double distance) {
        String unit = "m";
        if (distance < 1) {
            distance *= 1000;
            unit = "m";
        } else if (distance > 1000) {
            distance /= 1000;
            unit = "kms";
        }

        return String.format("%4.3f%s", distance, unit);
    }


}
