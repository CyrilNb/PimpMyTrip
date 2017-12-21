package fr.univtln.cniobechoudayer.pimpmytrip.controllers;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import fr.univtln.cniobechoudayer.pimpmytrip.entities.Statistics;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.Trip;

/**
 * Controller to manage CRUD of user statistics
 * NB : SINGLETON PATTERN IMPLEMENTED
 */

public class StatisticsController {

    private static StatisticsController sInstance;
    private static ValueEventListener sListenerDbStats;
    private static Statistics sUserStats;
    private static DatabaseReference sDbStats = FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase").child("statistics");
    private static DatabaseReference sDbUserStats = sDbStats.child(FirebaseAuth.getInstance().getCurrentUser().getUid());


    public StatisticsController() {


        /**
         * Setting up sDatabase listeners
         */
        sListenerDbStats = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sUserStats = dataSnapshot.getValue(Statistics.class);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        sDbUserStats.addValueEventListener(sListenerDbStats);
    }

    /**
     * Singleton pattern
     *
     * @return
     */
    public static StatisticsController getInstance() {
        if (sInstance == null) {
            sInstance = new StatisticsController();
        }
        return sInstance;
    }

    /**
     * Method that update statistics of user when creating a new trip
     *
     * @param savedTrip
     * @param isUserWalking
     */
    public static void updateStats(Trip savedTrip, boolean isUserWalking) {

        if (sUserStats != null) {
            sUserStats.setNbMyTripsTravelled(sUserStats.getNbMyTripsTravelled() + 1);
            sUserStats.setNbTripsCreated(sUserStats.getNbTripsCreated() + 1);

            if (isUserWalking) {
                sUserStats.setNbTripsSUVCreated(sUserStats.getNbTripsSUVCreated() + 1);
                sUserStats.setTotalTimeDrove(sUserStats.getTotalTimeDrove() + 1);
                sUserStats.setTotalDistanceBySUV(sUserStats.getTotalDistanceBySUV() + savedTrip.getDistance());
            } else {
                sUserStats.setNbTripsWalkingCreated(sUserStats.getNbTripsWalkingCreated() + 1);
                sUserStats.setTotalTimeWalked(sUserStats.getTotalTimeWalked() + savedTrip.getDistance());
                sUserStats.setTotalDistanceByWalk(sUserStats.getTotalDistanceByWalk() + savedTrip.getDistance());
            }

            sUserStats.setTotalDistance(sUserStats.getTotalDistance() + savedTrip.getDistance());
            sUserStats.setTotalTimeTravelled(sUserStats.getTotalTimeTravelled() + savedTrip.getDistance());

            sDbUserStats.setValue(sUserStats);
        } else {
            sDbStats.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(new Statistics());
        }

    }

    /**
     * Getter for user stats
     *
     * @return
     */
    public Statistics getUserStats() {
        return sUserStats;
    }


}
