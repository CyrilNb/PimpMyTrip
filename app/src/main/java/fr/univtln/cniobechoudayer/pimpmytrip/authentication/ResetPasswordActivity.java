package fr.univtln.cniobechoudayer.pimpmytrip.authentication;

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
import com.google.firebase.auth.FirebaseAuth;

import fr.univtln.cniobechoudayer.pimpmytrip.R;

/**
 * Reset account password activity
 * Created by Cyril Niobé on 23/11/2017.
 */

public class ResetPasswordActivity extends AppCompatActivity{

    private EditText mInputEmail;
    private Button mBtnReset, mBtnBack;
    private FirebaseAuth mAuth;
    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mInputEmail = (EditText) findViewById(R.id.email);
        mBtnReset = (Button) findViewById(R.id.btn_reset_password);
        mBtnBack = (Button) findViewById(R.id.btn_back);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        /**
         * Setting up click listeners
         */

        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToLoginActivity();
            }
        });
        mBtnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mInputEmail.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplication(), "Enter your registered email", Toast.LENGTH_SHORT).show();
                    return;
                }

                mProgressBar.setVisibility(View.VISIBLE);
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ResetPasswordActivity.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ResetPasswordActivity.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                }

                                mProgressBar.setVisibility(View.GONE);
                            }
                        });
            }
        });
    }

    /**
     * Handles hardware back button when pressed
     */
    @Override
    public void onBackPressed() {
        returnToLoginActivity();
    }


    /**
     * Method that allows to go back to login activity
     */
    private void returnToLoginActivity(){
        //Return to the Login activity
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }
}
