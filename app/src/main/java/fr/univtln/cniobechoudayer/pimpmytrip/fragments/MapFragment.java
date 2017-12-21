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
import android.location.LocationListener;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import fr.univtln.cniobechoudayer.pimpmytrip.entities.Position;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.Trip;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.TypeWaypoint;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.User;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.Waypoint;
import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.services.ConnectedUserLocationService;
import fr.univtln.cniobechoudayer.pimpmytrip.services.RecordUserLocationService;
import fr.univtln.cniobechoudayer.pimpmytrip.utils.CircleTransform;
import fr.univtln.cniobechoudayer.pimpmytrip.utils.Utils;
import fr.univtln.cniobechoudayer.pimpmytrip.controllers.TripController;
import fr.univtln.cniobechoudayer.pimpmytrip.controllers.UserController;

public class MapFragment extends Fragment implements View.OnClickListener, LocationListener {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    private Context mContext;

    private boolean isUserRecording = false;
    private boolean isUserWalkingForRecordingPath = true;
    private boolean isUserSaving = false;

    private List<Position> mListPositions, mListPositionsCurrentRecordedTrip;
    private List<Waypoint> mListWaypoints;
    private List<Trip> mListReferenceTrip, mListMyTrips, mListSwipedTrips;
    private Map<String, Marker> mConnectedUsersMarkersHashMap;

    private LocationManager mLocationManager;
    private LocationManager locationManager;
    private LocationListener mLocationListener;

    public static MapFragment sInstance;
    private PolylineOptions mCurrentDrawingPathOptions;
    private Polyline mCurrentPath;
    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private FloatingActionButton mButtonRecordTrip;
    private ColorPicker mColorPicker;
    private EditText mTitleEditText;
    private Button mColorButton;
    private AlertDialog.Builder mBuilder;
    private Handler mHandler;
    private Intent mIntentRecordUserLocationService;
    private String mCurrentChosenColor;
    private Spinner mChoicesTypeWaypoint;
    private UserController mUserController;

    private IconGenerator mFactory;

    private final TripController fTripController = TripController.getInstance();
    private final DatabaseReference fDbTrips = FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase").child("trips");
    private final DatabaseReference fDbMyTrips = (DatabaseReference) fDbTrips.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    private final DatabaseReference fDbUsersConnected = (DatabaseReference) FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase").child("connectedUsers");
    private final DatabaseReference fDbUsers = (DatabaseReference) FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase").child("users");
    private final DatabaseReference dDatabaseUsersConnectedReference = fDbUsersConnected.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    private final Query fDbRefTrips = fDbTrips.orderByChild("reference").equalTo(true);
    private ValueEventListener mListenerDbTrips, mListenerConnectedUsers, mListenerDbMyTrips, mListenerDbUserPhoto;


    public MapFragment() {
        // Required empty public constructor
    }

    //Managing the sInstance
    public static MapFragment getInstance() {

        if (sInstance == null) {
            sInstance = new MapFragment();
        }

        return sInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getContext();
        mFactory = new IconGenerator(getActivity());
        mConnectedUsersMarkersHashMap = new HashMap<>();
        getActivity().startService(new Intent(getActivity(), ConnectedUserLocationService.class));
        requestLocationPermissions();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        /**
         * Initializing location manager
         */
        initializeLocationManager();
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

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
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListener);

        //Setting lists to manage trips to display
        mListReferenceTrip = new ArrayList<>();
        mListMyTrips = new ArrayList<>();
        mListPositions = new ArrayList<>();
        mListWaypoints = new ArrayList<>();
        mListPositionsCurrentRecordedTrip = new ArrayList<>();

        mUserController = UserController.getInstance();

