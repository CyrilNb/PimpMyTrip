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
import android.support.design.widget.Snackbar;
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
import fr.univtln.cniobechoudayer.pimpmytrip.utils.Utils;
import fr.univtln.cniobechoudayer.pimpmytrip.controllers.TripController;
import fr.univtln.cniobechoudayer.pimpmytrip.controllers.UserController;

public class MapFragment extends Fragment implements View.OnClickListener, LocationListener {

    public static final int LOCATION_UPDATE_MIN_DISTANCE = 3; //meters
    public static final int LOCATION_UPDATE_MIN_TIME = 1000; //milliseconds
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    private Context mContext;

    private boolean isUserRecording = false;
    private boolean isUserWalkingForRecordingPath = true;
    private boolean isUserSaving = false;

    private List<Position> mListPositions;
    private List<Waypoint> mListWaypoints;
    private List<Trip> mListReferenceTrip, mListMyTrips, mListSwipedTrips;
    private List<User> connectedUserlist;
    private Map<String, Marker> mConnectedUsersMarkersHashMap;

    private LocationManager mLocationManager;
    private LocationManager locationManager;
    private LocationListener mLocationListener;

    public static MapFragment sInstance;
    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private FloatingActionButton mButtonRecordTrip;
    private ColorPicker mColorPicker;
    private EditText mTitleEditText;
    private Button mColorButton;
    private AlertDialog.Builder mBuilder;
    private Handler mHandler;
    private Intent mIntentRecordUserLocationService;
    private String CurrentChosenColor;
    private Spinner mChoicesTypeWaypoint;
    private UserController mUserController;





    private IconGenerator mFactory;

    private final TripController fTripController = TripController.getInstance();
    private final DatabaseReference fDbTrips = FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase").child("trips");
    private final DatabaseReference fDbMyTrips = (DatabaseReference) fDbTrips.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    private final DatabaseReference fDbUsersConnected = (DatabaseReference) FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase").child("connectedUsers");
    private final DatabaseReference fDbUsers = (DatabaseReference) FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase").child("users");
    private final Query fDbRefTrips = fDbTrips.orderByChild("reference").equalTo(true);
    private ValueEventListener mListenerDbTrips;
    private ValueEventListener mListenerDbMyTrips;
    private ValueEventListener mListenerDbUserPhoto;


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
        connectedUserlist = new ArrayList<>();
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

        mUserController = UserController.getsInstance();

