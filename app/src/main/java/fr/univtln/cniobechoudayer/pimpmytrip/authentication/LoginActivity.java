package fr.univtln.cniobechoudayer.pimpmytrip.authentication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import fr.univtln.cniobechoudayer.pimpmytrip.activities.MainActivity;
import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.utils.Utils;
import fr.univtln.cniobechoudayer.pimpmytrip.controllers.UserController;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Login Activity to handle the process of logging in by users
 */

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private static final int RC_SIGN_IN = 007;

    private UserController mUserController;

    private EditText mEmailEditText, mPasswordEditText;
    private Button mBtnLogin, mBtnSignup, mBtnForgotPassword, mBtnSignInWithGoogle;
    private String mEmail, mPassword;
    private ProgressBar mProgressBar;
    private CoordinatorLayout mRootView;

    private FirebaseAuth mAuth;
    public static GoogleSignInClient mGoogleSignInClient;
    public static GoogleApiClient mGoogleApiClient;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setLanguage();

        /**
         * Retrieving graphic elements from view layout
         */

        mRootView = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutLogin);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBarLogin);
        mEmailEditText = (EditText) findViewById(R.id.editTxtEmailLogin);
        mPasswordEditText = (EditText) findViewById(R.id.editTxtPasswordLogin);
        mBtnLogin = (Button) findViewById(R.id.sign_in_button);
        mBtnSignup = (Button) findViewById(R.id.sign_up_button_from_login);
        mBtnSignInWithGoogle = (Button) findViewById(R.id.sign_in_with_google_button);
        mBtnForgotPassword = (Button) findViewById(R.id.btn_reset_password);

        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Utils.displayErrorMessage(getApplicationContext(), LoginActivity.this, mRootView, "Failed to connect");
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Utils.displayErrorMessage(getApplicationContext(), LoginActivity.this, mRootView, "Failed to connect");
                    }
                })
                .build();

        /**
         * Setting up click listeners
         */

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                // NOTE: In the author's example, he uses an identifier
                // called searchBar. If setting this code on your EditText
                // then use v.getWindowToken() as a reference to your
                // EditText is passed into this callback as a TextView

                in.hideSoftInputFromWindow(mRootView.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                loginWithEmail();
            }
        });
        mBtnSignInWithGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginWithGoogle();
            }
        });

        mBtnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
            }
        });

        mBtnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the ResetPassword activity
                Intent intent = new Intent(getApplicationContext(), ResetPasswordActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Disable going back
        moveTaskToBack(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent startMainActivity = new Intent(this, MainActivity.class);
            //startActivity(startMainActivity);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent startMainActivity = new Intent(this, MainActivity.class);
            //startActivity(startMainActivity);
        }
    }

    /**
     * Validates all inputs (mEmail and mPassword here) if they are not empty and in the expected format.
     *
     * @return true if it's ok or false if there is an error
     */
    private boolean validateInputs() {
        boolean valid = true;

        if (TextUtils.isEmpty(mEmail) || !android.util.Patterns.EMAIL_ADDRESS.matcher(mEmail).matches()) {
            mEmailEditText.setError("Enter a valid mEmail address");
            valid = false;
        } else {
            mEmailEditText.setError(null);
        }

        if (TextUtils.isEmpty(mPassword)) {
            mPasswordEditText.setError("Enter a mPassword");
            valid = false;
        } else {
            mPasswordEditText.setError(null);
        }

        return valid;
    }

    /**
     * Login with mEmail
     */
    private void loginWithEmail() {
        mEmail = mEmailEditText.getText().toString();
        mPassword = mPasswordEditText.getText().toString();

        if (validateInputs()) {
            mProgressBar.setVisibility(View.VISIBLE);

            //authenticate user
            mAuth.signInWithEmailAndPassword(mEmail, mPassword)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the mAuth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            mProgressBar.setVisibility(View.GONE);
                            if (!task.isSuccessful()) {
                                Utils.displayErrorMessage(getApplicationContext(), LoginActivity.this, mRootView, getResources().getString(R.string.auth_failed));
                            } else {
                                onConnectionSuccess();
                            }
                        }
                    });

        } else {
            Utils.displayErrorMessage(getApplicationContext(), LoginActivity.this, mRootView, "Login failed");
        }

    }

    /**
     * Starts the intent to login with a Google account
     */
    private void loginWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Get results from intent launched
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Utils.displayErrorMessage(getApplicationContext(), LoginActivity.this, mRootView, "Google sign in failed");
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    /**
     * Method to set up the language
     */
    private void setLanguage(){
        Log.d("set", "language");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d("language", prefs.getString(getString(R.string.preferenceLanguage), "en"));
        /*    Locale myLocale = new Locale(prefs.getString(getString(R.string.preferenceLanguage), "en"));
            Locale.setDefault(new Locale("en"));
            android.content.res.Configuration config = new android.content.res.Configuration();
            config.locale = myLocale;
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);*/
    }

    /**
     * Signing in with Google
     * @param acct
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        mProgressBar.setVisibility(View.VISIBLE);
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            mProgressBar.setVisibility(View.GONE);
                            onConnectionSuccess();
                        } else {
                            mProgressBar.setVisibility(View.GONE);
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Utils.displayErrorMessage(getApplicationContext(), LoginActivity.this, mRootView, "Authentication failed");
                        }
                    }
                });
    }

    /**
     * Method that sets the user as connected
     * and calls the main view
     */
    private void onConnectionSuccess() {
        mUserController = UserController.getsInstance();
        mUserController.setUserAsConnected();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
