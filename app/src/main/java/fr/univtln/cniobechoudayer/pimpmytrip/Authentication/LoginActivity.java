package fr.univtln.cniobechoudayer.pimpmytrip.Authentication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import fr.univtln.cniobechoudayer.pimpmytrip.Activities.MainActivity;
import fr.univtln.cniobechoudayer.pimpmytrip.R;

/**
 * Login Activity
 * Created by Cyril Niobé on 23/11/2017.
 */

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    EditText emailEditText, passwordEditText;
    Button loginButton, signupButton, btnSignInWithGoogle, btnForgotPassword;
    String email;
    String password;

    private FirebaseAuth auth;
    ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressBar = (ProgressBar) findViewById(R.id.progressBarLogin);
        emailEditText = (EditText) findViewById(R.id.editTxtEmailLogin);
        passwordEditText = (EditText) findViewById(R.id.editTxtPasswordLogin);
        loginButton = (Button) findViewById(R.id.sign_in_button);
        signupButton = (Button) findViewById(R.id.sign_up_button_from_login);
        btnSignInWithGoogle = (Button) findViewById(R.id.sign_in_with_google_button);
        btnForgotPassword = (Button) findViewById(R.id.btn_reset_password);

        auth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
            }
        });

        btnForgotPassword.setOnClickListener(new View.OnClickListener() {
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

    private boolean validateInputs() {
        boolean valid = true;

        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            emailEditText.setError("Enter a valid email address");
            valid = false;
        } else
        {
            emailEditText.setError(null);
        }
        return valid;
    }

    private void login(){
        Log.d(TAG, "Login");
        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();

        if(validateInputs()) {
            progressBar.setVisibility(View.VISIBLE);

            //authenticate user
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            progressBar.setVisibility(View.GONE);
                            if (!task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                            }else {

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });

        }else{
            Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }
}
