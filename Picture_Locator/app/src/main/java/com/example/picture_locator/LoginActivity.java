package com.example.picture_locator;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class LoginActivity extends AppCompatActivity {
    private final String TAG = "LoginActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void onButtonClick(View v){
        switch (v.getId()){
            case R.id.button_sign_in:
                startActivity(new Intent(this, MainActivity.class));
                return;
            case R.id.button_sign_up:
                startActivity(new Intent(this, RegisterActivity.class));
        }

    }
}

