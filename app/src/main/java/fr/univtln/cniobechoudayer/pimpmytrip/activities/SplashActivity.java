package fr.univtln.cniobechoudayer.pimpmytrip.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import java.util.Locale;

import fr.univtln.cniobechoudayer.pimpmytrip.authentication.LoginActivity;
import fr.univtln.cniobechoudayer.pimpmytrip.controllers.UserController;

/**
 * Splash activity displaying a splash screen when the app is starting
 * to handle loading from the database and some settings stuff
 */
public class SplashActivity extends AppCompatActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setLanguage();
        UserController.getInstance();

        try {
            Thread.sleep(1500);

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Method to set up the language
     */
    private void setLanguage() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d("language", prefs.getString("language", "en"));
        Locale myLocale = new Locale(prefs.getString("language", "en"));
        Locale.setDefault(new Locale("en"));
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

}
