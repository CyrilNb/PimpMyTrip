package fr.univtln.cniobechoudayer.pimpmytrip.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.univtln.cniobechoudayer.pimpmytrip.controllers.UserController;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.Position;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.Statistics;
import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.controllers.StatisticsController;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.TypeWaypoint;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.User;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.Waypoint;
import fr.univtln.cniobechoudayer.pimpmytrip.utils.Utils;

/**
 * Fragment that manages user profile
 */

public class ProfileFragment extends Fragment implements AppBarLayout.OnOffsetChangedListener{

    private ImageView mProfileImage;
    private int mMaxScrollSize;
    private static final int PERCENTAGE_TO_ANIMATE_AVATAR = 20;
    private boolean mIsAvatarShown = true;
    private ListView listViewStats;
    private List<String> listStatsToDisplay = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private Statistics userStats;
    private AlertDialog.Builder builder;
    private EditText nameEditText;
    private UserController userController;
    private StatisticsController statsController;
    private TextView textViewPseudoUser;
    private TextView textViewEmailUser;
    public static final int GET_FROM_GALLERY = 3;
    private ValueEventListener listenerUser;
    private DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase").child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());


    private static ProfileFragment singleton;

    public ProfileFragment() {
        // Required empty public constructor
    }


    public static ProfileFragment getInstance() {
        if(singleton == null){
            singleton = new ProfileFragment();

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

        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        /**
         * Instanciating useful controllers
         */
        userController = UserController.getInstance();
        statsController = StatisticsController.getInstance();
        userStats = statsController.getUserStats();


        /**
         * Retrieving elements from view
         */
        textViewPseudoUser = (TextView) rootView.findViewById(R.id.textViewPseudoUser);
        textViewEmailUser = (TextView) rootView.findViewById(R.id.textViewEmailUser);
        AppBarLayout appbarLayout = (AppBarLayout) rootView.findViewById(R.id.materialup_appbar);
        mProfileImage = (ImageView) rootView.findViewById(R.id.materialup_profile_image);
        listViewStats = (ListView) rootView.findViewById(R.id.listViewStats);

        /**
         * Setting up listener to retrieve user from firebase db
         */
        listenerUser = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User connectedUser = dataSnapshot.getValue(User.class);
                if(connectedUser != null){
                    textViewPseudoUser.setText(connectedUser.getPseudo());
                    mProfileImage.setImageBitmap(connectedUser.getConvertedPhoto());
                    textViewEmailUser.setText(connectedUser.getEmail());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        if (listenerUser != null)
            dbUser.addValueEventListener(listenerUser);


        /**
         * Setting up listeners in order to allow user to modify his profile
         */
        textViewPseudoUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayAlertDialogUpdateName();
            }
        });

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.materialup_toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    //TODO BACK
                }
            });
        }

        appbarLayout.addOnOffsetChangedListener(this);
        mMaxScrollSize = appbarLayout.getTotalScrollRange();

        /**
         * Populating the view to display
         */
        /**
         * Displaying user statistics
         */
        if(userStats != null){

            listStatsToDisplay.add(getString(R.string.nbMyTripsTravelledLabel) + String.valueOf(userStats.getNbMyTripsTravelled()));
            listStatsToDisplay.add(getString(R.string.totalDistanceLabel) + String.valueOf(userStats.getTotalDistance()));
            listStatsToDisplay.add(getString(R.string.totalDistanceBySUVLAbel) + String.valueOf(userStats.getTotalDistanceBySUV()));
            listStatsToDisplay.add(getString(R.string.totalDistanceByWalkLabel) + String.valueOf(userStats.getTotalDistanceByWalk()));
            listStatsToDisplay.add(getString(R.string.nbTripsCreatedLabel) + String.valueOf(userStats.getNbTripsCreated()));
            listStatsToDisplay.add(getString(R.string.nbTripsSUVCreatedLabel) + String.valueOf(userStats.getNbTripsSUVCreated()));
            listStatsToDisplay.add(getString(R.string.nbTripsWalkingCreatedLabel) + String.valueOf(userStats.getNbTripsWalkingCreated()));
            listStatsToDisplay.add(getString(R.string.totalTimeTravelledLabel) + String.valueOf(userStats.getTotalTimeTravelled()));
            listStatsToDisplay.add(getString(R.string.totalTimeDroveLabel) + String.valueOf(userStats.getTotalTimeDrove()));
            listStatsToDisplay.add(getString(R.string.totalTimeWalkedLabel) + String.valueOf(userStats.getTotalTimeWalked()));

            adapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_list_item_1,
                    listStatsToDisplay);

            // Create an ArrayAdapter from List
            adapter = new ArrayAdapter<String>
                    (getContext(), android.R.layout.simple_list_item_1, listStatsToDisplay){
                @Override
                public View getView(int position, View convertView, ViewGroup parent){
                    // Get the Item from ListView
                    View view = super.getView(position, convertView, parent);

                    // Initialize a TextView for ListView each Item
                    TextView tv = (TextView) view.findViewById(android.R.id.text1);

                    // Set the text color of TextView (ListView Item)
                    tv.setTextColor(Color.WHITE);

                    // Generate ListView Item using TextView
                    return view;
                }
            };

            listViewStats.setAdapter(adapter);
            Log.d("userStats", "not null");
        }else{
            Log.d("userStats", "null");
        }

        /**
         * Displaying user information
         */
        textViewPseudoUser.setText(userController.getConnectedUser().getPseudo());
        textViewEmailUser.setText(userController.getConnectedUser().getEmail());
        mProfileImage.setImageBitmap(userController.getConnectedUser().getConvertedPhoto());

        /**
         * Setting up fragment title
         */
        Utils.setActionBarTitle((AppCompatActivity) getActivity(), getString(R.string.titleProfile));

        return rootView;
    }


    /**
     * Methods that is used to get the picture that the user uploaded
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                userController.updatePhotoUser(bitmap);
                mProfileImage.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Method to animate profile picture
     * @param appBarLayout
     * @param i
     */
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (mMaxScrollSize == 0)
            mMaxScrollSize = appBarLayout.getTotalScrollRange();

        int percentage = (Math.abs(i)) * 100 / mMaxScrollSize;

        if (percentage >= PERCENTAGE_TO_ANIMATE_AVATAR && mIsAvatarShown) {
            mIsAvatarShown = false;

            mProfileImage.animate()
                    .scaleY(0).scaleX(0)
                    .setDuration(200)
                    .start();
        }

        if (percentage <= PERCENTAGE_TO_ANIMATE_AVATAR && !mIsAvatarShown) {
            mIsAvatarShown = true;

            mProfileImage.animate()
                    .scaleY(1).scaleX(1)
                    .start();
        }
    }

    /**
     * Display alert dialog to update user pseudo
     */
    private void displayAlertDialogUpdateName() {
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

        nameEditText = new EditText(getContext());
        nameEditText.setText(userController.getConnectedUser().getPseudo());

        alertLayout.addView(nameEditText);

        builder.setTitle("Update your name")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        userController.updatePseudoUser(nameEditText.getText().toString());
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
     * Handling fragment life cycle to avoid crashes due to view updates
     */
    @Override
    public void onStop() {
        super.onStop();
        if (listenerUser != null)
            dbUser.removeEventListener(listenerUser);
    }



}
