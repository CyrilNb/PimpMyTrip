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
import fr.univtln.cniobechoudayer.pimpmytrip.utils.CircleTransform;
import fr.univtln.cniobechoudayer.pimpmytrip.utils.Utils;
import fr.univtln.cniobechoudayer.pimpmytrip.controllers.TripController;
import fr.univtln.cniobechoudayer.pimpmytrip.controllers.UserController;

public class MapFragment extends Fragment implements View.OnClickListener, LocationListener {

    public static MapFragment mSingleton;
    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private FloatingActionButton buttonRecordTrip;
    private boolean isUserRecording = false;
    private ColorPicker mColorPicker;
    private EditText titleEditText;
    private Button colorButton;
    private AlertDialog.Builder builder;
    private boolean isUserWalkingForRecordingPath = true;
    private Handler handler;
    private Intent intentRecordUserLocationService;
    private List<Position> listPositions;
    private List<Waypoint> listWaypoints;
    private String currentChosenColor;
    private Spinner choicesTypeWaypoint;
    private boolean isUserSaving = false;
    private UserController userController;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private HashMap<String, Marker> mConnectedUsersMarkersHashMap;
    private List<User> connectedUserlist;

    public static final int LOCATION_UPDATE_MIN_DISTANCE = 3; //meters
    public static final int LOCATION_UPDATE_MIN_TIME = 1000; //milliseconds
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;

    private Context context;

    private List<Trip> listReferenceTrip;
    private List<Trip> listMyTrips;
    private ArrayList<Trip> listSwipedTrips;
    private IconGenerator factory;
    private ValueEventListener listenerDbTrips;
    private ValueEventListener listenerDbMyTrips;
    private ValueEventListener listenerDbUserPhoto;
    private TripController tripController = TripController.getInstance();

    private DatabaseReference dbTrips = FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase").child("trips");
    private Query dbRefTrips = dbTrips.orderByChild("reference").equalTo(true);
    private DatabaseReference dbMyTrips = (DatabaseReference) dbTrips.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    private DatabaseReference dbUsersConnected = (DatabaseReference) FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase").child("connectedUsers");
    private DatabaseReference dbUsers = (DatabaseReference) FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase").child("users");

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    public MapFragment() {
        // Required empty public constructor
    }

