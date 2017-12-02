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

    public void createTrip(boolean isReference, List<Position> listPositions, List<Waypoint> listWaypoints, String color, String name, int distance, String creatorId){
        String keyTrip = database.child("trips").child(currentUserId).push().getKey();
        Trip newTrip = new Trip.TripBuilder(keyTrip,name).reference(isReference).listPositions(listPositions).listWaypoints(listWaypoints)
                .color(color).distance(distance).creator(creatorId).build();
        database.child("trips").child(currentUserId).child(keyTrip).setValue(newTrip);

    }

    public void deleteTrip(Trip tripToDelete){
        database.child("trips").child(currentUserId).child(tripToDelete.getId()).removeValue();
    }
}
