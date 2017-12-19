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

    private static final String TAG = "Debug GPS RecordUserLocationService";
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    private List<Position> mPositionList = new ArrayList<>();
    private Position mCurrentPosition;
    private LocationManager locationManager;
    private Messenger mMessenger;
    private Message mMessage;
    private Bundle mRetrievedBundle;

    private LocationManager mLocationManager = null;
    private LocationListener mLocationListener;


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
        mRetrievedBundle = bundle;
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
                mCurrentPosition = new Position(location.getLatitude(), location.getLongitude());
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

    /**
     * Initialize location manager in order to get user position
     */
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

    /**
     * Sending positions lists back to the caller of this service
     */
    @Override
    public void onDestroy() {
        sendPositionToCaller();
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
        while(true){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
                if(location != null){
                    mCurrentPosition = new Position(location.getLatitude(), location.getLongitude());

                        mPositionList.add(mCurrentPosition);
                        try {
                            Thread.sleep(intervalInMs);
                        } catch (InterruptedException e) {


                        Log.d("positionsList size", String.valueOf(mPositionList.size()));
                    }
                }else{
                    Log.d("Location in service", "null");
                }

            }else{
                Log.d("permission location", "denied");
            }
        }


    }

    private void sendPositionToCaller(){
        Bundle bundleToSend = new Bundle();
        bundleToSend.putParcelableArrayList("listPositionsTrip", (ArrayList<? extends Parcelable>) mPositionList);
        mMessage = Message.obtain();
        mMessage.setData(bundleToSend); //put the data here
        mMessenger = (Messenger) mRetrievedBundle.get("mMessenger");
        if (mMessage != null) {
            if (mMessenger != null) {
                try {
                    mMessenger.send(mMessage);
                    Log.d("service", "message sent with position : " + mCurrentPosition.toString());
                } catch (RemoteException e) {
                    Log.i("error", "error");
                }
            } else {
                Log.d("mMessenger service", "null");
            }

        } else {
            Log.d("message service", "null");
        }
    }




    /**
     * Implementing LocationListener interface
     */
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
