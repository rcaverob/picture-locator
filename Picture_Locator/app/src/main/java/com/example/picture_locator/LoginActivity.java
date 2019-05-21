package com.example.picture_locator;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LoginActivity extends AppCompatActivity {
    private final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private EditText mEmailInput,mPasswordInput;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        mEmailInput = findViewById(R.id.layout_sign_in_email);
        mPasswordInput = findViewById(R.id.layout_sign_in_password);

    }

    public void onButtonClick(View v){
        switch (v.getId()){
            case R.id.button_sign_in:
                validUserAccount();
                //startActivity(new Intent(this, MainActivity.class));
                return;
            case R.id.button_sign_up:
                startActivity(new Intent(this, RegisterActivity.class));
        }

    }

    private void validUserAccount(){
        mAuth.signInWithEmailAndPassword(mEmailInput.getText().toString(),mPasswordInput.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
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

