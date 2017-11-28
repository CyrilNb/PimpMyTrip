package fr.univtln.cniobechoudayer.pimpmytrip.Fragments;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
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
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import java.util.ArrayList;
import java.util.List;

import fr.univtln.cniobechoudayer.pimpmytrip.Entities.Position;
import fr.univtln.cniobechoudayer.pimpmytrip.Entities.TypeWaypoint;
import fr.univtln.cniobechoudayer.pimpmytrip.Entities.Waypoint;
import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.Utils.Utils;
import fr.univtln.cniobechoudayer.pimpmytrip.controllers.TripController;
import fr.univtln.cniobechoudayer.pimpmytrip.controllers.UserController;


public class CreationTripFragment extends Fragment implements View.OnClickListener {

    public static CreationTripFragment singleton;
    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private FloatingActionButton buttonRecordTrip;
    private boolean isUserRecording = false;
    private boolean isManagerDrawingPath = false;
    private ColorPicker mColorPicker;
    private EditText titleEditText;
    private Button colorButton;
    private Spinner choicesTypeWaypoint;
    private AlertDialog.Builder builder;
    private PolylineOptions pathTrip;
    private List<Position> listPositions;
    private List<Waypoint> listWaypoints;
    private String currentChosenColor;
    private FloatingActionMenu menuActionsFAB;
    private com.github.clans.fab.FloatingActionButton saveButtonFAB;
    private com.github.clans.fab.FloatingActionButton deleteButtonFAB;
    private TripController tripController;



    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    public CreationTripFragment() {
        // Required empty public constructor
    }

