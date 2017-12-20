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
import android.widget.TextView;

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
import fr.univtln.cniobechoudayer.pimpmytrip.entities.Statistics;
import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.controllers.StatisticsController;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.User;
import fr.univtln.cniobechoudayer.pimpmytrip.utils.Utils;

/**
 * Fragment that manages user profile
 */

public class ProfileFragment extends Fragment implements AppBarLayout.OnOffsetChangedListener{

    private static final int PERCENTAGE_TO_ANIMATE_AVATAR = 20;
    public static final int GET_FROM_GALLERY = 3;

    private UserController mUserController;
    private StatisticsController mStatsController;
    private static ProfileFragment sInstance;
    private Statistics userStats;

    private boolean mIsAvatarShown = true;

    private int mMaxScrollSize;

    private List<String> mListStatsToDisplay = new ArrayList<>();

    private ImageView mProfileImage;
    private ListView mListViewStats;
    private ArrayAdapter<String> mAdapter;
    private AlertDialog.Builder mBuilder;
    private EditText mNameEditText;
    private TextView mTextViewPseudoUser, mTextViewEmailUser;

    private DatabaseReference mDbUser = FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase").child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    private ValueEventListener mListenerUser;


    public ProfileFragment() {
        // Required empty public constructor
    }


    public static ProfileFragment getInstance() {
        if(sInstance == null){
            sInstance = new ProfileFragment();
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

        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        /**
         * Instanciating useful controllers
         */
        mUserController = UserController.getsInstance();
        mStatsController = StatisticsController.getInstance();
        userStats = mStatsController.getUserStats();


        /**
         * Retrieving elements from view
         */
        mTextViewPseudoUser = (TextView) rootView.findViewById(R.id.textViewPseudoUser);
        mTextViewEmailUser = (TextView) rootView.findViewById(R.id.textViewEmailUser);
        AppBarLayout appbarLayout = (AppBarLayout) rootView.findViewById(R.id.materialup_appbar);
        mProfileImage = (ImageView) rootView.findViewById(R.id.materialup_profile_image);
        mListViewStats = (ListView) rootView.findViewById(R.id.listViewStats);

        /**
         * Setting up listener to retrieve user from firebase db
         */
        mListenerUser = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User connectedUser = dataSnapshot.getValue(User.class);
                if(connectedUser != null){
                    mTextViewPseudoUser.setText(connectedUser.getPseudo());
                    mProfileImage.setImageBitmap(connectedUser.convertedPhoto());
                    mTextViewEmailUser.setText(connectedUser.getEmail());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        if (mListenerUser != null)
            mDbUser.addValueEventListener(mListenerUser);


        /**
         * Setting up listeners in order to allow user to modify his profile
         */
        mTextViewPseudoUser.setOnClickListener(new View.OnClickListener() {
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

            mListStatsToDisplay.add(getString(R.string.nbMyTripsTravelledLabel) + String.valueOf(userStats.getNbMyTripsTravelled()));
            mListStatsToDisplay.add(getString(R.string.totalDistanceLabel) + String.valueOf(Utils.formatTripDistance(userStats.getTotalDistance())));
            mListStatsToDisplay.add(getString(R.string.totalDistanceBySUVLAbel) + String.valueOf(Utils.formatTripDistance(userStats.getTotalDistanceBySUV())));
            mListStatsToDisplay.add(getString(R.string.totalDistanceByWalkLabel) + String.valueOf(Utils.formatTripDistance(userStats.getTotalDistanceByWalk())));
            mListStatsToDisplay.add(getString(R.string.nbTripsCreatedLabel) + String.valueOf(userStats.getNbTripsCreated()));
            mListStatsToDisplay.add(getString(R.string.nbTripsSUVCreatedLabel) + String.valueOf(userStats.getNbTripsSUVCreated()));
            mListStatsToDisplay.add(getString(R.string.nbTripsWalkingCreatedLabel) + String.valueOf(userStats.getNbTripsWalkingCreated()));
            mListStatsToDisplay.add(getString(R.string.totalTimeTravelledLabel) + String.valueOf(Utils.formatTripTime(userStats.getTotalTimeTravelled())));
            mListStatsToDisplay.add(getString(R.string.totalTimeDroveLabel) + String.valueOf(Utils.formatTripTime(userStats.getTotalTimeDrove())));
            mListStatsToDisplay.add(getString(R.string.totalTimeWalkedLabel) + String.valueOf(Utils.formatTripTime(userStats.getTotalTimeWalked())));

            mAdapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_list_item_1,
                    mListStatsToDisplay);

            // Create an ArrayAdapter from List
            mAdapter = new ArrayAdapter<String>
                    (getContext(), android.R.layout.simple_list_item_1, mListStatsToDisplay){
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

            mListViewStats.setAdapter(mAdapter);
            Log.d("userStats", "not null");
        }else{
            Log.d("userStats", "null");
        }

        /**
         * Displaying user information
         */
        mTextViewPseudoUser.setText(mUserController.getmConnectedUser().getPseudo());
        mTextViewEmailUser.setText(mUserController.getmConnectedUser().getEmail());
        mProfileImage.setImageBitmap(mUserController.getmConnectedUser().convertedPhoto());

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
                mUserController.updatePhotoUser(bitmap);
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
            mBuilder = new AlertDialog.Builder(getContext(), R.drawable.custom_alert_dialog);
        } else {
            mBuilder = new AlertDialog.Builder(getContext());
        }
        LinearLayout alertLayout = new LinearLayout(getContext());
        alertLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(Utils.convertPixelsToDp(20, getContext()), Utils.convertPixelsToDp(40, getContext()), Utils.convertPixelsToDp(20, getContext()), Utils.convertPixelsToDp(40, getContext()));
        alertLayout.setLayoutParams(params);

        mNameEditText = new EditText(getContext());
        mNameEditText.setText(mUserController.getmConnectedUser().getPseudo());

        alertLayout.addView(mNameEditText);

        mBuilder.setTitle("Update your name")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mUserController.updatePseudoUser(mNameEditText.getText().toString());
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
        if (mListenerUser != null)
            mDbUser.removeEventListener(mListenerUser);
    }



}
