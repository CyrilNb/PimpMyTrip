package fr.univtln.cniobechoudayer.pimpmytrip.Fragments;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.ui.IconGenerator;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Handler;

import fr.univtln.cniobechoudayer.pimpmytrip.Entities.Position;
import fr.univtln.cniobechoudayer.pimpmytrip.Entities.Trip;
import fr.univtln.cniobechoudayer.pimpmytrip.Entities.Waypoint;
import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.Services.RecordUserLocationService;
import fr.univtln.cniobechoudayer.pimpmytrip.Services.UserLocationReceiver;
import fr.univtln.cniobechoudayer.pimpmytrip.Utils.Utils;
import fr.univtln.cniobechoudayer.pimpmytrip.controllers.UserController;


public class MapFragment extends Fragment implements View.OnClickListener {

    public static MapFragment singleton;
    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private FloatingActionButton buttonRecordTrip;
    private boolean isUserRecording = false;
    private ColorPicker mColorPicker;
    private EditText titleEditText;
    private Button colorButton;
    private AlertDialog.Builder builder;
    private boolean isUserWalkingForRecordingPath = true;
    private UserLocationReceiver userLocationReceiver;

    private List<Trip> listReferenceTrip;
    private List<Trip> listMyTrips;
    private IconGenerator factory;
    private ValueEventListener listenerDbTrips;
    private ValueEventListener listenerDbMyTrips;

    private DatabaseReference dbTrips = FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase").child("trips");
    private DatabaseReference dbMyTrips = (DatabaseReference) dbTrips.child(FirebaseAuth.getInstance().getCurrentUser().getUid());


    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    public MapFragment() {
        // Required empty public constructor
    }

    //Managing the singleton
    public static MapFragment getInstance() {

        if (singleton == null) {
            singleton = new MapFragment();
        }

        return singleton;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        //Setting lists to manage trips to display
        listReferenceTrip = new ArrayList<>();
        listMyTrips = new ArrayList<>();

        //Get floating action button
        buttonRecordTrip = (FloatingActionButton) rootView.findViewById(R.id.buttonRecordTrip);
        buttonRecordTrip.setOnClickListener(this);

        //Setting view for save alert dialog
        titleEditText = new EditText(getContext());
        colorButton = new Button(getContext());

        factory = new IconGenerator(getActivity());

        //Get the mapView
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        mColorPicker = new ColorPicker(getActivity(), 127, 127, 127);
        mColorPicker.setCallback(new ColorPickerCallback() {
            @Override
            public void onColorChosen(@ColorInt int color) {
                // Do whatever you want
                // Examples
                Log.d("Alpha", Integer.toString(Color.alpha(color)));
                Log.d("Red", Integer.toString(Color.red(color)));
                Log.d("Green", Integer.toString(Color.green(color)));
                Log.d("Blue", Integer.toString(Color.blue(color)));

                Log.d("Pure Hex", Integer.toHexString(color));
                Log.d("#Hex no alpha", String.format("#%06X", (0xFFFFFF & color)));
                Log.d("#Hex with alpha", String.format("#%08X", (0xFFFFFFFF & color)));

                mColorPicker.dismiss();
                if(colorButton != null){
                    colorButton.setBackgroundColor(Color.parseColor(String.format("#%06X", (0xFFFFFF & color))));
                }
            }
        });

        //Initializing map
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        /**
         * Loading the map asynchronously and adding a OnMapReadyCallback for displaying locations
         */
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                mGoogleMap = mMap;

                // For showing a move to my location button
                //Checking if user's location is accessible
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //Requesting location permission
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    return;
                }
                mGoogleMap.setMyLocationEnabled(true);