    //Managing the mSingleton
    public static MapFragment getInstance() {

        if (mSingleton == null) {
            mSingleton = new MapFragment();
        }

        return mSingleton;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getContext();
        factory = new IconGenerator(getActivity());

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
        locationListener = new LocationListener() {
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

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, locationListener);

        //Setting lists to manage trips to display
        listReferenceTrip = new ArrayList<>();
        listMyTrips = new ArrayList<>();
        listPositions = new ArrayList<>();
        listWaypoints = new ArrayList<>();

        userController = UserController.getInstance();

        /**
         * Handler to get a message that returns position from
         * RecordUserLocationService
         */
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle reply = msg.getData();
                Log.d("listPositions service", String.valueOf(reply.get("listPositionsTrip")));
                if (reply.get("listPositionsTrip") != null) {
                    listPositions.add((Position) reply.getParcelable("current_position"));
                    Log.d("listPos recorded size", String.valueOf(listPositions.size()));
                    Boolean isServiceEnded = reply.getBoolean("recording_finished");
                    if (isUserSaving && isServiceEnded) {
                        Trip addedTrip = tripController.insertTrip(false, listPositions, listWaypoints, currentChosenColor, titleEditText.getText().toString(), computeTotalTripDistance(listPositions), userController.getConnectedUserId());
                        isUserSaving = false;
                    }
                } else {
                    listPositions = new ArrayList<>();
                    Log.d("listPositions", "null");
                }
            }
        };

        currentChosenColor = "#F2F2F2";

        //Get floating action button
        buttonRecordTrip = (FloatingActionButton) rootView.findViewById(R.id.buttonRecordTrip);
        buttonRecordTrip.setOnClickListener(this);

        //Setting view for save alert dialog
        titleEditText = new EditText(getContext());
        colorButton = new Button(getContext());

        //Get the mapView
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        choicesTypeWaypoint = new Spinner(getContext());

        mColorPicker = new ColorPicker(getActivity(), 127, 127, 127);
        mColorPicker.setCallback(new ColorPickerCallback() {
            @Override
            public void onColorChosen(@ColorInt int color) {
                Log.d("#Hex no alpha", String.format("#%06X", (0xFFFFFF & color)));

                mColorPicker.dismiss();
                if (colorButton != null) {
                    colorButton.setBackgroundColor(Color.parseColor(String.format("#%06X", (0xFFFFFF & color))));
                    currentChosenColor = String.format("#%06X", (0xFFFFFF & color));
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
            listSwipedTrips = extras.getParcelableArrayList("listSwipedTrips");
            if (listSwipedTrips != null) {

                /**
                 * Loading the map asynchronously and display the trip swiped
                 */
                mMapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap mMap) {
                        mGoogleMap = mMap;
                        displaySwipedTrip(listSwipedTrips.get(0));
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
                        Trip currentTrip = tripSnapshot.getValue(Trip.class);
                        listMyTrips.add(currentTrip);
                    }
                    Log.d("listMyTrips", "updated");
                    loadMyTrips();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            //TODO
            dbRefTrips.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    for (DataSnapshot tripSnapshot : dataSnapshot.getChildren()) {
                        Trip currentTrip = (Trip) tripSnapshot.getValue(Trip.class);
                        Log.d("dbRefTrips", "starting");
                        Log.d("New reftrip retrieved", String.valueOf(currentTrip.getName()));
                        Log.d("value reference trip ", String.valueOf(currentTrip.isReference()));
                        if (currentTrip.isReference()) {
                            listReferenceTrip.add(currentTrip);
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


            dbUsersConnected.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    System.out.println("new connected User added: " + dataSnapshot.getKey());
                    String idUser = dataSnapshot.getKey();
                    //if (!idUser.equals(userController.getConnectedUserId())) {
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
                    //if (!idUser.equals(userController.getConnectedUserId())) {
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
        if (listenerDbMyTrips != null)
            dbMyTrips.addValueEventListener(listenerDbMyTrips);
        if (listenerDbTrips != null)
            dbTrips.addValueEventListener(listenerDbTrips);
        //dbTrips.addListenerForSingleValueEvent(listenerDbTrips);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (listenerDbMyTrips != null)
            dbMyTrips.removeEventListener(listenerDbMyTrips);
        if (listenerDbTrips != null)
            dbTrips.removeEventListener(listenerDbTrips);
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
            locationManager.removeUpdates(locationListener);
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
                    buttonRecordTrip.setImageResource(R.drawable.ic_stop_white_48dp);
                }
                break;
        }
    }

    /**
     * Method to display an alert dialog to save a specific marker
     */
    private void displayDialogSaveMarker(final LatLng pointToSave) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getContext(), R.style.CustomDialogTheme);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }
        LinearLayout alertLayout = new LinearLayout(getContext());
        alertLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(Utils.convertPixelsToDp(20, getContext()), Utils.convertPixelsToDp(40, getContext()), Utils.convertPixelsToDp(20, getContext()), Utils.convertPixelsToDp(40, getContext()));
        alertLayout.setLayoutParams(params);

        choicesTypeWaypoint = new Spinner(getContext());

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.spinnerChoicesMarker));
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

    /**
     * Method that create and display the alert dialog to save a trip to database
     */
    private void displayAlertDialogSaveTrip() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getContext(), R.style.CustomDialogTheme);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }
        LinearLayout alertLayout = new LinearLayout(getContext());
        alertLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(Utils.convertPixelsToDp(20, getContext()), Utils.convertPixelsToDp(40, getContext()), Utils.convertPixelsToDp(20, getContext()), Utils.convertPixelsToDp(40, getContext()));
        alertLayout.setLayoutParams(params);

        /**
         * Setting view for save alert dialog
         */
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
                        getActivity().stopService(intentRecordUserLocationService);
                        buttonRecordTrip.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                        isUserRecording = false;
                        if (listPositions != null) {
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
            builder = new AlertDialog.Builder(getContext(), R.style.CustomDialogTheme);
        } else {
            builder = new AlertDialog.Builder(getContext());
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

        builder.setTitle("Choose your transporation mode")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        buttonRecordTrip.setImageResource(R.drawable.ic_stop_white_48dp);
                        isUserRecording = true;
                        if (choiceTransportationMode.getSelectedItemPosition() == 0) {
                            isUserWalkingForRecordingPath = true;
                        } else {
                            isUserWalkingForRecordingPath = false;
                        }
                        Log.d("transportation mode", "selected");
                        intentRecordUserLocationService = new Intent(getContext(), RecordUserLocationService.class);
                        intentRecordUserLocationService.putExtra("isUserWalking", isUserWalkingForRecordingPath);
                        intentRecordUserLocationService.putExtra("messenger", new Messenger(handler));
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
        for (Trip refTrip : listReferenceTrip) {
            displayTrip(refTrip);
            displayWaypoints(refTrip);
        }
    }

    /**
     * Method that loads and display all the trips created by the current user
     */
    private void loadMyTrips() {
        for (Trip myTrip : listMyTrips) {
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
        factory = new IconGenerator(this.context);
        if (positionList != null) {
            ListIterator<Position> iterator = positionList.listIterator();
            while (iterator.hasNext()) {

                Position pos = null;
                String titleLabel = "";
                Bitmap icon = null;

                if (!iterator.hasPrevious()) {
                    if (tripToDisplay.isReference()) {
                        titleLabel = "REFERENCE TRIP \n Departure " + tripToDisplay.getName();
                        factory.setColor(Color.YELLOW);
                    } else {
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
                } else {
                    pos = iterator.next();
                }

                if (!iterator.hasNext()) {
                    if (tripToDisplay.isReference()) {
                        titleLabel = "REFERENCE TRIP \n Arrival " + tripToDisplay.getName();
                        factory.setColor(Color.YELLOW);
                    } else {
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
                .build();                   // Creates a CameraPosition from the builder
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

        factory = new IconGenerator(this.context);
        factory.setColor(getResources().getColor(R.color.colorPrimaryDark));
        /*if (userController.getConnectedUser().getConvertedPhoto() != null)
            icon = Bitmap.createScaledBitmap(new CircleTransform().transform(userController.getConnectedUser().getConvertedPhoto()), 200, 200, false);
        if (icon != null)

        if(userController.getConnectedUser() != null){
            Bitmap icon = Bitmap.createScaledBitmap(new CircleTransform().transform(userController.getConnectedUser().getConvertedPhoto()), 200, 200, false);
            mGoogleMap.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .snippet(userController.getConnectedUser().getPseudo())
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
        dbUsers.child(idUser);
        final User[] userRetrieved = {new User()};
        listenerDbUserPhoto = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userRetrieved[0] = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        dbUsers.addValueEventListener(listenerDbUserPhoto);
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
                    buttonRecordTrip.setVisibility(View.GONE);
                } else {
                    buttonRecordTrip.setVisibility(View.VISIBLE);
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
        if (!idUser.equals(userController.getConnectedUserId())) {
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


