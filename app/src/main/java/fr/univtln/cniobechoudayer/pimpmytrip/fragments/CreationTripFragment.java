package fr.univtln.cniobechoudayer.pimpmytrip.fragments;


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
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
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

import com.github.clans.fab.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import fr.univtln.cniobechoudayer.pimpmytrip.entities.Position;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.Trip;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.TypeWaypoint;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.Waypoint;
import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.services.RecordUserLocationService;
import fr.univtln.cniobechoudayer.pimpmytrip.utils.Utils;
import fr.univtln.cniobechoudayer.pimpmytrip.controllers.StatisticsController;
import fr.univtln.cniobechoudayer.pimpmytrip.controllers.TripController;
import fr.univtln.cniobechoudayer.pimpmytrip.controllers.UserController;

/**
 * Fragment that allows a manager to create / view references trips
 * NB : SINGLETON PATTERN IMPLEMENTED
 */

public class CreationTripFragment extends Fragment implements View.OnClickListener {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private boolean isUserRecording = false;
    private boolean isManagerDrawingPath = false;
    private boolean isUserSaving = false;
    private boolean isUserWalkingForRecordingPath = true;

    public static CreationTripFragment sInstance;
    private GoogleMap mGoogleMap;
    private PolylineOptions mCurrentDrawingPathOptions;
    private Polyline mCurrentPath;
    private MapView mMapView;
    private FloatingActionButton mButtonRecordTrip;

    private List<Position> mListPositions, mListPositionsCurrentRecordedTrip;
    private List<Waypoint> mListWaypoints;
    private List<Trip> mListReferenceTrip;
    private List<LatLng> mListCoordsCurrentPath;

    private TripController mTripController;
    private StatisticsController mStatisticsController;
    private UserController mUserController;

    private FloatingActionMenu mMenuActionsFAB;
    private com.github.clans.fab.FloatingActionButton mSaveButtonFAB, mDeleteButtonFAB, mUndoButtonFAB;

    private ColorPicker mColorPicker;
    private EditText mTitleEditText;
    private Button mColorButton;
    private Spinner mChoicesTypeWaypoint;
    private AlertDialog.Builder mBuilder;
    private PolylineOptions mPathTrip;
    private IconGenerator mFactory;
    private String mCurrentChosenColor;

    private Handler mHandler;
    private Intent mIntentRecordUserLocationService;

    private final DatabaseReference fDbUsersConnected = (DatabaseReference) FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase").child("connectedUsers");
    private final DatabaseReference dDatabaseUsersConnectedReference = fDbUsersConnected.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    private final DatabaseReference fDbTrips = FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase").child("trips");
    private ValueEventListener mListenerConnectedUsers;


    /**
     * Constructor
     */
    public CreationTripFragment() {
        // Required empty public constructor
    }

    /**
     * Gets the unique instance of the singleton
     * Create it if not initialized yet
     *
     * @return
     */
    public static CreationTripFragment getInstance() {

        if (sInstance == null) {
            sInstance = new CreationTripFragment();
        }

        return sInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_creation_trip, container, false);

        mTripController = TripController.getInstance();
        mStatisticsController = StatisticsController.getInstance();
        mUserController = UserController.getInstance();

        //Setting lists to manage trips to display
        mListReferenceTrip = new ArrayList<>();
        mListCoordsCurrentPath = new ArrayList<>();

        //Setting up maps mFactory for labels
        mFactory = new IconGenerator(getActivity());

        //Setting up positions array
        mListPositions = new ArrayList<>();
        mListWaypoints = new ArrayList<>();

        //Setting up the view
        mMenuActionsFAB = (FloatingActionMenu) rootView.findViewById(R.id.menuFAB);
        mMenuActionsFAB.setVisibility(View.GONE);

