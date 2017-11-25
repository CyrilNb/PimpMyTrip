package fr.univtln.cniobechoudayer.pimpmytrip.Utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import fr.univtln.cniobechoudayer.pimpmytrip.R;

/**
 * Helper class to provide some useful tools
 * Created by Cyril Niobé on 21/11/2017.
 */

public class Utils {

    /**
     * Displaying a snackbar with a specific message
     *
     * @param message message to display
     */
    public static void displayErrorMessage(Context context, Activity activity, View view, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);

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
}
