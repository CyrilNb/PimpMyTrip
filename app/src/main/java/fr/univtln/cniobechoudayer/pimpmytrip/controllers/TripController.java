package fr.univtln.cniobechoudayer.pimpmytrip.controllers;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.List;

import fr.univtln.cniobechoudayer.pimpmytrip.entities.Position;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.Trip;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.Waypoint;

/**
 * Class which represents a Trip controller
 * to manage CRUD operations and operations on trips
 * NB: THIS IS A SINGLETON
 */
public class TripController {

    /***********
     * MEMBERS *
     **********/

    private FirebaseAuth firebaseAuth;
    private DatabaseReference database;
    private FirebaseUser currentUser;
    private String currentUserId;
    private static TripController instance;

    /***************
     * CONSTRUCTOR *
     ***************/

    protected TripController() {
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase");
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = firebaseAuth.getCurrentUser().getUid();
        }
    }

    /**
     * Returns the singleton
     * @return the unique instance
     */
    public static TripController getInstance(){
        if(instance == null){
            instance = new TripController();
        }
        return instance;
    }

    /***************
     *   METHODS   *
     ***************/

    /**
     * Insert a trip in db from a given trip
     * @param tripToInsert the trip to insert
     */
    public void insertTrip(Trip tripToInsert){
        database.child("trips").child(currentUserId).child(tripToInsert.getId()).setValue(tripToInsert);

    }

    /**
     * Insert a trip in db from values
     * @param isReference boolean if the trip is a reference one
     * @param listPositions list of all positions of the trip
     * @param listWaypoints list of all waypoints of the trip
     * @param color color of the trip
     * @param name name of the trip
     * @param distance distance of the trip
     * @param creatorId userID of the creator of the trip
     */
    public Trip insertTrip(boolean isReference, List<Position> listPositions, List<Waypoint> listWaypoints, String color, String name, int distance, String creatorId){
        String keyTrip = database.child("trips").child(currentUserId).push().getKey();
        Trip newTrip = new Trip.TripBuilder(keyTrip,name).reference(isReference).listPositions(listPositions).listWaypoints(listWaypoints).creationDate(Calendar.getInstance().getTime())
                .color(color).distance(distance).creator(creatorId).build();
        database.child("trips").child(currentUserId).child(keyTrip).setValue(newTrip);
        return newTrip;
    }

    /**
     * Delet the given trip from the database
     * @param tripToDelete trip to be deleted from db
     */
    public void deleteTrip(Trip tripToDelete){
        database.child("trips").child(currentUserId).child(tripToDelete.getId()).removeValue();
    }
}
