package fr.univtln.cniobechoudayer.pimpmytrip.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.List;

import fr.univtln.cniobechoudayer.pimpmytrip.Entities.Position;
import fr.univtln.cniobechoudayer.pimpmytrip.Entities.Trip;
import fr.univtln.cniobechoudayer.pimpmytrip.Entities.Waypoint;

public class TripController {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference database;
    private FirebaseUser currentUser;
    private String currentUserId;
    private static TripController singleton;
    private List<Position> listPositions;

    public TripController() {
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase");
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = firebaseAuth.getCurrentUser().getUid();
        }

    }

    public static TripController getInstance(){
        if(singleton == null){
            singleton = new TripController();
        }
        return singleton;
    }

    public Trip createTrip(boolean isReference, List<Position> listPositions, List<Waypoint> listMarkers, String color, String name, int distance, String creatorId){
        Trip newTrip = new Trip(color, Calendar.getInstance().getTime(), name, isReference, listPositions, listMarkers, distance, creatorId);
        database.child("trips").child(currentUserId).push().setValue(newTrip);
        return newTrip;
    }

    public void deleteTrip(String id){
        //TODO
        database.child("trips").child(currentUserId).removeValue();
    }
}