        /**
         * Handler to get a message that returns position from
         * RecordUserLocationService
         */
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle reply = msg.getData();
                Log.d("mListPositions service", String.valueOf(reply.get("listPositionsTrip")));
                if (reply.get("listPositionsTrip") != null) {
                    //mListPositions.add((Position) reply.getParcelable("current_position"));
                    mListPositions = reply.getParcelableArrayList("listPositionsTrip");
                    Log.d("listPos recorded size", String.valueOf(mListPositions.size()));
                    if (isUserSaving) {
                        Trip addedTrip = fTripController.insertTrip(false, mListPositions, mListWaypoints, CurrentChosenColor, mTitleEditText.getText().toString(), computeTotalTripDistance(mListPositions), mUserController.getConnectedUserId());
                        isUserSaving = false;
                    }
                } else {
                    mListPositions = new ArrayList<>();
                    Log.d("mListPositions", "null");
                }
            }
        };

        CurrentChosenColor = "#F2F2F2";

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
                Log.d("#Hex no alpha", String.format("#%06X", (0xFFFFFF & color)));

                mColorPicker.dismiss();
                if (mColorButton != null) {
                    mColorButton.setBackgroundColor(Color.parseColor(String.format("#%06X", (0xFFFFFF & color))));
                    CurrentChosenColor = String.format("#%06X", (0xFFFFFF & color));
                }
            }
        });

        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Utils.setActionBarTitle((AppCompatActivity) getActivity(), getString(R.string.titleMap));

        //Initializing map
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bundle extras = getArguments();
        if (extras != null) {
            mListSwipedTrips = extras.getParcelableArrayList("mListSwipedTrips");
            if (mListSwipedTrips != null) {

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
                        displayUserOnMap();
                    }
                    //getCurrentLocation();
                    /*if (currentLocation != null) {
                        zoomInMap(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 10);
                    }*/
                }
            });
            mListenerDbTrips = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot tripSnapshot : dataSnapshot.getChildren()) {
                        Trip currentTrip = (Trip) tripSnapshot.getValue(Trip.class);
                        Log.d("New trip retrieved", String.valueOf(currentTrip.getName()));
                        if (currentTrip.isReference()) {
                            mListReferenceTrip.add(currentTrip);
                        }
                    }

                    Log.d("mListReferenceTrip size:", String.valueOf(mListReferenceTrip.size()));

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

            //TODO
            fDbRefTrips.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    for (DataSnapshot tripSnapshot : dataSnapshot.getChildren()) {
                        Trip currentTrip = (Trip) tripSnapshot.getValue(Trip.class);
                        Log.d("fDbRefTrips", "starting");
                        Log.d("New reftrip retrieved", String.valueOf(currentTrip.getName()));
                        Log.d("value reference trip ", String.valueOf(currentTrip.isReference()));
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
                    System.out.println("new connected User added: " + dataSnapshot.getKey());
                    String idUser = dataSnapshot.getKey();
                    //if (!idUser.equals(mUserController.getConnectedUserId())) {
                    Position position = dataSnapshot.child("lastKnownLocation").getValue(Position.class);
                    //LatLng latLng = getLastPositionFromDB(dataSnapshot, idUser);
                    LatLng latLng = new LatLng(position.getCoordX(), position.getCoordY());
                    MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(idUser).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    Marker marker = mGoogleMap.addMarker(markerOptions);
                    mConnectedUsersMarkersHashMap.put(idUser, marker);
                    //}
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    System.out.println("onchild changed of user: " + dataSnapshot.getKey());
                    String idUser = dataSnapshot.getKey();
                    //if (!idUser.equals(mUserController.getConnectedUserId())) {
                    Position position = dataSnapshot.child("lastKnownLocation").getValue(Position.class);
                    //LatLng latLng = getLastPositionFromDB(dataSnapshot, idUser);
                    LatLng latLng = new LatLng(position.getCoordX(), position.getCoordY());
                    Marker marker = mConnectedUsersMarkersHashMap.get(idUser);
                    marker.setPosition(latLng);
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    String idUser = dataSnapshot.getKey();
                    //mConnectedUsersMarkersHashMap.get(idUser).remove();
                    System.out.println(" connected User removed: " + idUser);

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

        //TODO REFACTOR IN a classe mere fragment
        if (mListenerDbMyTrips != null)
            fDbMyTrips.addValueEventListener(mListenerDbMyTrips);
        if (mListenerDbTrips != null)
            fDbTrips.addValueEventListener(mListenerDbTrips);
        //fDbTrips.addListenerForSingleValueEvent(mListenerDbTrips);

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
        //mLocationManager.removeUpdates(mLocationListener);
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
                    isUserRecording = true;
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
                        getActivity().stopService(mIntentRecordUserLocationService);
                        mButtonRecordTrip.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                        isUserRecording = false;
                        if (mListPositions != null) {
                            Log.d("add trip ?", "reached");
                            isUserSaving = true;
                        } else {
                            Log.d("can't save cause", "listPos is null");
                        }
                        Toast.makeText(getContext(), "Your trip has been saved successfully !", Toast.LENGTH_LONG).show();
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

        Log.d("distance computed : ", String.valueOf(distance));

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
                        titleLabel = "REFERENCE TRIP \n Departure " + tripToDisplay.getName();
                        mFactory.setColor(Color.YELLOW);
                    } else {
                        mFactory.setColor(Color.parseColor(tripToDisplay.getColor()));
                        titleLabel = "Departure " + tripToDisplay.getName();
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
                        titleLabel = "REFERENCE TRIP \n Arrival " + tripToDisplay.getName();
                        mFactory.setColor(Color.YELLOW);
                    } else {
                        mFactory.setColor(Color.parseColor(tripToDisplay.getColor()));
                        titleLabel = "Arrival " + tripToDisplay.getName();
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
    public void displaySwipedTrip(Trip tripToDisplay) {
        List<Position> positionList = tripToDisplay.getListPositions();

        if (positionList != null && !positionList.isEmpty()) {

            PolylineOptions pathTrip = new PolylineOptions();

            ListIterator<Position> iterator = positionList.listIterator();
            while (iterator.hasNext()) {
                Position pos = null;
                pos = iterator.next();
                pathTrip.add(new LatLng(pos.getCoordX(), pos.getCoordY()));
            }

            pathTrip.color(Color.parseColor(tripToDisplay.getColor()));
            mGoogleMap.addPolyline(pathTrip);

            double latitude = tripToDisplay.getListPositions().get(0).getCoordX();
            double longitude = tripToDisplay.getListPositions().get(0).getCoordY();
            zoomInMap(new LatLng(latitude, longitude), 7);
        }

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


    private void displayUserOnMap() {
        Bitmap icon = null;
        Criteria blankCriteria = new Criteria();
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(blankCriteria, false));

        mFactory = new IconGenerator(this.mContext);
        mFactory.setColor(getResources().getColor(R.color.colorPrimaryDark));
        /*if (mUserController.getmConnectedUser().getConvertedPhoto() != null)
            icon = Bitmap.createScaledBitmap(new CircleTransform().transform(mUserController.getmConnectedUser().getConvertedPhoto()), 200, 200, false);
        if (icon != null)

        if(mUserController.getmConnectedUser() != null){
            Bitmap icon = Bitmap.createScaledBitmap(new CircleTransform().transform(mUserController.getmConnectedUser().getConvertedPhoto()), 200, 200, false);
            mGoogleMap.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .snippet(mUserController.getmConnectedUser().getPseudo())
                            .icon(BitmapDescriptorFactory.fromBitmap(icon))
            );

        }*/


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
     * Get the lastknow location of the user
     *
     * @return
     */
    private void getCurrentLocation() {

        /*mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        // For showing a move to my location button
        // Checking if user's location is accessible
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Requesting location permission
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return null;
        }
        return mLocationManager.getLastKnownLocation(mLocationManager.getBestProvider(criteria, false));*/

        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Location location = null;
        if (!(isGPSEnabled || isNetworkEnabled))
            Snackbar.make(mMapView, "error location provider", Snackbar.LENGTH_INDEFINITE).show();
        else {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //TODO
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if (isNetworkEnabled) {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
                location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (isGPSEnabled) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
                location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
        if (location != null) {
            System.out.println(location);
            drawMarker(location, "current position");
        }
    }

    /**
     * Draw a marker on the google map
     *
     * @param location location where to draw
     */
    private void drawMarker(Location location, String title) {
        if (mGoogleMap != null) {
            mGoogleMap.clear();
            LatLng gps = new LatLng(location.getLatitude(), location.getLongitude());
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(gps)
                    .title(title));
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 12));
        }

    }

    /**
     * Get the profile photo of a user
     *
     * @param idUser id of the user
     * @return bitmap
     */
    private Bitmap getUserPhoto(String idUser) {
        fDbUsers.child(idUser);
        final User[] userRetrieved = {new User()};
        mListenerDbUserPhoto = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userRetrieved[0] = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        fDbUsers.addValueEventListener(mListenerDbUserPhoto);
        return userRetrieved[0].getConvertedPhoto();

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


    /**
     * * Get the last position of the user retrieve from the database
     *
     * @param dataSnapshot data from the firebase db
     * @param idUser       idUser related to the data changed
     */
    private LatLng getLastPositionFromDB(DataSnapshot dataSnapshot, String idUser) {
        if (!idUser.equals(mUserController.getConnectedUserId())) {
            Position position = dataSnapshot.child("lastKnownLocation").getValue(Position.class);
            if (position != null)
                return new LatLng(position.getCoordX(), position.getCoordY());
            else {
                return new LatLng(0.0, 0.0);
            }
        } else {
            return new LatLng(0.0, 0.0);
        }
    }
}


