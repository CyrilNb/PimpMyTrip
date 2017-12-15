package fr.univtln.cniobechoudayer.pimpmytrip.services;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
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

public class RecordUserLocationService extends IntentService implements LocationListener {

    private List<Position> positionList = new ArrayList<>();
    private LocationManager locationManager;
    private Messenger messenger;
    private Message msg;
    private static Intent currentIntent;
    private Bundle retrievedBundle;

    private static final String TAG = "Debug GPS Service";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    LocationListener mLocationListener;

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
        } else {
            Log.d("passed bundle", "null");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("onCreate", "reached");
        initializeLocationManager();

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // A new location update is received.  Do something useful with it

                String latitude = "latitude: " + location.getLatitude();
                String longitude = "longitude: " + location.getLongitude();
                String toastString = "location is" + latitude + "," +longitude;
                Log.d(TAG, toastString);

            }
            @Override
            public void onProviderDisabled(String provider) {
                // No code here
            }

            @Override
            public void onProviderEnabled(String provider) {
                // No code here
            }

            @Override
            public void onStatusChanged(String provider, int status,Bundle extras)
            {
                // No code here
            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListener);
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null || locationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
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
        if (msg != null) {
            if (messenger != null) {
                try {
                    messenger.send(msg);
                    Log.d("service", "message sent !");
                } catch (RemoteException e) {
                    Log.i("error", "error");
                }
            } else {
                Log.d("messenger service", "null");
            }

        } else {
            Log.d("message service", "null");
        }
        super.onDestroy();
        if(locationManager != null){
            locationManager.removeUpdates(mLocationListener);
        }
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
            Log.d("permission location", "denied");
        }


    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, String.valueOf(location.getAltitude()));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