        mSaveButtonFAB = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.saveTripFAB);
        mDeleteButtonFAB = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.deleteTripFAB);
        mUndoButtonFAB = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.undoTripFAB);
        mSaveButtonFAB.setOnClickListener(this);
        mDeleteButtonFAB.setOnClickListener(this);
        mUndoButtonFAB.setOnClickListener(this);

        //Get floating action button
        mButtonRecordTrip = (FloatingActionButton) rootView.findViewById(R.id.buttonRecordTrip);
        mButtonRecordTrip.setOnClickListener(this);

        //Setting view for save alert dialog
        mTitleEditText = new EditText(getContext());
        mColorButton = new Button(getContext());
        mChoicesTypeWaypoint = new Spinner(getContext());

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.spinnerChoicesMarker));
        mChoicesTypeWaypoint.setAdapter(spinnerArrayAdapter);

        //Get the mapView
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        //Default color
        mCurrentChosenColor = "#F2F2F2";

        /**
         * Callback for color picker
         * when creating a new trip
         */

        mColorPicker = new ColorPicker(getActivity(), 249, 191, 59);
        mColorPicker.setCallback(new ColorPickerCallback() {
            @Override
            public void onColorChosen(@ColorInt int color) {
                mColorPicker.dismiss();
                if (mColorButton != null) {
                    mColorButton.setBackgroundColor(Color.parseColor(String.format("#%06X", (0xFFFFFF & color))));
                    mCurrentChosenColor = String.format("#%06X", (0xFFFFFF & color));
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
         * Listening result of recording service
         */
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle reply = msg.getData();
                Log.d("mListPositions service", String.valueOf(reply.get("listPositionsTrip")));
                if (reply.get("listPositionsTrip") != null) {
                    mListPositions = reply.getParcelableArrayList("listPositionsTrip");
                    Log.d("listPos recorded size", String.valueOf(mListPositions.size()));
                    if (isUserSaving) {
                        Trip addedTrip = mTripController.insertTrip(true, mListPositions, mListWaypoints, mCurrentChosenColor, mTitleEditText.getText().toString(), computeTotalTripDistance(mListPositions), mUserController.getConnectedUserId());
                        isUserSaving = false;
                    }
                } else {
                    mListPositions = new ArrayList<>();
                    Log.d("mListPositions", "null");
                }
            }
        };

        /**
         * Setting up fragment title
         */
        Utils.setActionBarTitle((AppCompatActivity) getActivity(), getString(R.string.titleTripsAdd));

        /**
         * Setting up default chosen color
         */
        mCurrentChosenColor = "#F2F2F2";

        /**
         * Set up listener if order to get user location and display the trip
         */
        mListenerConnectedUsers = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Position newPosition = dataSnapshot.child("lastKnownLocation").getValue(Position.class);
                Log.d("pos retrieved", newPosition.toString());
                if(isUserRecording){
                    displayRecordingTrip(newPosition);
                    Log.d("new pos","added to path");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

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
                            .build();                   // Creates a CameraPosition from the mBuilder
                    mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }

                mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                    @Override
                    public void onMapClick(LatLng point) {
                        if (isUserRecording || mPathTrip == null)
                            mGoogleMap.clear();
                        mGoogleMap.addMarker(new MarkerOptions().position(point));
                        if (!isUserRecording)
                            addLineBetweenMarkers(point);
                    }
                });

                mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng point) {
                        if (isUserRecording || isManagerDrawingPath)
                            displayDialogSaveMarker(point);
                    }
                });
            }
        });

        return rootView;
    }

    /**
     * Setting up database listener on fragment start
     */
    @Override
    public void onStart() {
        super.onStart();
        fDbTrips.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot tripSnapshot : dataSnapshot.getChildren()) {
                    Trip currentTrip = tripSnapshot.getValue(Trip.class);
                    Log.d("New trip retrieved", String.valueOf(currentTrip.getName()));
                    if (currentTrip.isReference()) {
                        mListReferenceTrip.add(currentTrip);
                    }
                }
                Log.d("mListRefTrip size: ", String.valueOf(mListReferenceTrip.size()));

                MapFragment myFragment = (MapFragment) getActivity().getSupportFragmentManager().findFragmentByTag("MapFragment");
                if (myFragment != null && myFragment.isVisible()) {
                    loadReferenceTrip();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(mListenerConnectedUsers != null)
            dDatabaseUsersConnectedReference.addValueEventListener(mListenerConnectedUsers);
    }

    /**
     * Method that loads and display the current referenced trip
     */
    private void loadReferenceTrip() {
        for (Trip refTrip : mListReferenceTrip) {
            displayTrip(refTrip);
            displayWaypoints(refTrip);
        }
    }


    /**
     * Method that display the passed trip in google maps
     *
     * @param tripToDisplay the trip to display on the map
     */
    private void displayTrip(Trip tripToDisplay) {
        List<Position> positionList = tripToDisplay.getListPositions();
        PolylineOptions pathTrip = new PolylineOptions();
        mFactory = new IconGenerator(getActivity());

        ListIterator<Position> iterator = positionList.listIterator();
        while (iterator.hasNext()) {

            Position pos = null;

            if (!iterator.hasPrevious()) {
                mFactory.setColor(Color.parseColor(tripToDisplay.getColor()));
                Bitmap icon = null;
                icon = mFactory.makeIcon("Departure " + tripToDisplay.getName());
                pos = iterator.next();
                mGoogleMap.addMarker(
                        new MarkerOptions()
                                .position(new LatLng(pos.getCoordX(), pos.getCoordY()))
                                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                );
            } else {
                pos = iterator.next();
            }

            if (!iterator.hasNext()) {
                IconGenerator factory = new IconGenerator(getActivity());
                factory.setColor(Color.parseColor(tripToDisplay.getColor()));
                Bitmap icon = null;
                icon = factory.makeIcon("Arrival " + tripToDisplay.getName());
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
     *
     * @param tripToLoadWaypoints trip to load its waypoints
     */
    private void displayWaypoints(Trip tripToLoadWaypoints) {

        if (tripToLoadWaypoints.getListWaypoints() != null) {
            ListIterator<Waypoint> listIterator = tripToLoadWaypoints.getListWaypoints().listIterator();
            while (listIterator.hasNext()) {
                Waypoint waypoint = listIterator.next();
                BitmapDescriptor iconForMarker = null;
                switch (waypoint.getType()) {
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

    /**
     * Method that display the current recorded trip in live
     * @param pos
     */
    private void displayRecordingTrip(Position pos){
        Toast.makeText(getContext(), "mListPositionsCurrentRecordedTrip" + mListPositionsCurrentRecordedTrip.size(), Toast.LENGTH_SHORT).show();
        if(mCurrentDrawingPathOptions == null){
            mCurrentDrawingPathOptions = new PolylineOptions();
            mCurrentDrawingPathOptions.color(getActivity().getResources().getColor(R.color.colorPrimaryDark));
        }
        if(mListPositionsCurrentRecordedTrip.size() == 0){
            Toast.makeText(getContext(), "mCurrentDrawingPathOptions linked to map " + pos.toString(), Toast.LENGTH_SHORT).show();
            mCurrentPath = mGoogleMap.addPolyline(mCurrentDrawingPathOptions);
        }else{
            mCurrentPath.remove();
        }
        mListPositionsCurrentRecordedTrip.add(pos);
        mCurrentDrawingPathOptions.add(new LatLng(pos.getCoordX(), pos.getCoordY()));
        mCurrentPath = mGoogleMap.addPolyline(mCurrentDrawingPathOptions);
        Toast.makeText(getContext(), "mCurrentDrawingPathOptions" + mCurrentDrawingPathOptions.getPoints().size(), Toast.LENGTH_SHORT).show();

    }

    /**
     * Method to react once user gives an requested permission
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                break;
        }
    }

    ;

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
        switch (v.getId()) {
            case R.id.buttonRecordTrip:
                if (isUserRecording) {
                    displayAlertDialogSaveTrip();
                } else if (!isUserRecording) {
                    displayAlertDialogChoiceTransportationMode();
                    mButtonRecordTrip.setImageResource(R.drawable.ic_stop_white_48dp);
                }
                break;
            case R.id.saveTripFAB:
                displayAlertDialogSaveTrip();
                break;
            case R.id.deleteTripFAB:
                resetPath();
                break;
            case R.id.undoTripFAB:
                mListCoordsCurrentPath.remove(mListCoordsCurrentPath.size()-1);
                mCurrentPath.remove();
                for(LatLng pos : mListCoordsCurrentPath){
                    mPathTrip.add(pos);
                }
                mCurrentPath = mGoogleMap.addPolyline(mPathTrip);
                break;
        }
    }

    /**
     * Method that shows alert dialog to save trip
     */

    private void displayAlertDialogSaveTrip() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder = new AlertDialog.Builder(getContext(), R.style.CustomDialogTheme);
        } else {
            mBuilder = new AlertDialog.Builder(getContext());
        }
        LinearLayout alertLayout = new LinearLayout(getContext());
        alertLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(Utils.convertPixelsToDp(20, getContext()), Utils.convertPixelsToDp(40, getContext()), Utils.convertPixelsToDp(20, getContext()), Utils.convertPixelsToDp(40, getContext()));
        alertLayout.setLayoutParams(params);

        //Setting view for save alert dialog
        mTitleEditText = new EditText(getContext());
        mColorButton = new Button(getContext());

        mTitleEditText.setHint("Choose a title");
        mTitleEditText.setSingleLine(false);
        mTitleEditText.setMaxLines(2);
        mTitleEditText.setHorizontalScrollBarEnabled(false);
        mTitleEditText.setHintTextColor(Color.WHITE);
        mTitleEditText.setTextColor(Color.WHITE);

        mColorButton.setText("Choose a color");
        mColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mColorPicker.show();
            }
        });

        alertLayout.addView(mTitleEditText);
        alertLayout.addView(mColorButton);


        mBuilder.setTitle("Stop recording & save trip ?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (mListPositions != null && !isUserRecording) {
                            Trip addedTrip = mTripController.insertTrip(true, mListPositions, mListWaypoints, mCurrentChosenColor, mTitleEditText.getText().toString(), computeTotalTripDistance(mListPositions), mUserController.getConnectedUserId());
                            mStatisticsController.updateStats(addedTrip, isUserWalkingForRecordingPath);
                            mButtonRecordTrip.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                            resetPath();
                        } else {
                            isUserSaving = true;
                            Log.d("can't save cause", "listPos is null");
                        }
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
    private void displayDialogSaveMarker(final LatLng pointToSave) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder = new AlertDialog.Builder(getContext(), R.style.CustomDialogTheme);
        } else {
            mBuilder = new AlertDialog.Builder(getContext());
        }
        LinearLayout alertLayout = new LinearLayout(getContext());
        alertLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(Utils.convertPixelsToDp(20, getContext()), Utils.convertPixelsToDp(40, getContext()), Utils.convertPixelsToDp(20, getContext()), Utils.convertPixelsToDp(40, getContext()));
        alertLayout.setLayoutParams(params);

        mChoicesTypeWaypoint = new Spinner(getContext());

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.spinnerChoicesMarker));
        mChoicesTypeWaypoint.setAdapter(spinnerArrayAdapter);

        mTitleEditText = new EditText(getContext());

        alertLayout.addView(mChoicesTypeWaypoint);
        alertLayout.addView(mTitleEditText);

        mBuilder.setTitle("Save waypoint ?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        BitmapDescriptor iconForMarker;
                        TypeWaypoint typeWaypoint;
                        switch (mChoicesTypeWaypoint.getSelectedItem().toString()) {
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

                        Waypoint waypoint = new Waypoint(new Position(pointToSave.latitude, pointToSave.longitude), mTitleEditText.getText().toString(), typeWaypoint);
                        mListWaypoints.add(waypoint);
                        mGoogleMap.addMarker(new MarkerOptions()
                                .position(pointToSave)
                                .title(waypoint.getLabel())
                                .icon(iconForMarker));

                        Log.d("mListWaypoints size", String.valueOf(mListWaypoints.size()));
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

    /**
     * Method that stores clicked position and draw line between marker
     *
     * @param coords coordonates of the clicked position
     */
    private void addLineBetweenMarkers(LatLng coords) {
        mListCoordsCurrentPath.add(coords);
        mListPositions.add(new Position(coords.latitude, coords.longitude));
        if (mListPositions.size() > 1) {
            mMenuActionsFAB.setVisibility(View.VISIBLE);
            mButtonRecordTrip.setVisibility(View.GONE);
        }

        if (mPathTrip == null) {
            mPathTrip = new PolylineOptions().add(coords);
            isManagerDrawingPath = true;
            isUserRecording = false;
        } else {
            mPathTrip.add(coords);
        }
        mCurrentPath = mGoogleMap.addPolyline(mPathTrip);
    }

    /**
     * Method to reset the current draw path
     */
    private void resetPath() {
        mGoogleMap.clear();
        mListPositions.clear();
        mListWaypoints.clear();
        mListCoordsCurrentPath.clear();
        mMenuActionsFAB.setVisibility(View.GONE);
        mButtonRecordTrip.setVisibility(View.VISIBLE);
        mPathTrip = null;
        isManagerDrawingPath = false;
        if(mCurrentPath != null)
            mCurrentPath.remove();
    }

    /**
     * Method to compute the total distance of a trip
     *
     * @param listPositions
     * @return
     */
    private int computeTotalTripDistance(List<Position> listPositions) {

        Marker prevMarker = null;
        Marker currentMarker = null;
        double distance = 0;

        ListIterator<Position> listIterator = listPositions.listIterator();
        while (listIterator.hasNext()) {
            if (!listIterator.hasPrevious()) {
                Position pos = listIterator.next();
                prevMarker = mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(pos.getCoordX(), pos.getCoordY())).visible(false));
            } else {
                Position pos = listIterator.next();
                currentMarker = mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(pos.getCoordX(), pos.getCoordY())).visible(false));
                distance += SphericalUtil.computeDistanceBetween(prevMarker.getPosition(), currentMarker.getPosition());
                prevMarker = currentMarker;
            }

        }

        Log.d("distance computed : ", String.valueOf(distance));

        return (int) distance;

    }

    /**
     * Method that create and displays alert dialog to choose the transportation mode
     */
    private void displayAlertDialogChoiceTransportationMode() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder = new AlertDialog.Builder(getContext(), R.style.CustomDialogTheme);
        } else {
            mBuilder = new AlertDialog.Builder(getContext());
        }
        LinearLayout alertLayout = new LinearLayout(getContext());
        alertLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(Utils.convertPixelsToDp(20, getContext()), Utils.convertPixelsToDp(40, getContext()), Utils.convertPixelsToDp(20, getContext()), Utils.convertPixelsToDp(40, getContext()));
        alertLayout.setLayoutParams(params);

        final Spinner choiceTransportationMode = new Spinner(getContext());
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.spinnerChoicesTransportationMode));
        choiceTransportationMode.setAdapter(spinnerArrayAdapter);

        alertLayout.addView(choiceTransportationMode);

        mBuilder.setTitle("Choose your transporation mode")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mButtonRecordTrip.setImageResource(R.drawable.ic_stop_white_48dp);
                        isUserRecording = true;
                        if (choiceTransportationMode.getSelectedItemPosition() == 0) {
                            isUserWalkingForRecordingPath = true;
                        } else {
                            isUserWalkingForRecordingPath = false;
                        }
                        mIntentRecordUserLocationService = new Intent(getContext(), RecordUserLocationService.class);
                        mIntentRecordUserLocationService.putExtra("isUserWalking", isUserWalkingForRecordingPath);
                        mIntentRecordUserLocationService.putExtra("messenger", new Messenger(mHandler));
                        getActivity().startService(mIntentRecordUserLocationService);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        isUserRecording = false;
                        mButtonRecordTrip.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                    }
                })
                .setIcon(android.R.drawable.ic_menu_save)
                .setView(alertLayout)
                .show();

    }

}
