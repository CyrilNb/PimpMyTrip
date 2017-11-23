package fr.univtln.cniobechoudayer.pimpmytrip.Authentication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import fr.univtln.cniobechoudayer.pimpmytrip.R;

/**
 * SignUp Activity using Firebase auth
 * Created by Cyril Niobé on 22/11/2017.
 */

public class SignUpActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword, confirmPassword;
    private Button btnSignIn, btnSignUp, btnResetPassword, btnSignUpWithGoogle;

    private ProgressBar progressBar;
    private FirebaseAuth auth;

    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnSignIn = (Button) findViewById(R.id.sign_in_buttonFromSignupActiv);
        btnSignUp = (Button) findViewById(R.id.sign_up_button);
        btnSignUpWithGoogle = (Button) findViewById(R.id.sign_up_with_google_button);
        inputEmail = (EditText) findViewById(R.id.editTxtEmail);
        inputPassword = (EditText) findViewById(R.id.editTxtPassword);
        confirmPassword = (EditText) findViewById(R.id.editTxtConfirmPassword);
        progressBar = (ProgressBar) findViewById(R.id.progressBarSignUp);
        btnResetPassword = (Button) findViewById(R.id.btn_reset_password);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpWithEmail();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        //Return to the Login activity
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    private void signUpWithEmail(){
        email = inputEmail.getText().toString().trim();
        password = inputPassword.getText().toString().trim();

        if(validateInputs()){
        progressBar.setVisibility(View.VISIBLE);

        //create user
        auth.createUserWithEmailAndPassword(email, password).
                addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Toast.makeText(SignUpActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "Register failed." + task.getException(),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            startActivity(new Intent(SignUpActivity.this, AccountSettingsActivity.class));
                            finish();
                        }
                    }
                });
        }else{
            Toast.makeText(getBaseContext(), "Register failed", Toast.LENGTH_LONG).show();
        }
    }

    public boolean validateInputs() {
        boolean valid = true;

        String reEnterPassword = confirmPassword.getText().toString();

        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputEmail.setError("Enter a valid email address");
            valid = false;
        } else {
            inputEmail.setError(null);
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            inputPassword.setError("Must be at least 6 characters long");
            valid = false;
        } else {
            inputPassword.setError(null);
        }

        if (TextUtils.isEmpty(reEnterPassword) || reEnterPassword.length() < 6 || !(reEnterPassword.equals(password))) {
            confirmPassword.setError("Password do not match");
            valid = false;
        } else {
            confirmPassword.setError(null);
        }

        return valid;
    }
}
