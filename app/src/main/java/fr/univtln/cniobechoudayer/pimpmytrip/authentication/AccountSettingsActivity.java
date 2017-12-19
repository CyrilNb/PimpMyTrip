package fr.univtln.cniobechoudayer.pimpmytrip.authentication;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.utils.Utils;

/**
 * Activity to allow user managing his account settings
 */
public class AccountSettingsActivity extends AppCompatActivity implements View.OnClickListener{

    private Button mBtnChangeEmail, mBtnChangePassword, mBtnSendResetEmail, mBtnRemoveUser,
            mChangeEmail, mChangePassword, mBtnSendEmail, mBtnRemove, mBtnSignOut, mBtnRevokeAccessGoogle;

    private EditText mOldEmail, mNewEmail, mPassword, mNewPassword;
    private ProgressBar mProgressBar;
    private CoordinatorLayout mCoordinatorLayout;

    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        /**
         * Retrieving elements from view
         */
        mBtnChangeEmail = (Button) findViewById(R.id.change_email_button);
        mBtnChangePassword = (Button) findViewById(R.id.change_password_button);
        mBtnSendResetEmail = (Button) findViewById(R.id.sending_pass_reset_button);
        mBtnRemoveUser = (Button) findViewById(R.id.remove_user_button);
        mBtnRevokeAccessGoogle = (Button) findViewById(R.id.revoke_access_google);
        mChangeEmail = (Button) findViewById(R.id.changeEmail);
        mChangePassword = (Button) findViewById(R.id.changePass);
        mBtnSendEmail = (Button) findViewById(R.id.send);
        mBtnRemove = (Button) findViewById(R.id.remove);
        mBtnSignOut = (Button) findViewById(R.id.sign_out);

        mOldEmail = (EditText) findViewById(R.id.old_email);
        mNewEmail = (EditText) findViewById(R.id.new_email);
        mPassword = (EditText) findViewById(R.id.password);
        mNewPassword = (EditText) findViewById(R.id.newPassword);

