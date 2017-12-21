package fr.univtln.cniobechoudayer.pimpmytrip.controllers;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import fr.univtln.cniobechoudayer.pimpmytrip.entities.Position;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.User;

/**
 * Class which represents a User controller
 * to manage CRUD operations and operations on User class
 * NB: THIS IS A SINGLETON
 */

public class UserController {


    /***********
     * MEMBERS *
     **********/

    private static UserController sInstance;
    private User mConnectedUser;

    private final FirebaseAuth fFirebaseAuth;
    private final FirebaseUser fCurrentUser;
    private final ValueEventListener fListenerUser, fListenerUsers;

    private String mCurrentUserId;
    private Map<String, User> mMapUsers;

    private DatabaseReference mDatabase, mDbUser, mDatabaseUsersConnectedReference, mDbAllUsers;



    /***************
     * CONSTRUCTOR *
     ***************/

    private UserController() {
        fFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase");
        fCurrentUser = fFirebaseAuth.getCurrentUser();
        mMapUsers = new HashMap<>();
        mDbAllUsers = mDatabase.child("users");
        if (fCurrentUser != null) {
            mCurrentUserId = fFirebaseAuth.getCurrentUser().getUid();
            mDbUser = mDatabase.child("users").child(mCurrentUserId);
            mDatabaseUsersConnectedReference = mDatabase.child("connectedUsers").child(mCurrentUserId);
        }

        fListenerUsers = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                    User user = userSnapshot.getValue(User.class);
                    mMapUsers.put(userSnapshot.getKey(), user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


        /**
         * Setting up listener to retrieve user from firebase db
         */
        fListenerUser = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mConnectedUser = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        if(mDbUser != null){
            mDbUser.addValueEventListener(fListenerUser);
        }
        if(mDbAllUsers != null)
            mDbAllUsers.addValueEventListener(fListenerUsers);

    }

    /**
     * Method which returns the sInstance
     *
     * @return the unique sInstance
     */
    public static UserController getInstance() {
        if (sInstance == null) {
            sInstance = new UserController();
        }
        return sInstance;
    }

    /***************
     *   GETTERS   *
     *     AND     *
     *   SETTERS   *
     ***************/

    public User getmConnectedUser() {
        return mConnectedUser;
    }


    public Map<String, User> getmMapUsers() {
        return mMapUsers;
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
            mDatabase.child("users").child(mCurrentUserId).setValue(user);
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
        mDatabase.child("users").child(mCurrentUserId).child("pseudo").setValue(pseudo);
    }

    /**
     * Method that updates the email of the connected user
     *
     * @param email new email
     */
    public void updateEmailUser(String email) {
        mDatabase.child("users").child(mCurrentUserId).child("email").setValue(email);
    }

    /**
     * Methd to update user profile picture of connected user
     *
     * @param photo new photo
     */
    public void updatePhotoUser(Bitmap photo) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        mDatabase.child("users").child(mCurrentUserId).child("photo").setValue(encoded);
    }

    /**
     * Method that delete the connected user
     */
    public void deleteUser() {
        mDatabase.child("users").child(mCurrentUserId).removeValue();
    }

    /**
     * Method that returns the ID of the current connected user
     *
     * @return
     */
    public String getConnectedUserId() {
        return mCurrentUserId;
    }

    /**
     * Method that updates in mDatabase the fact that a user is connected
     */
    public void setUserAsConnected() {
        mDatabase.child("connectedUsers").child(mCurrentUserId).setValue(fCurrentUser);
        Position position = new Position(0.0,0.0);
        mDatabase.child("connectedUsers").child(mCurrentUserId).child("lastKnownLocation").setValue(position);
    }

    /**
     * Methods to get the current connected user
     *
     * @return
     */
    public FirebaseUser getfCurrentUser() {
        return fCurrentUser;
    }

    /**
     * Method that updates in mDatabase the fact that a user is disconnected
     */
    public void setUserAsDisconnected() {
        if(mDatabaseUsersConnectedReference != null)
            mDatabaseUsersConnectedReference.removeValue();
    }

    /**
     * Method that updates in mDatabase the last known location of the connected user
     *
     * @param position new last known position
     */
    public void updateLastKnownUserLocation(Position position) {
        mDatabaseUsersConnectedReference.child("lastKnownLocation").setValue(position);
    }

    /**
     * Methods that returns if the user is a manager or not
     *
     * @return true if the user is a manager, otherwise false
     */
    public boolean isUserManager() {
        if (this.mConnectedUser != null) {
            if (this.mConnectedUser.isManager())
                return true;
            else
                return false;
        } else {
            return false;
        }
    }


}
