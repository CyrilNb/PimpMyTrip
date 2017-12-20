package fr.univtln.cniobechoudayer.pimpmytrip.utils;

import android.support.v7.app.AppCompatActivity;
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

    /**
     * Method that convert pixels to dp
     * @param px
     * @param context
     * @return
     */
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

    /**
     * Method that returns a well formatted trip distance
     * @param distance
     * @return
     */
    public static String formatTripDistance(double distance) {
        String unit = " m";
        if (distance < 1) {
            distance *= 1000;
            unit = " m";
        } else if (distance >= 1000) {
            distance /= 1000;
            if(distance == 1.000)
                unit = " km";
            else
                unit = " kms";
        }

        return String.format("%4.3f%s", distance, unit);
    }

    /**
     * Method that returns a well formatted trip duration
     * @param duration
     * @return
     */
    public static String formatTripTime(int duration){
        String finalresult = "";
        Double computedDuration = 0.0;
        if(duration > 60){
            computedDuration = Double.valueOf(duration/60);
            finalresult = computedDuration + "mins";
        } if(computedDuration > 60){
            finalresult = computedDuration + "h";
        }
        return finalresult;
    }

    /**
     * Method to update the action bar title
     */
    public static void setActionBarTitle(AppCompatActivity activity, String titleToDisplay) {
        if (activity.getSupportActionBar() != null)
            activity.getSupportActionBar().setTitle(titleToDisplay);
    }


}
