package fr.univtln.cniobechoudayer.pimpmytrip.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

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

}
