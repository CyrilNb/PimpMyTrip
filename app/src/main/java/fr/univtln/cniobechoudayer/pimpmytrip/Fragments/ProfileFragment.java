package fr.univtln.cniobechoudayer.pimpmytrip.Fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import fr.univtln.cniobechoudayer.pimpmytrip.Entities.Statistics;
import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.Utils.CircleTransform;
import fr.univtln.cniobechoudayer.pimpmytrip.controllers.StatisticsController;


public class ProfileFragment extends Fragment implements AppBarLayout.OnOffsetChangedListener{

    private ImageView mProfileImage;
    private int mMaxScrollSize;
    private static final int PERCENTAGE_TO_ANIMATE_AVATAR = 20;
    private boolean mIsAvatarShown = true;
    private ListView listViewStats;
    private List<String> listStatsToDisplay = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private Statistics userStats;

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

        StatisticsController statsController = StatisticsController.getInstance();
        userStats = statsController.getUserStats();

        //mProfileImage = (ImageView) rootView.findViewById(R.id.profilePicture);

        //Picasso.with(getContext()).load(R.drawable.test_profile).transform(new CircleTransform()).into(mProfileImage);
        // Inflate the layout for this fragment

        AppBarLayout appbarLayout = (AppBarLayout) rootView.findViewById(R.id.materialup_appbar);
        mProfileImage = (ImageView) rootView.findViewById(R.id.materialup_profile_image);
        listViewStats = (ListView) rootView.findViewById(R.id.listViewStats);

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

        if(userStats != null){

            listStatsToDisplay.add(String.valueOf(userStats.getNbMyTripsTravelled()));
            listStatsToDisplay.add(String.valueOf(userStats.getTotalDistance()));
            listStatsToDisplay.add(String.valueOf(userStats.getTotalDistanceBySUV()));
            listStatsToDisplay.add(String.valueOf(userStats.getTotalDistanceByWalk()));
            listStatsToDisplay.add(String.valueOf(userStats.getNbTripsCreated()));
            listStatsToDisplay.add(String.valueOf(userStats.getNbTripsSUVCreated()));
            listStatsToDisplay.add(String.valueOf(userStats.getNbTripsWalkingCreated()));
            listStatsToDisplay.add(String.valueOf(userStats.getTotalTimeTravelled()));
            listStatsToDisplay.add(String.valueOf(userStats.getTotalTimeDrove()));
            listStatsToDisplay.add(String.valueOf(userStats.getTotalTimeWalked()));

            adapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_list_item_1,
                    listStatsToDisplay);

            listViewStats.setAdapter(adapter);
            Log.d("userStats", "not null");
        }else{
            Log.d("userStats", "null");
        }

        return rootView;
    }

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



}
