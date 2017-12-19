package fr.univtln.cniobechoudayer.pimpmytrip.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;
import java.util.concurrent.Executor;

import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.authentication.LoginActivity;
import fr.univtln.cniobechoudayer.pimpmytrip.authentication.SignUpActivity;
import fr.univtln.cniobechoudayer.pimpmytrip.utils.Utils;

public class AccountFragment extends Fragment implements View.OnClickListener {

    private Button mBtnChangeEmail, mBtnChangePassword, mBtnSendResetEmail, mBtnRemoveUser,
            mChangeEmail, mChangePassword, mBtnSendEmail, mBtnRemove, mSignOut, mBtnRevokeAccessGoogle, mBtnChangeLanguage;

    private EditText mOldEmail, mNewEmail, mPassword, mNewPassword;
    private AlertDialog.Builder mBuilder;
    private ProgressBar mProgressBar;
    private CoordinatorLayout mCoordinatorLayout;

    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private static AccountFragment sInstance;

    //Managing the sInstance
    public static AccountFragment getInstance() {

        if (sInstance == null) {
            sInstance = new AccountFragment();
        }

        return sInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_account_settings, container, false);

        /**
         * Retrieving elements from view
         */
        mBtnChangeEmail = (Button) rootView.findViewById(R.id.change_email_button);
        mBtnChangePassword = (Button) rootView.findViewById(R.id.change_password_button);
        mBtnChangeLanguage = (Button) rootView.findViewById(R.id.change_language);
        mBtnSendResetEmail = (Button) rootView.findViewById(R.id.sending_pass_reset_button);
        mBtnRemoveUser = (Button) rootView.findViewById(R.id.remove_user_button);
        mBtnRevokeAccessGoogle = (Button) rootView.findViewById(R.id.revoke_access_google);
        mChangeEmail = (Button) rootView.findViewById(R.id.changeEmail);
        mChangePassword = (Button) rootView.findViewById(R.id.changePass);
        mBtnSendEmail = (Button) rootView.findViewById(R.id.send);
        mBtnRemove = (Button) rootView.findViewById(R.id.remove);
        mSignOut = (Button) rootView.findViewById(R.id.sign_out);

        mOldEmail = (EditText) rootView.findViewById(R.id.old_email);
        mNewEmail = (EditText) rootView.findViewById(R.id.new_email);
        mPassword = (EditText) rootView.findViewById(R.id.password);
        mNewPassword = (EditText) rootView.findViewById(R.id.newPassword);

        mOldEmail.setVisibility(View.GONE);
        mNewEmail.setVisibility(View.GONE);
        mPassword.setVisibility(View.GONE);
        mNewPassword.setVisibility(View.GONE);
        mChangeEmail.setVisibility(View.GONE);
        mChangePassword.setVisibility(View.GONE);
        mBtnSendEmail.setVisibility(View.GONE);
        mBtnRemove.setVisibility(View.GONE);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinatorLayoutSettings);

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
                    startActivity(new Intent(getActivity(), LoginActivity.class));
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
                                        Utils.displayErrorMessage(getContext(), getActivity(), mCoordinatorLayout, "Email address is updated. Please sign in with new email!");
                                        signOut();
                                        mProgressBar.setVisibility(View.GONE);
                                    } else {
                                        Utils.displayErrorMessage(getContext(), getActivity(), mCoordinatorLayout, "Failed to update email!");
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

        mBtnChangeLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayChangeLanguageAlertDialog();
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
                                        Utils.displayErrorMessage(getContext(), getActivity(), mCoordinatorLayout, "Reset mPassword email is sent!");
                                        mProgressBar.setVisibility(View.GONE);
                                    } else {
                                        Utils.displayErrorMessage(getContext(), getActivity(), mCoordinatorLayout, "Failed to send reset email!");
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
                                            Utils.displayErrorMessage(getContext(), getActivity(), mCoordinatorLayout, "Password is updated, sign in with new mPassword!");
                                            signOut();
                                            mProgressBar.setVisibility(View.GONE);
                                        } else {
                                            Utils.displayErrorMessage(getContext(), getActivity(), mCoordinatorLayout, "Failed to update mPassword!");
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
                                        Utils.displayErrorMessage(getContext(), getActivity(), mCoordinatorLayout, "Your profile is deleted :(");
                                        startActivity(new Intent(getActivity(), SignUpActivity.class));
                                        mProgressBar.setVisibility(View.GONE);
                                    } else {
                                        Utils.displayErrorMessage(getContext(), getActivity(), mCoordinatorLayout, "Failed to mBtnRemove account");
                                        mProgressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                }
            }
        });

        mSignOut.setOnClickListener(new View.OnClickListener() {
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

        return rootView;
    }

    /**
     * Method that displays an alert dialog to change app language
     */
    private void displayChangeLanguageAlertDialog() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder = new AlertDialog.Builder(getContext(), R.style.CustomDialogTheme);
        } else {
            mBuilder = new AlertDialog.Builder(getContext());
        }
        final LinearLayout alertLayout = new LinearLayout(getContext());
        alertLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(Utils.convertPixelsToDp(20, getContext()), Utils.convertPixelsToDp(40, getContext()), Utils.convertPixelsToDp(20, getContext()), Utils.convertPixelsToDp(40, getContext()));
        alertLayout.setLayoutParams(params);

        final Spinner choiceLanguage = new Spinner(getContext());
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.spinnerSelectLanguage));
        choiceLanguage.setAdapter(spinnerArrayAdapter);

        alertLayout.addView(choiceLanguage);

        mBuilder.setTitle(getString(R.string.titleDialogLanguage))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String langFormatted;
                        switch(choiceLanguage.getSelectedItem().toString()){
                            case "French":
                                langFormatted = "fr";
                                break;
                            case "English":
                                langFormatted = "en";
                                break;
                            default :
                                langFormatted = "en";
                        }
                        Locale myLocale = new Locale(langFormatted);
                        Locale.setDefault(myLocale);
                        Configuration config = new Configuration();
                        config.locale = myLocale;
                        getContext().getResources().updateConfiguration(config, getContext().getResources().getDisplayMetrics());
                        Log.d("SELECTED LANGUAGE", langFormatted);
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(getString(R.string.preferenceLanguage), langFormatted);
                        editor.commit();
                        /**
                         * Restarting the app
                         */
                        System.exit(0);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_menu_save)
                .setView(alertLayout)
                .show();

    }


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

    /**
     * sign out method
     */
    public void signOut() {
        if (LoginActivity.googleApiClient != null && LoginActivity.googleApiClient.isConnected()) //has to be only one instance of googleApiClient in the app
        {
            LoginActivity.googleSignInClient.signOut()
                    .addOnCompleteListener((Executor) this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            startActivity(intent);
                        }
                    });
        } else {
            mAuth.signOut();
        }
    }

    private void revokeAccess() {
        LoginActivity.googleSignInClient.revokeAccess()
                .addOnCompleteListener((Executor) this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                    }
                });
    }

    /**
     * Handling life cycle methods
     */
    @Override
    public void onResume() {
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
}
