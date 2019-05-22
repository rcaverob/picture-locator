package com.example.picture_locator;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    EditText  mEmailInput, mPasswordInput, mPhoneInput,mUserNameInput;
    Button registerBtn;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mEmailInput = findViewById(R.id.layout_register_email);
        mPasswordInput = findViewById(R.id.layout_register_password);
        mPhoneInput = findViewById(R.id.layout_register_phone);
        mUserNameInput = findViewById(R.id.layout_register_userName);
        registerBtn = findViewById(R.id.button_register);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    public void onButtonClick(View v){
        registerUser();
    }

    public void registerUser(){
        final String username = mUserNameInput.getText().toString();
        final String email = mEmailInput.getText().toString();
        final String password = mPasswordInput.getText().toString();
        final String phone = mPhoneInput.getText().toString();

        if(validName()&&validEmail()&&validPassword()&&validPhone()){
            mAuth.createUserWithEmailAndPassword(mEmailInput.getText().toString(), mPasswordInput.getText().toString())
                    .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, "Account registered successfully",
                                        Toast.LENGTH_LONG).show();
                                String userId = mAuth.getCurrentUser().getUid();
                                DatabaseReference userDB = mDatabase.child(userId);
                                userDB.child("Username").setValue(username);
                                userDB.child("Email").setValue(email);
                                userDB.child("Password").setValue(password);
                                userDB.child("Phone").setValue(phone);

                            } else {
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    Toast.makeText(getApplicationContext(), "Account already exisit", Toast.LENGTH_LONG).show();
                                }
                            }
                            finish();
                        }
                    });
        }
    }




    //Private helper function that check if user enter a valid name.
    private boolean validName() {
        String name = mUserNameInput.getText().toString().trim();
        //Throw error if user didnt enter the name.
        if (name.isEmpty()) {
            mUserNameInput.setError("Name field can't be empty.");
            return false;
        } else {
            mUserNameInput.setError(null);
        }
        return true;
    }

    //Private helper function that check if user enter a valid Email.
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

    //Private helper function that check if user enter a valid phone number.
    private boolean validPhone() {
        String phone = mPhoneInput.getText().toString().trim();
        if (phone.isEmpty()) {
            mPhoneInput.setError("Phone can't be empty");
        } else if (phone.length() > 11 || phone.length() < 10) {
            mPhoneInput.setError("Not valid phone number.");
        } else {
            mPhoneInput.setError(null);
        }

        return true;
    }
}
