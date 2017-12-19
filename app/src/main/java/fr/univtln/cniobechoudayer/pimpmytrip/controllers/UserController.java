package fr.univtln.cniobechoudayer.pimpmytrip.controllers;

import android.location.Location;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.List;

import fr.univtln.cniobechoudayer.pimpmytrip.entities.Position;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.User;

/**
 * Class which represents a User controller
 * to manage CRUD operations and operations on User class
 * NB: THIS IS A SINGLETON
 */

public class UserController {

    private static final String TAG = "UserController";

    /***********
     * MEMBERS *
     **********/

    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference database;
    private DatabaseReference databaseUsersConnectedReference;
    private final FirebaseUser currentUser;
    private String currentUserId;
    private static UserController instance;
    private User connectedUser;
    private final ValueEventListener listenerUser;
    private DatabaseReference dbUser;



    /***************
     * CONSTRUCTOR *
     ***************/

    private UserController() {
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase");
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = firebaseAuth.getCurrentUser().getUid();
            dbUser = database.child("users").child(currentUserId);
            Log.d("dbUser", dbUser.getKey());
            databaseUsersConnectedReference = database.child("connectedUsers").child(currentUserId);
        }else{
            Log.d("user connected", "null");
        }


        /**
         * Setting up listener to retrieve user from firebase db
         */
        listenerUser = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("setting","connected user");
                connectedUser = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        if(dbUser != null){
            Log.d("dbUser","not null");
            dbUser.addValueEventListener(listenerUser);
            //dbUser.keepSynced(true);
        }


    }

    /**
     * Method which returns the mSingleton
     *
     * @return the unique instance
     */
    public static UserController getInstance() {
        if (instance == null) {
            instance = new UserController();
        }
        return instance;
    }

    public User getConnectedUser() {
        return connectedUser;
    }

    public void setConnectedUser(User connectedUser) {
        this.connectedUser = connectedUser;
    }

    /***************
     *   METHODS   *
     ***************/

    /**
     * Creating new user node under 'users'
     */
    public boolean createUser(String pseudo, String email) {
        if (!TextUtils.isEmpty(pseudo)) {
            User user = new User(pseudo, email);
            //create CONSTANT or config file to store
            // .child(Constants.FIREBASE_CHILD_USERS);
            database.child("users").child(currentUserId).setValue(user);
            return true;
        } else {
            return false;
        }

    }

    /**
     * Method that updates the pseudo of the connected user
     *
     * @param pseudo new pseudo
     */
    public void updatePseudoUser(String pseudo) {
        database.child("users").child(currentUserId).child("pseudo").setValue(pseudo);
    }

    /**
     * Method that updates the email of the connected user
     *
     * @param email new email
     */
    public void updateEmailUser(String email) {
        database.child("users").child(currentUserId).child("email").setValue(email);
    }

    /**
     * Methd to update user profile picture of connected user
     *
     * @param photo
     */
    public void updatePhotoUser(Bitmap photo) {
        Log.d("updating photo user", "reached");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        database.child("users").child(currentUserId).child("photo").setValue(encoded);
    }

    /**
     * Method that delete the connected user
     */
    public void deleteUser() {
        database.child("users").child(currentUserId).removeValue();
    }

    /**
     * Method that returns the ID of the current connected user
     *
     * @return
     */
    public String getConnectedUserId() {
        return currentUserId;
    }

    /**
     * Method that updates in database the fact that a user is connected
     */
    public void setUserAsConnected() {
        database.child("connectedUsers").child(currentUserId).setValue(currentUser);
        Position position = new Position(0.0,0.0);
        database.child("connectedUsers").child(currentUserId).child("lastKnownLocation").setValue(position);
    }

    /**
     * Methods to get the current connected user
     *
     * @return
     */
    public FirebaseUser getCurrentUser() {
        return currentUser;
    }

    /**
     * Method that updates in database the fact that a user is disconnected
     */
    public void setUserAsDisconnected() {
        if(databaseUsersConnectedReference != null)
            databaseUsersConnectedReference.removeValue();
    }

    /**
     * Method that updates in database the last known location of the connected user
     *
     * @param position new last known position
     */
    public void updateLastKnownUserLocation(Position position) {
        databaseUsersConnectedReference.child("lastKnownLocation").setValue(position);
    }


}
