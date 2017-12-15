package fr.univtln.cniobechoudayer.pimpmytrip.controllers;

import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    private FirebaseAuth firebaseAuth;
    private DatabaseReference database;
    private FirebaseUser currentUser;
    private static String currentUserId;
    private static UserController instance;

    /***************
     * CONSTRUCTOR *
     ***************/

    protected UserController() {
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase");
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = firebaseAuth.getCurrentUser().getUid();
        }
    }

    /**
     * Method which returns the singleton
     * @return the unique instance
     */
    public static UserController getInstance() {
        if (instance == null) {
            instance = new UserController();
        }
        return instance;
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
     * @param pseudo new pseudo
     */
    public void updatePseudoUser(String pseudo) {
        database.child("users").child(currentUserId).child("pseudo").setValue(pseudo);
    }

    /**
     * Method that updates the email of the connected user
     * @param email new email
     */
    public void updateEmailUser(String email) {
        database.child("users").child(currentUserId).child("email").setValue(email);
    }

    /**
     * Method that delete the connected user
     */
    public void deleteUser() {
        database.child("users").child(currentUserId).removeValue();
    }


    //TODO this method will be used in the view of the user profile
    /**
     * User data change listener
     */
    private void addUserChangeListener() {
        // User data change listener
        database.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                // Check for null
                if (user == null) {
                    Log.e(TAG, "User data is null!");
                    return;
                }

                Log.e(TAG, "User data is changed!" + user.getPseudo() + ", " + user.getEmail());

                // Display newly updated name and email
                //txtDetails.setText(user.getPseudo() + ", " + user.getEmail());

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read user", error.toException());
            }
        });
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
    }

    /**
     * Method that updates in database the fact that a user is disconnected
     */
    public void setUserAsDisconnected() {
        database.child("connectedUsers").child(currentUserId).removeValue();
    }

    /**
     * Method that updates both in database and the attribute of the connected user instance his last known location
     * @param location new last known location
     */
    public void updateLastKnownUserLocation(Location location){
        database.child("connectedUsers").child(currentUserId).child("lastKnownLocation").setValue(location);
    }

}