                /**
                 * Loading and displaying the current location of user
                 */
                LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();

                Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
                if (location != null)
                {
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                            .zoom(17)                   // Sets the zoom
                            .build();                   // Creates a CameraPosition from the builder
                    mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        });

        listenerDbTrips = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot tripSnapshot : dataSnapshot.getChildren()) {
                    Trip currentTrip = (Trip) tripSnapshot.getValue(Trip.class);
                    Log.d("New trip retrieved", String.valueOf(currentTrip.getName()));
                    if (currentTrip.isReference()) {
                        listReferenceTrip.add(currentTrip);
                    }
                }

                Log.d("listReferenceTrip size:", String.valueOf(listReferenceTrip.size()));

                MapFragment myFragment = (MapFragment) getActivity().getSupportFragmentManager().findFragmentByTag("MapFragment");
                if (myFragment != null && myFragment.isVisible()) {
                    loadReferenceTrip();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        listenerDbMyTrips = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot tripSnapshot : dataSnapshot.getChildren()) {
                    Trip currentTrip = (Trip) tripSnapshot.getValue(Trip.class);
                    listMyTrips.add(currentTrip);
                }
                Log.d("listMyTrips", "updated");
                MapFragment myFragment = (MapFragment) getActivity().getSupportFragmentManager().findFragmentByTag("MapFragment");
                if (myFragment != null && myFragment.isVisible()) {
                    loadMyTrips();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        return rootView;
    }

    /**
     * Method to react once user gives an requested permission
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch(requestCode){
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:

                break;
        }
    };

    /**
     * Method to update the action bar title
     */
    public void setActionBarTitle() {
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = ((AppCompatActivity) getActivity());
            if (activity.getSupportActionBar() != null)
                activity.getSupportActionBar().setTitle(getString(R.string.titleMap));
        }
    }



    /**
     * Handle different fragment states and adapt the map
     */

    @Override
    public void onStart() {
        super.onStart();
        dbMyTrips.addValueEventListener(listenerDbMyTrips);
        dbTrips.addValueEventListener(listenerDbTrips);
    }

    @Override
    public void onStop() {
        super.onStop();
        dbMyTrips.removeEventListener(listenerDbMyTrips);
        dbTrips.removeEventListener(listenerDbTrips);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.buttonRecordTrip:
                if(isUserRecording){

                    displayAlertDialogSaveTrip();
                }else{
                    displayAlertDialogChoiceTransportationMode();
                    isUserRecording = true;
                    buttonRecordTrip.setImageResource(R.drawable.ic_stop_white_48dp);
                }
                break;
        }
    }

    /**
     * Method that create and display the alert dialog to save a trip to database
     */
    private void displayAlertDialogSaveTrip(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }
        LinearLayout alertLayout = new LinearLayout(getContext());
        alertLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(Utils.convertPixelsToDp(20, getContext()), Utils.convertPixelsToDp(40, getContext()), Utils.convertPixelsToDp(20, getContext()), Utils.convertPixelsToDp(40, getContext()));
        alertLayout.setLayoutParams(params);

        //Setting view for save alert dialog
        titleEditText = new EditText(getContext());
        colorButton = new Button(getContext());

        titleEditText.setHint("Choose a title");
        titleEditText.setSingleLine(false);
        titleEditText.setMaxLines(2);
        titleEditText.setHorizontalScrollBarEnabled(false);
        titleEditText.setHintTextColor(Color.WHITE);
        titleEditText.setTextColor(Color.WHITE);

        colorButton.setText("Choose a color");
        colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mColorPicker.show();
            }
        });

        alertLayout.addView(titleEditText);
        alertLayout.addView(colorButton);


        builder.setTitle("Stop recording & save trip ?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        buttonRecordTrip.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                        isUserRecording = false;
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        isUserRecording = true;
                    }
                })
                .setIcon(android.R.drawable.ic_menu_save)
                .setView(alertLayout)
                .show();
    }

    /**
     * Method that create and displays alert dialog to choose the transporation mode
     */
    private void displayAlertDialogChoiceTransportationMode(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }
        LinearLayout alertLayout = new LinearLayout(getContext());
        alertLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(Utils.convertPixelsToDp(20, getContext()), Utils.convertPixelsToDp(40, getContext()), Utils.convertPixelsToDp(20, getContext()), Utils.convertPixelsToDp(40, getContext()));
        alertLayout.setLayoutParams(params);

        final Spinner choiceTransportationMode = new Spinner(getContext());
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String> (getContext(), android.R.layout.simple_list_item_1 , getResources().getStringArray(R.array.spinnerChoicesTransportationMode));
        choiceTransportationMode.setAdapter(spinnerArrayAdapter);

        alertLayout.addView(choiceTransportationMode);

        builder.setTitle("Choose your transporation mode")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        buttonRecordTrip.setImageResource(R.drawable.ic_stop_white_48dp);
                        isUserRecording = true;
                        if(choiceTransportationMode.getSelectedItemPosition() == 0){
                            isUserWalkingForRecordingPath = true;
                        }else{
                            isUserWalkingForRecordingPath = false;
                        }
                        Intent intentRecordUserLocationService = new Intent(getContext(), RecordUserLocationService.class);
                        intentRecordUserLocationService.putExtra("isUserWalking", isUserWalkingForRecordingPath);
                        getActivity().startService(intentRecordUserLocationService);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        isUserRecording = false;
                        buttonRecordTrip.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                    }
                })
                .setIcon(android.R.drawable.ic_menu_save)
                .setView(alertLayout)
                .show();

    }

    /**
     * Method that loads and display the current referenced trip
     */
    private void loadReferenceTrip(){
        for(Trip refTrip : listReferenceTrip){
            displayTrip(refTrip);
            displayWaypoints(refTrip);
        }
    }

    /**
     * Method that loads and display all the trips created by the current user
     */
    private void loadMyTrips(){
        for(Trip myTrip : listMyTrips){
            displayTrip(myTrip);
        }
    }

    /**
     * Method that display the passed trip in google maps
     * @param tripToDisplay
     */
    private void displayTrip(Trip tripToDisplay){
        List<Position> positionList = tripToDisplay.getListPositions();
        PolylineOptions pathTrip = new PolylineOptions();
        factory = new IconGenerator(getActivity());

        ListIterator<Position> iterator = positionList.listIterator();
        while(iterator.hasNext()){

            Position pos = null;
            String titleLabel = "";
            Bitmap icon = null;

            if(!iterator.hasPrevious()){
                if(tripToDisplay.isReference()){
                    titleLabel = "REFERENCE TRIP \n Departure " + tripToDisplay.getName();
                    factory.setColor(Color.YELLOW);
                } else{
                    factory.setColor(Color.parseColor(tripToDisplay.getColor()));
                    titleLabel = "Departure " + tripToDisplay.getName();
                }
                icon = factory.makeIcon(titleLabel);
                pos = iterator.next();
                mGoogleMap.addMarker(
                        new MarkerOptions()
                                .position(new LatLng(pos.getCoordX(), pos.getCoordY()))
                                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                );
            }else{
                pos = iterator.next();
            }

            if(!iterator.hasNext()){
                if(tripToDisplay.isReference()){
                    titleLabel = "REFERENCE TRIP \n Arrival " + tripToDisplay.getName();
                    factory.setColor(Color.YELLOW);
                } else{
                    factory.setColor(Color.parseColor(tripToDisplay.getColor()));
                    titleLabel = "Arrival " + tripToDisplay.getName();
                }
                factory.setColor(Color.parseColor(tripToDisplay.getColor()));
                icon = factory.makeIcon(titleLabel);
                mGoogleMap.addMarker(
                        new MarkerOptions()
                                .position(new LatLng(pos.getCoordX(), pos.getCoordY()))
                                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                );
            }
            pathTrip.add(new LatLng(pos.getCoordX(), pos.getCoordY()));
        }

        pathTrip.color(Color.parseColor(tripToDisplay.getColor()));

        mGoogleMap.addPolyline(pathTrip);
    }

    /**
     * Method that display the waypoints related to a specific displayed trip
     * @param tripToLoadWaypoints
     */
    private void displayWaypoints(Trip tripToLoadWaypoints){

        if(tripToLoadWaypoints.getListWaypoints() != null){
            ListIterator<Waypoint> listIterator = tripToLoadWaypoints.getListWaypoints().listIterator();
            while (listIterator.hasNext()){
                Waypoint waypoint = listIterator.next();
                BitmapDescriptor iconForMarker = null;
                switch (waypoint.getType()){
                    case DANGER:
                        iconForMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
                        break;
                    case INFO:
                        iconForMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                        break;
                    case WARNING:
                        iconForMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
                        break;
                }
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(waypoint.getPosition().getCoordX(), waypoint.getPosition().getCoordY()))
                        .title(waypoint.getLabel())
                        .icon(iconForMarker));
            }
        }


    }

}
