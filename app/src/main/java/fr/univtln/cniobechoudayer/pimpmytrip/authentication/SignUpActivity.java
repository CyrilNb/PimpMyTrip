package fr.univtln.cniobechoudayer.pimpmytrip.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.activities.MainActivity;
import fr.univtln.cniobechoudayer.pimpmytrip.utils.Utils;
import fr.univtln.cniobechoudayer.pimpmytrip.controllers.UserController;

/**
 * SignUp Activity using Firebase mAuth
 * Created by Cyril Niobé on 22/11/2017.
 */

public class SignUpActivity extends AppCompatActivity {

    private EditText mInputEmail, mInputPassword, mConfirmPassword, mInputPseudo;
    private Button mBtnSignIn, mBtnSignUp, mBtnResetPassword, mBtnSignUpWithGoogle;
    private CoordinatorLayout mRootView;
    private ProgressBar mProgressBar;
    private String mEmail, mPassword, mPseudo;

    private FirebaseAuth mAuth;

    private UserController mUserController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mRootView = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutSignup);
        mBtnSignIn = (Button) findViewById(R.id.sign_in_buttonFromSignupActiv);
        mBtnSignUp = (Button) findViewById(R.id.sign_up_button);
        mBtnSignUpWithGoogle = (Button) findViewById(R.id.sign_up_with_google_button);
        mInputEmail = (EditText) findViewById(R.id.editTxtEmail);
        mInputPassword = (EditText) findViewById(R.id.editTxtPassword);
        mInputPseudo = (EditText) findViewById(R.id.editTxtPseudo);
        mConfirmPassword = (EditText) findViewById(R.id.editTxtConfirmPassword);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBarSignUp);
        mBtnResetPassword = (Button) findViewById(R.id.btn_reset_password);


        //Get Singleton instances
        mAuth = FirebaseAuth.getInstance();
        mUserController = UserController.getsInstance();

        mBtnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
            }
        });

        mBtnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpWithEmail();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.GONE);
    }

    /**
     * Handle back button pressed
     */
    @Override
    public void onBackPressed() {
        //Return to the Login activity
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    /**
     * Method to sign up with mEmail
     */
    private void signUpWithEmail(){
        mEmail = mInputEmail.getText().toString().trim();
        mPassword = mInputPassword.getText().toString().trim();
        mPseudo = mInputPseudo.getText().toString().trim();

        if(validateInputs()){
        mProgressBar.setVisibility(View.VISIBLE);

        //create user
        mAuth.createUserWithEmailAndPassword(mEmail, mPassword).
                addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Toast.makeText(SignUpActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the mAuth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Utils.displayErrorMessage(getApplicationContext(),SignUpActivity.this, mRootView,"Register failed");
                        } else {
                            mUserController.createUser(mPseudo, mEmail);
                            onRegisterSuccess();
                        }
                    }
                });
        }else{
            Utils.displayErrorMessage(getApplicationContext(),SignUpActivity.this, mRootView,"Register failed");
        }
    }

    /**
     * Check and validate inputs when user is signing up
     * @return
     */
    public boolean validateInputs() {
        boolean valid = true;

        String reEnterPassword = mConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(mEmail) || !android.util.Patterns.EMAIL_ADDRESS.matcher(mEmail).matches()) {
            mInputEmail.setError("Enter a valid mEmail address");
            valid = false;
        } else {
            mInputEmail.setError(null);
        }

        if (TextUtils.isEmpty(mPseudo) || mPseudo.length() < 2) {
            mInputPseudo.setError("Must be at least 2 characters long");
            valid = false;
        } else {
            mInputPseudo.setError(null);
        }

        if (TextUtils.isEmpty(mPassword) || mPassword.length() < 6) {
            mInputPassword.setError("Must be at least 6 characters long");
            valid = false;
        } else {
            mInputPassword.setError(null);
        }

        if (TextUtils.isEmpty(reEnterPassword) || reEnterPassword.length() < 6 || !(reEnterPassword.equals(mPassword))) {
            mConfirmPassword.setError("Password do not match");
            valid = false;
        } else {
            mConfirmPassword.setError(null);
        }

        return valid;
    }


    /**
     * Method that sets the user as connected
     * and calls the main view
     */
    private void onRegisterSuccess() {
        mUserController = UserController.getsInstance();
        mUserController.setUserAsConnected();
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
