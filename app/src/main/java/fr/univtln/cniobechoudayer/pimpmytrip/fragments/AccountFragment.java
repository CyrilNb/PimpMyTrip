package fr.univtln.cniobechoudayer.pimpmytrip.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Messenger;
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
import fr.univtln.cniobechoudayer.pimpmytrip.services.RecordUserLocationService;
import fr.univtln.cniobechoudayer.pimpmytrip.utils.Utils;

public class AccountFragment extends Fragment implements View.OnClickListener {

    private Button btnChangeEmail, btnChangePassword, btnSendResetEmail, btnRemoveUser,
            changeEmail, changePassword, sendEmail, remove, signOut, btnRevokeAccessGoogle, btnChangeLanguage;

    private EditText oldEmail, newEmail, password, newPassword;
    private AlertDialog.Builder builder;
    private ProgressBar progressBar;
    private CoordinatorLayout coordinatorLayout;

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private static AccountFragment singleton;

    //Managing the singleton
    public static AccountFragment getInstance() {

        if (singleton == null) {
            singleton = new AccountFragment();
        }

        return singleton;
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
        btnChangeEmail = (Button) rootView.findViewById(R.id.change_email_button);
        btnChangePassword = (Button) rootView.findViewById(R.id.change_password_button);
        btnChangeLanguage = (Button) rootView.findViewById(R.id.change_language);
        btnSendResetEmail = (Button) rootView.findViewById(R.id.sending_pass_reset_button);
        btnRemoveUser = (Button) rootView.findViewById(R.id.remove_user_button);
        btnRevokeAccessGoogle = (Button) rootView.findViewById(R.id.revoke_access_google);
        changeEmail = (Button) rootView.findViewById(R.id.changeEmail);
        changePassword = (Button) rootView.findViewById(R.id.changePass);
        sendEmail = (Button) rootView.findViewById(R.id.send);
        remove = (Button) rootView.findViewById(R.id.remove);
        signOut = (Button) rootView.findViewById(R.id.sign_out);

        oldEmail = (EditText) rootView.findViewById(R.id.old_email);
        newEmail = (EditText) rootView.findViewById(R.id.new_email);
        password = (EditText) rootView.findViewById(R.id.password);
        newPassword = (EditText) rootView.findViewById(R.id.newPassword);

        oldEmail.setVisibility(View.GONE);
        newEmail.setVisibility(View.GONE);
        password.setVisibility(View.GONE);
        newPassword.setVisibility(View.GONE);
        changeEmail.setVisibility(View.GONE);
        changePassword.setVisibility(View.GONE);
        sendEmail.setVisibility(View.GONE);
        remove.setVisibility(View.GONE);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        coordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinatorLayoutSettings);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        /**
         * get firebase auth instance
         */
        auth = FirebaseAuth.getInstance();

        /**
         * get current user
         */
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        /**
         * Setting up listeners
         */
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                }
            }
        };

        btnChangePassword.setOnClickListener(this);
        btnChangeEmail.setOnClickListener(this);
        btnSendResetEmail.setOnClickListener(this);

        btnChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (user != null && !newEmail.getText().toString().trim().equals("")) {
                    user.updateEmail(newEmail.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Utils.displayErrorMessage(getContext(), getActivity(), coordinatorLayout, "Email address is updated. Please sign in with new email!");
                                        signOut();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Utils.displayErrorMessage(getContext(),getActivity(),coordinatorLayout,"Failed to update email!");
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                } else if (newEmail.getText().toString().trim().equals("")) {
                    newEmail.setError("Enter email");
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        btnChangeLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayChangeLanguageAlertDialog();
            }
        });

        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (!oldEmail.getText().toString().trim().equals("")) {
                    auth.sendPasswordResetEmail(oldEmail.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Utils.displayErrorMessage(getContext(),getActivity(),coordinatorLayout,"Reset password email is sent!");
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Utils.displayErrorMessage(getContext(),getActivity(),coordinatorLayout,"Failed to send reset email!");
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                } else {
                    oldEmail.setError("Enter email");
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (user != null && !newPassword.getText().toString().trim().equals("")) {
                    if (newPassword.getText().toString().trim().length() < 6) {
                        newPassword.setError("Password too short, enter minimum 6 characters");
                        progressBar.setVisibility(View.GONE);
                    } else {
                        user.updatePassword(newPassword.getText().toString().trim())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Utils.displayErrorMessage(getContext(),getActivity(),coordinatorLayout,"Password is updated, sign in with new password!");
                                            signOut();
                                            progressBar.setVisibility(View.GONE);
                                        } else {
                                            Utils.displayErrorMessage(getContext(),getActivity(),coordinatorLayout,"Failed to update password!");
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    }
                } else if (newPassword.getText().toString().trim().equals("")) {
                    newPassword.setError("Enter password");
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        btnRemoveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (user != null) {
                    user.delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Utils.displayErrorMessage(getContext(), getActivity(), coordinatorLayout, "Your profile is deleted :(");
                                        startActivity(new Intent(getActivity(), SignUpActivity.class));
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Utils.displayErrorMessage(getContext(),getActivity(),coordinatorLayout,"Failed to remove account");
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                }
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        btnRevokeAccessGoogle.setOnClickListener(new View.OnClickListener() {
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
            builder = new AlertDialog.Builder(getContext(), R.style.CustomDialogTheme);
        } else {
            builder = new AlertDialog.Builder(getContext());
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

        builder.setTitle(getString(R.string.titleDialogLanguage))
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
                oldEmail.setVisibility(View.GONE);
                newEmail.setVisibility(View.GONE);
                password.setVisibility(View.VISIBLE);
                newPassword.setVisibility(View.VISIBLE);
                changeEmail.setVisibility(View.GONE);
                changePassword.setVisibility(View.VISIBLE);
                sendEmail.setVisibility(View.GONE);
                remove.setVisibility(View.GONE);
                break;
            case R.id.sending_pass_reset_button:
                oldEmail.setVisibility(View.VISIBLE);
                newEmail.setVisibility(View.GONE);
                password.setVisibility(View.GONE);
                newPassword.setVisibility(View.GONE);
                changeEmail.setVisibility(View.GONE);
                changePassword.setVisibility(View.GONE);
                sendEmail.setVisibility(View.VISIBLE);
                remove.setVisibility(View.GONE);
                break;
            case R.id.change_email_button:
                oldEmail.setVisibility(View.GONE);
                newEmail.setVisibility(View.VISIBLE);
                password.setVisibility(View.GONE);
                newPassword.setVisibility(View.GONE);
                changeEmail.setVisibility(View.VISIBLE);
                changePassword.setVisibility(View.GONE);
                sendEmail.setVisibility(View.GONE);
                remove.setVisibility(View.GONE);
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
            auth.signOut();
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
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }
}
