package fr.univtln.cniobechoudayer.pimpmytrip.controllers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Messenger;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.services.RecordUserLocationService;
import fr.univtln.cniobechoudayer.pimpmytrip.utils.Utils;

public class MapController {

    private static MapController sInstance;

    private static MapController getInstance(){
        if(sInstance == null)
            sInstance = new MapController();
        return sInstance;
    }

} 
