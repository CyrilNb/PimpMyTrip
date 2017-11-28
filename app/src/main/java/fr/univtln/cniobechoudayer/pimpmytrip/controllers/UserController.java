package fr.univtln.cniobechoudayer.pimpmytrip.controllers;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import fr.univtln.cniobechoudayer.pimpmytrip.Entities.User;

/**
 * Controller of User class
 * Created by Cyril Niobé on 24/11/2017.
 */

public class UserController {
    private static final String TAG = "UserController";

    //FirebaseAuth
    private FirebaseAuth firebaseAuth;
    private DatabaseReference database;
    private FirebaseUser currentUser;
    private static String currentUserId;

    //TODO singleton
    public UserController() {
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase");
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = firebaseAuth.getCurrentUser().getUid();
        }
    }

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

    public void updatePseudoUser(String pseudo) {
        database.child(currentUserId).child("pseudo").setValue(pseudo);
    }

    public void updateEmailUser(String email) {
        database.child(currentUserId).child("email").setValue(email);
    }

    public void deleteUser() {
        database.child(currentUserId).removeValue();
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

    public static String getConnectedUserId(){
        return currentUserId;
    }

}