    //Managing the singleton
    public static CreationTripFragment getInstance() {

        if (singleton == null) {
            singleton = new CreationTripFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_creation_trip, container, false);

        tripController = TripController.getInstance();

        //Setting up positions array
        listPositions = new ArrayList<>();
        listWaypoints = new ArrayList<>();

        //Setting up the view
        menuActionsFAB = (FloatingActionMenu) rootView.findViewById(R.id.menuFAB);
        menuActionsFAB.setVisibility(View.GONE);

        saveButtonFAB = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.saveTripFAB);
        deleteButtonFAB = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.deleteTripFAB);
        saveButtonFAB.setOnClickListener(this);
        deleteButtonFAB.setOnClickListener(this);

        //Get floating action button
        buttonRecordTrip = (FloatingActionButton) rootView.findViewById(R.id.buttonRecordTrip);
        buttonRecordTrip.setOnClickListener(this);

        //Setting view for save alert dialog
        titleEditText = new EditText(getContext());
        colorButton = new Button(getContext());
        choicesTypeWaypoint = new Spinner(getContext());

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String> (getContext(), android.R.layout.simple_list_item_1 , getResources().getStringArray(R.array.spinnerChoicesMarker));
        choicesTypeWaypoint.setAdapter(spinnerArrayAdapter);

        //Get the mapView
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        mColorPicker = new ColorPicker(getActivity(), 127, 127, 127);
        mColorPicker.setCallback(new ColorPickerCallback() {
            @Override
            public void onColorChosen(@ColorInt int color) {

                Log.d("Pure Hex", Integer.toHexString(color));
                Log.d("#Hex no alpha", String.format("#%06X", (0xFFFFFF & color)));
                Log.d("#Hex with alpha", String.format("#%08X", (0xFFFFFFFF & color)));

                mColorPicker.dismiss();
                if(colorButton != null){
                    colorButton.setBackgroundColor(Color.parseColor(String.format("#%06X", (0xFFFFFF & color))));
                    currentChosenColor = String.format("#%06X", (0xFFFFFF & color));
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

                /**
                 * Loading and displaying the current location of user
                 */
                LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();

                Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
                if (location != null) {
                    mGoogleMap.setMyLocationEnabled(true);
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                            .zoom(13)                   // Sets the zoom
                            .build();                   // Creates a CameraPosition from the builder
                    mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }

                mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                    @Override
                    public void onMapClick(LatLng point) {
                        if(isUserRecording || pathTrip == null)
                            mGoogleMap.clear();
                        mGoogleMap.addMarker(new MarkerOptions().position(point));
                        if(!isUserRecording)
                            addLineBetweenMarkers(point);
                    }
                });

                mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng point) {
                        if(isUserRecording || isManagerDrawingPath)
                            displayDialogSaveMarker(point);
                    }
                });
            }
        });

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
                }else if(!isUserRecording){
                    //TODO start recording trip
                    isUserRecording = true;
                    buttonRecordTrip.setImageResource(R.drawable.ic_stop_white_48dp);
                }
                break;
            case R.id.saveTripFAB:
                displayAlertDialogSaveTrip();
                break;
            case R.id.deleteTripFAB:
                resetPath();
                break;
        }
    }

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
                        tripController.createTrip(true, listPositions, listWaypoints, currentChosenColor, titleEditText.getText().toString(), UserController.getConnectedUserId());
                        buttonRecordTrip.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                        isUserRecording = false;
                        Toast.makeText(getContext(), "Your trip has been saved successfully !", Toast.LENGTH_LONG).show();
                        resetPath();
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
     * Method to display an alert dialog to save a specific marker
     */
    private void displayDialogSaveMarker(final LatLng pointToSave){
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

        choicesTypeWaypoint = new Spinner(getContext());

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String> (getContext(), android.R.layout.simple_list_item_1 , getResources().getStringArray(R.array.spinnerChoicesMarker));
        choicesTypeWaypoint.setAdapter(spinnerArrayAdapter);

        titleEditText = new EditText(getContext());

        alertLayout.addView(choicesTypeWaypoint);
        alertLayout.addView(titleEditText);

        builder.setTitle("Save waypoint ?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        BitmapDescriptor iconForMarker;
                        TypeWaypoint typeWaypoint;
                        switch (choicesTypeWaypoint.getSelectedItem().toString()) {
                            case "Info":
                                iconForMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                                typeWaypoint = TypeWaypoint.INFO;
                                break;
                            case "Danger":
                                iconForMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
                                typeWaypoint = TypeWaypoint.DANGER;
                                break;
                            case "Warning":
                                iconForMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
                                typeWaypoint = TypeWaypoint.WARNING;
                                break;
                            default:
                                iconForMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
                                typeWaypoint = TypeWaypoint.INFO;
                                break;
                        }

                        Waypoint waypoint = new Waypoint(new Position(pointToSave.latitude, pointToSave.longitude), titleEditText.getText().toString(), typeWaypoint);
                        listWaypoints.add(waypoint);
                        mGoogleMap.addMarker(new MarkerOptions()
                                .position(pointToSave)
                                .title(waypoint.getLabel())
                                .icon(iconForMarker));

                        Log.d("listWaypoints size", String.valueOf(listWaypoints.size()));
                    }

                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_menu_save)
                .setView(alertLayout)
                .show();
    }

    private void displayAlertDialogManageTrip(){
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
     * Method that stores clicked position and draw line between marker
     * @param coords
     */
    private void addLineBetweenMarkers(LatLng coords){

        listPositions.add(new Position(coords.latitude, coords.longitude));
        if(listPositions.size() > 1){
            menuActionsFAB.setVisibility(View.VISIBLE);
            buttonRecordTrip.setVisibility(View.GONE);
        }

        if(pathTrip == null){
            pathTrip = new PolylineOptions().add(coords);
            isManagerDrawingPath = true;
            isUserRecording = false;
        }else{
            pathTrip.add(coords);
        }
        mGoogleMap.addPolyline(pathTrip);
    }

    /**
     * Method to reset the current draw path
     */
    private void resetPath(){
        mGoogleMap.clear();
        listPositions.clear();
        listWaypoints.clear();
        menuActionsFAB.setVisibility(View.GONE);
        buttonRecordTrip.setVisibility(View.VISIBLE);
        pathTrip = null;
        isManagerDrawingPath = false;
    }

}
