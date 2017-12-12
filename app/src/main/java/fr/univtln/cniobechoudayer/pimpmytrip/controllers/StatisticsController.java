package fr.univtln.cniobechoudayer.pimpmytrip.controllers;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import fr.univtln.cniobechoudayer.pimpmytrip.entities.Statistics;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.Trip;

public class StatisticsController {

    private static FirebaseAuth firebaseAuth;
    private static DatabaseReference database;
    private static StatisticsController singleton;
    private static ValueEventListener listenerDbStats;
    private static Statistics userStats;
    private static DatabaseReference dbStats = FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase").child("statistics");
    private static DatabaseReference dbUserStats = dbStats.child(FirebaseAuth.getInstance().getCurrentUser().getUid());


    public StatisticsController() {
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase");
        //setUpDataFromDatabase();
        listenerDbStats = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    String statsKey = dataSnapshot.getKey();
                    Log.d("statsKey", statsKey);
                    Log.d("datasnap", String.valueOf(dataSnapshot));
                    userStats = dataSnapshot.getValue(Statistics.class);
                    if(userStats != null){
                        Log.d("userStats", "retrieved : " + userStats.toString());
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        dbUserStats.addValueEventListener(listenerDbStats);
    }

    public static StatisticsController getInstance(){
        if(singleton == null){
            singleton = new StatisticsController();
        }
        return singleton;
    }

    public static void updateStats(Trip savedTrip, boolean isUserWalking){

        if(userStats != null){
            Log.d("userStats", "exists");
            userStats.setNbMyTripsTravelled(userStats.getNbMyTripsTravelled() + 1);
            userStats.setNbTripsCreated(userStats.getNbTripsCreated() + 1);

            if(isUserWalking){
                userStats.setNbTripsSUVCreated(userStats.getNbTripsSUVCreated() + 1);
                userStats.setTotalTimeDrove(userStats.getTotalTimeDrove() + 1);
                userStats.setTotalDistanceBySUV(userStats.getTotalDistanceBySUV() + savedTrip.getDistance());
            }else{
                userStats.setNbTripsWalkingCreated(userStats.getNbTripsWalkingCreated() + 1);
                userStats.setTotalTimeWalked(userStats.getTotalTimeWalked() + savedTrip.getDistance());
                userStats.setTotalDistanceByWalk(userStats.getTotalDistanceByWalk() + savedTrip.getDistance());
            }

            userStats.setTotalDistance(userStats.getTotalDistance() + savedTrip.getDistance());
            userStats.setTotalTimeTravelled(userStats.getTotalTimeTravelled() + savedTrip.getDistance());

            dbUserStats.setValue(userStats);
        }else{
            Log.d("userStats", "null");
            dbStats.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(new Statistics());
        }

    }

    public Statistics getUserStats(){
        return userStats;
    }


}