        mOldEmail.setVisibility(View.GONE);
        mNewEmail.setVisibility(View.GONE);
        mPassword.setVisibility(View.GONE);
        mNewPassword.setVisibility(View.GONE);
        mChangeEmail.setVisibility(View.GONE);
        mChangePassword.setVisibility(View.GONE);
        mBtnSendEmail.setVisibility(View.GONE);
        mBtnRemove.setVisibility(View.GONE);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutSettings);

        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }

        /**
         * get firebase mAuth instance
         */
        mAuth = FirebaseAuth.getInstance();

        /**
         * get current user
         */
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        /**
         * Setting up listeners
         */
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user mAuth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(AccountSettingsActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        mBtnChangePassword.setOnClickListener(this);
        mBtnChangeEmail.setOnClickListener(this);
        mBtnSendResetEmail.setOnClickListener(this);

        mBtnChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                if (user != null && !mNewEmail.getText().toString().trim().equals("")) {
                    user.updateEmail(mNewEmail.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Utils.displayErrorMessage(getApplicationContext(), AccountSettingsActivity.this, mCoordinatorLayout, "Email address is updated. Please sign in with new email!");
                                        signOut();
                                        mProgressBar.setVisibility(View.GONE);
                                    } else {
                                        Utils.displayErrorMessage(getApplicationContext(), AccountSettingsActivity.this, mCoordinatorLayout, "Failed to update email!");
                                        mProgressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                } else if (mNewEmail.getText().toString().trim().equals("")) {
                    mNewEmail.setError("Enter email");
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });

        mBtnSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                if (!mOldEmail.getText().toString().trim().equals("")) {
                    mAuth.sendPasswordResetEmail(mOldEmail.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Utils.displayErrorMessage(getApplicationContext(), AccountSettingsActivity.this, mCoordinatorLayout, "Reset mPassword email is sent!");
                                        mProgressBar.setVisibility(View.GONE);
                                    } else {
                                        Utils.displayErrorMessage(getApplicationContext(), AccountSettingsActivity.this, mCoordinatorLayout, "Failed to send reset email!");
                                        mProgressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                } else {
                    mOldEmail.setError("Enter email");
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });

        mChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                if (user != null && !mNewPassword.getText().toString().trim().equals("")) {
                    if (mNewPassword.getText().toString().trim().length() < 6) {
                        mNewPassword.setError("Password too short, enter minimum 6 characters");
                        mProgressBar.setVisibility(View.GONE);
                    } else {
                        user.updatePassword(mNewPassword.getText().toString().trim())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Utils.displayErrorMessage(getApplicationContext(), AccountSettingsActivity.this, mCoordinatorLayout, "Password is updated, sign in with new mPassword!");
                                            signOut();
                                            mProgressBar.setVisibility(View.GONE);
                                        } else {
                                            Utils.displayErrorMessage(getApplicationContext(), AccountSettingsActivity.this, mCoordinatorLayout, "Failed to update mPassword!");
                                            mProgressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    }
                } else if (mNewPassword.getText().toString().trim().equals("")) {
                    mNewPassword.setError("Enter mPassword");
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });

        mBtnRemoveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                if (user != null) {
                    user.delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Utils.displayErrorMessage(getApplicationContext(), AccountSettingsActivity.this, mCoordinatorLayout, "Your profile is deleted :(");
                                        startActivity(new Intent(AccountSettingsActivity.this, SignUpActivity.class));
                                        finish();
                                        mProgressBar.setVisibility(View.GONE);
                                    } else {
                                        Utils.displayErrorMessage(getApplicationContext(), AccountSettingsActivity.this, mCoordinatorLayout, "Failed to mBtnRemove account");
                                        mProgressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                }
            }
        });

        mBtnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        mBtnRevokeAccessGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revokeAccess();
            }
        });

    }

    /**
     * sign out method
     */
    public void signOut() {
        if (LoginActivity.googleApiClient != null && LoginActivity.googleApiClient.isConnected()) //has to be only one instance of googleApiClient in the app
            {
            LoginActivity.googleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent = new Intent(AccountSettingsActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
            } else {
            mAuth.signOut();
        }
    }

    private void revokeAccess() {
        LoginActivity.googleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(AccountSettingsActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    /**
     * Handling life cycle methods
     */
    @Override
    protected void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * Getting on click when need to change view
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.change_password_button:
                mOldEmail.setVisibility(View.GONE);
                mNewEmail.setVisibility(View.GONE);
                mPassword.setVisibility(View.VISIBLE);
                mNewPassword.setVisibility(View.VISIBLE);
                mChangeEmail.setVisibility(View.GONE);
                mChangePassword.setVisibility(View.VISIBLE);
                mBtnSendEmail.setVisibility(View.GONE);
                mBtnRemove.setVisibility(View.GONE);
                break;
            case R.id.sending_pass_reset_button:
                mOldEmail.setVisibility(View.VISIBLE);
                mNewEmail.setVisibility(View.GONE);
                mPassword.setVisibility(View.GONE);
                mNewPassword.setVisibility(View.GONE);
                mChangeEmail.setVisibility(View.GONE);
                mChangePassword.setVisibility(View.GONE);
                mBtnSendEmail.setVisibility(View.VISIBLE);
                mBtnRemove.setVisibility(View.GONE);
                break;
            case R.id.change_email_button:
                mOldEmail.setVisibility(View.GONE);
                mNewEmail.setVisibility(View.VISIBLE);
                mPassword.setVisibility(View.GONE);
                mNewPassword.setVisibility(View.GONE);
                mChangeEmail.setVisibility(View.VISIBLE);
                mChangePassword.setVisibility(View.GONE);
                mBtnSendEmail.setVisibility(View.GONE);
                mBtnRemove.setVisibility(View.GONE);
                break;
        }
    }
}