        /**
         * Handler to get a message that returns position from
         * RecordUserLocationService
         */
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle reply = msg.getData();
                if (reply.get("listPositionsTrip") != null) {
                    mListPositions = reply.getParcelableArrayList("listPositionsTrip");
                    if (isUserSaving) {
                        Toast.makeText(getContext(), getString(R.string.successMessageCreationTrip) + mListPositions.size() + " positions !", Toast.LENGTH_LONG).show();
                        fTripController.insertTrip(false, mListPositions, mListWaypoints, mCurrentChosenColor, mTitleEditText.getText().toString(), computeTotalTripDistance(mListPositions), mUserController.getConnectedUserId());
                        isUserSaving = false;
                    }
                } else {
                    mListPositions = new ArrayList<>();
                }
            }
        };

        mCurrentChosenColor = "#F2F2F2";

        //Get floating action button
        mButtonRecordTrip = (FloatingActionButton) rootView.findViewById(R.id.buttonRecordTrip);
        mButtonRecordTrip.setOnClickListener(this);

        //Setting view for save alert dialog
        mTitleEditText = new EditText(getContext());
        mColorButton = new Button(getContext());

        //Get the mapView
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        mChoicesTypeWaypoint = new Spinner(getContext());

        mColorPicker = new ColorPicker(getActivity(), 127, 127, 127);
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

        //mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Utils.setActionBarTitle((AppCompatActivity) getActivity(), getString(R.string.titleMap));

        //Initializing map
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bundle extras = getArguments();
        if (extras != null) {
            mListSwipedTrips = extras.getParcelableArrayList("listSwipedTrips");
            if (mListSwipedTrips != null) {
                System.out.println("name from bundle: " + mListSwipedTrips.get(0).getName());
                /**
                 * Loading the map asynchronously and display the trip swiped
                 */
                mMapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap mMap) {
                        mGoogleMap = mMap;
                        displaySwipedTrip(mListSwipedTrips.get(0));
                    }
                });

            }

        } else {
            /**
             * Loading the map asynchronously and adding a OnMapReadyCallback for displaying locations
             */
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap mMap) {
                    mGoogleMap = mMap;

                    // For showing a move to my location button
                    // Checking if user's location is accessible
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
                    if (location != null) {
                        zoomInMap(new LatLng(location.getLatitude(), location.getLongitude()), 10);
                    }

                    mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                        @Override
                        public void onMapLongClick(LatLng point) {
                            if (isUserRecording)
                                displayDialogSaveMarker(point);
                        }
                    });
                }
            });
            mListenerDbTrips = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot tripSnapshot : dataSnapshot.getChildren()) {
                        Trip currentTrip = (Trip) tripSnapshot.getValue(Trip.class);
                        if (currentTrip.isReference()) {
                            mListReferenceTrip.add(currentTrip);
                        }
                    }

                    MapFragment myFragment = (MapFragment) getActivity().getSupportFragmentManager().findFragmentByTag("MapFragment");
                    if (myFragment != null && myFragment.isVisible()) {
                        loadReferenceTrip();
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            mListenerDbMyTrips = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot tripSnapshot : dataSnapshot.getChildren()) {
                        Trip currentTrip = tripSnapshot.getValue(Trip.class);
                        mListMyTrips.add(currentTrip);
                    }
                    Log.d("mListMyTrips", "updated");
                    loadMyTrips();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            mListenerConnectedUsers = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Position newPosition = dataSnapshot.child("lastKnownLocation").getValue(Position.class);
                    if (newPosition != null) {
                        if (isUserRecording) {
                            displayRecordingTrip(newPosition);
                        }
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            fDbRefTrips.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    for (DataSnapshot tripSnapshot : dataSnapshot.getChildren()) {
                        Trip currentTrip = tripSnapshot.getValue(Trip.class);
                        if (currentTrip.isReference()) {
                            mListReferenceTrip.add(currentTrip);
                        }
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            fDbUsersConnected.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    String idUser = dataSnapshot.getKey();
                    if (!idUser.equals(mUserController.getConnectedUserId())) {
                        Position position = dataSnapshot.child("lastKnownLocation").getValue(Position.class);
                        if(position !=null){
                            LatLng latLng = new LatLng(position.getCoordX(), position.getCoordY());
                            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(idUser).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).visible(true);
                            Marker marker = mGoogleMap.addMarker(markerOptions);
                            //displayUserOnMap(latLng, idUser);
                            mConnectedUsersMarkersHashMap.put(idUser, marker);
                        }
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    String idUser = dataSnapshot.getKey();
                    if (!idUser.equals(mUserController.getConnectedUserId())) {
                        Position position = dataSnapshot.child("lastKnownLocation").getValue(Position.class);
                        if (position != null) {
                            LatLng latLng = new LatLng(position.getCoordX(), position.getCoordY());
                            Marker marker = mConnectedUsersMarkersHashMap.get(idUser);
                            marker.setPosition(latLng);
                        }
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    String idUser = dataSnapshot.getKey();
                    //mConnectedUsersMarkersHashMap.get(idUser).remove();
                    if(mConnectedUsersMarkersHashMap.get(idUser) != null){
                        mConnectedUsersMarkersHashMap.get(idUser).remove();
                    }

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        return rootView;
    }

    /**
     * Handle different fragment states and adapt the map
     */

    @Override
    public void onStart() {
        super.onStart();

        if (mListenerDbMyTrips != null)
            fDbMyTrips.addValueEventListener(mListenerDbMyTrips);
        if (mListenerDbTrips != null)
            fDbTrips.addValueEventListener(mListenerDbTrips);
        if (mListenerConnectedUsers != null)
            dDatabaseUsersConnectedReference.addValueEventListener(mListenerConnectedUsers);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mListenerDbMyTrips != null)
            fDbMyTrips.removeEventListener(mListenerDbMyTrips);
        if (mListenerDbTrips != null)
            fDbTrips.removeEventListener(mListenerDbTrips);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(mLocationListener);
        }
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
                } else {
                    displayAlertDialogChoiceTransportationMode();
                    mButtonRecordTrip.setImageResource(R.drawable.ic_stop_white_48dp);
                }
                break;
        }
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

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.spinnerChoicesMarker));
        mChoicesTypeWaypoint.setAdapter(spinnerArrayAdapter);

        mTitleEditText = new EditText(getContext());

        alertLayout.addView(mChoicesTypeWaypoint);
        alertLayout.addView(mTitleEditText);

        mBuilder.setTitle(getString(R.string.titleDialogWaypointTrip))
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
     * Method that create and display the alert dialog to save a trip to database
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

        /**
         * Setting view for save alert dialog
         */
        mTitleEditText = new EditText(getContext());
        mColorButton = new Button(getContext());

        mTitleEditText.setHint(getString(R.string.hintChooseTitleTrip));
        mTitleEditText.setSingleLine(false);
        mTitleEditText.setMaxLines(2);
        mTitleEditText.setHorizontalScrollBarEnabled(false);
        mTitleEditText.setHintTextColor(Color.WHITE);
        mTitleEditText.setTextColor(Color.WHITE);

        mColorButton.setText(getString(R.string.hintChooseColorTrip));
        mColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mColorPicker.show();
            }
        });

        alertLayout.addView(mTitleEditText);
        alertLayout.addView(mColorButton);


        mBuilder.setTitle(getString(R.string.titleDialogSaveTrip))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().stopService(mIntentRecordUserLocationService);
                        mButtonRecordTrip.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                        isUserRecording = false;
                        if (mListPositions != null) {
                            isUserSaving = true;
                        }
                        fTripController.insertTrip(false, mListPositionsCurrentRecordedTrip, mListWaypoints, mCurrentChosenColor, mTitleEditText.getText().toString(), computeTotalTripDistance(mListPositions), mUserController.getConnectedUserId());
                        mListPositionsCurrentRecordedTrip.clear();
                        mCurrentDrawingPathOptions = null;
                        mCurrentPath.remove();
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
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.spinnerChoicesTransportationMode));
        choiceTransportationMode.setAdapter(spinnerArrayAdapter);

        alertLayout.addView(choiceTransportationMode);

        mBuilder.setTitle(getString(R.string.titleDialogTransportationMode))
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

    /**
     * Method to compute the total distance of a trip
     *
     * @param listPositions list of positions of the trip
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
        return (int) distance;

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
     * Method that loads and display all the trips created by the current user
     */
    private void loadMyTrips() {
        for (Trip myTrip : mListMyTrips) {
            displayTrip(myTrip);
        }
    }

    /**
     * Method that display the current recorded trip in live
     *
     * @param pos
     */
    private void displayRecordingTrip(Position pos) {
        if (mCurrentDrawingPathOptions == null) {
            mCurrentDrawingPathOptions = new PolylineOptions();
            mCurrentDrawingPathOptions.color(getActivity().getResources().getColor(R.color.colorPrimaryDark));
        }
        if (mListPositionsCurrentRecordedTrip.size() == 0) {
            mCurrentPath = mGoogleMap.addPolyline(mCurrentDrawingPathOptions);
        } else {
            mCurrentPath.remove();
        }
        mListPositionsCurrentRecordedTrip.add(pos);
        mCurrentDrawingPathOptions.add(new LatLng(pos.getCoordX(), pos.getCoordY()));
        mCurrentPath = mGoogleMap.addPolyline(mCurrentDrawingPathOptions);
    }

    /**
     * Method that display the passed trip in google maps
     *
     * @param tripToDisplay
     */
    private void displayTrip(Trip tripToDisplay) {
        List<Position> positionList = tripToDisplay.getListPositions();
        PolylineOptions pathTrip = new PolylineOptions();
        mFactory = new IconGenerator(this.mContext);
        if (positionList != null) {
            ListIterator<Position> iterator = positionList.listIterator();
            while (iterator.hasNext()) {

                Position pos = null;
                String titleLabel = "";
                Bitmap icon = null;

                if (!iterator.hasPrevious()) {
                    if (tripToDisplay.isReference()) {
                        titleLabel = getString(R.string.titleReferenceTripDeparture) + " " + tripToDisplay.getName();
                        mFactory.setColor(Color.YELLOW);
                    } else {
                        mFactory.setColor(Color.parseColor(tripToDisplay.getColor()));
                        titleLabel = getString(R.string.titleTripDeparture) + " " + tripToDisplay.getName();
                    }
                    icon = mFactory.makeIcon(titleLabel);
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
                    if (tripToDisplay.isReference()) {
                        titleLabel = getString(R.string.titleReferenceTripArrival) + " " + tripToDisplay.getName();
                        mFactory.setColor(Color.YELLOW);
                    } else {
                        mFactory.setColor(Color.parseColor(tripToDisplay.getColor()));
                        titleLabel = getString(R.string.titleTripArrival) + " " + tripToDisplay.getName();
                    }
                    mFactory.setColor(Color.parseColor(tripToDisplay.getColor()));
                    icon = mFactory.makeIcon(titleLabel);
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

    }

    /**
     * Method that display the passed trip in google maps
     *
     * @param tripToDisplay
     */
    private void displaySwipedTrip(Trip tripToDisplay) {
        this.displayTrip(tripToDisplay);
        double latitude = tripToDisplay.getListPositions().get(0).getCoordX();
        double longitude = tripToDisplay.getListPositions().get(0).getCoordY();
        zoomInMap(new LatLng(latitude, longitude), 8);
    }


    /**
     * Method that displays the waypoints related to a specific displayed trip
     *
     * @param tripToLoadWaypoints
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
     * Method that zooms in the Google map on a specific zone
     *
     * @param latLng     latlng to zoom in
     * @param zoomDegree intensity of the zoom
     */
    private void zoomInMap(LatLng latLng, float zoomDegree) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to location user
                .zoom(zoomDegree)            // Sets the zoom
                .build();                   // Creates a CameraPosition from the mBuilder
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /**
     * Method that initializes location manager
     */
    private void initializeLocationManager() {

        if (locationManager == null) {
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        }

    }


    /**
     * Method to display user on map
     *
     * @param location
     * @param idUser
     */
    private void displayUserOnMap(LatLng location, String idUser) {
        Bitmap icon = null;
        User userRetrieved = findUserInList(idUser);
        //Toast.makeText(getContext(), userRetrieved.toString(), Toast.LENGTH_SHORT).show();
        mFactory = new IconGenerator(this.mContext);
        mFactory.setColor(getResources().getColor(R.color.colorPrimaryDark));
        icon = Bitmap.createScaledBitmap(new CircleTransform().transform(userRetrieved.convertedPhoto()), 100, 100, false);
        //icon = Bitmap.createScaledBitmap(new CircleTransform().transform(Utils.convertPicture(userRetrieved.getPhoto())), 200, 200, false);

        mGoogleMap.addMarker(
                new MarkerOptions()
                        .position(location)
                        .title(userRetrieved.getPseudo())
                        .icon(BitmapDescriptorFactory.fromBitmap(icon))
        );
    }

    private User findUserInList(String userID) {
        User user = new User();
        Toast.makeText(getContext(), String.valueOf(mUserController.getmMapUsers().size()), Toast.LENGTH_SHORT).show();
        if (mUserController.getmMapUsers().size() > 0) {
            for (Map.Entry<String, User> userFound : mUserController.getmMapUsers().entrySet()) {
                String key = userFound.getKey();
                if (userID.equals(key)) {
                    user = userFound.getValue();
                    Toast.makeText(getContext(), String.valueOf(user.getPseudo()), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            user = new User("DEFAULT", "default@gmail.com");
        }

        return user;
    }

    @Override
    public void onLocationChanged(Location location) {

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


    /**
     * Method to ask user to give the app rights to access the location
     */
    private void requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Requesting location permission
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        } else {
            launchServiceUserLocation();
        }
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
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    mButtonRecordTrip.setVisibility(View.GONE);
                } else {
                    mButtonRecordTrip.setVisibility(View.VISIBLE);
                    launchServiceUserLocation();
                }
                break;
        }
    }

    ;

    private void launchServiceUserLocation() {
        getActivity().startService(new Intent(getActivity(), ConnectedUserLocationService.class));
    }


}


