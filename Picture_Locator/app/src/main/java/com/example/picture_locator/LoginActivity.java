package com.example.picture_locator;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LoginActivity extends AppCompatActivity {
    private final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private EditText mEmailInput,mPasswordInput;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Initializing different widgets.
        mAuth = FirebaseAuth.getInstance();
        mEmailInput = findViewById(R.id.layout_sign_in_email);
        mPasswordInput = findViewById(R.id.layout_sign_in_password);
        progressBar = findViewById(R.id.login_progressbar);

    }

    public void onButtonClick(View v){
        switch (v.getId()){
            case R.id.button_sign_in:
                validUserAccount();
                return;
            case R.id.button_sign_up:
                startActivity(new Intent(this, RegisterActivity.class));
        }

    }

    //Helper function that allow user to login in via Firebase.
    private void validUserAccount(){
        if(validEmail() && validPassword()){
            progressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(mEmailInput.getText().toString(),mPasswordInput.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(View.GONE);
                            if(task.isSuccessful()){
                                //Direct to the home page once user login.
                                Intent home = new Intent(LoginActivity.this,MainActivity.class);
                                startActivity(home);
                                finish();
                            }else{
                                Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    //Helper function that checks if user enter a valid email.
    private boolean validEmail() {
        String emailInput = mEmailInput.getText().toString().trim();
        if (emailInput.isEmpty()) {
            mEmailInput.setError("Email can not be empty.");
            return false;
            //Use Util.Pattern library to check if the email is valid
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            mEmailInput.setError("Please enter the valid email.");
            return false;
        } else {
            mEmailInput.setError(null);
        }
        return true;
    }

    //Private helper function that check if user enter a valid password.
    private boolean validPassword() {
        String passwordInput = mPasswordInput.getText().toString().trim();
        //Check if user enter the password or not.
        if (passwordInput.isEmpty()) {
            mPasswordInput.setError("Password can not be empty.");
            return false;
            //Check if user enter a password with length greater than 3 or not.
        } else if (passwordInput.length() < 3) {
            mPasswordInput.setError("Minimum length for the password is 3.");
            return false;
        } else {
            mPasswordInput.setError(null);
        }
        return true;
    }
}

