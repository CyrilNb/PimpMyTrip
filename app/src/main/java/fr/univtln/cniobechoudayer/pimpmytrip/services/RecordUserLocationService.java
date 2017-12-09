package fr.univtln.cniobechoudayer.pimpmytrip.services;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import fr.univtln.cniobechoudayer.pimpmytrip.entities.Position;
import fr.univtln.cniobechoudayer.pimpmytrip.R;

public class RecordUserLocationService extends IntentService {

    private List<Position> positionList = new ArrayList<>();
    public static final String ACTION_MyIntentService = "com.example.androidintentservice.RESPONSE";
    public static final String ACTION_MyUpdate = "com.example.androidintentservice.UPDATE";
    public static final String EXTRA_KEY_IN = "EXTRA_IN";
    public static final String EXTRA_KEY_OUT = "EXTRA_OUT";
    public static final String EXTRA_KEY_UPDATE = "EXTRA_UPDATE";
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private LocationManager locationManager;
    private Messenger messenger;
    private Message msg;
    private static Intent currentIntent;
    private Bundle retrievedBundle;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public RecordUserLocationService(String name) {
        super(name);
    }

    public RecordUserLocationService() {
        super("RecordUserLocationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d("onHandleIntent", "reached");
        Bundle bundle = intent.getExtras();
        retrievedBundle = bundle;
        Log.d("bundle content", String.valueOf(bundle));
        if (bundle != null) {
            Log.d("passed bundle", "not null");
            recordUserPositions(bundle.getBoolean("isUserWalking"));
        }else{
            Log.d("passed bundle", "null");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("onCreate", "reached");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Bundle bundleToSend = new Bundle();
        bundleToSend.putParcelableArrayList("listPositionsTrip", (ArrayList<? extends Parcelable>) positionList);
        msg = Message.obtain();
        msg.setData(bundleToSend); //put the data here
        messenger = (Messenger) retrievedBundle.get("messenger");
        if(msg != null){
            if(messenger != null){
                try {
                    messenger.send(msg);
                    Log.d("service","message sent !");
                } catch (RemoteException e) {
                    Log.i("error", "error");
                }
            }else{
                Log.d("messenger service", "null");
            }

        }else{
            Log.d("message service", "null");
        }
        super.onDestroy();
    }

    /**
     * Method that records positions of user when manual mode is selected
     * With specific positions refresh intervals
     */
    private void recordUserPositions(boolean isUserWalking) {

        int intervalInMs = 0;

        if (isUserWalking) {
            intervalInMs = getResources().getInteger(R.integer.intervalRecordingUserByWalk);
        } else {
            intervalInMs = getResources().getInteger(R.integer.intervalRecordUserBySUV);
        }

        Criteria criteria = new Criteria();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if(location != null){
                while(true){
                    positionList.add(new Position(location.getLatitude(), location.getLongitude()));
                    try {
                        Thread.sleep(intervalInMs);
                    } catch (InterruptedException e) {

                    }
                    Log.d("positionsList size", String.valueOf(positionList.size()));
                    Log.d("added element", String.valueOf(location.getLatitude() + location.getLongitude()));
                }
            }else{
                Log.d("Location in service", "null");
            }

        }else{

        }


    }
}
