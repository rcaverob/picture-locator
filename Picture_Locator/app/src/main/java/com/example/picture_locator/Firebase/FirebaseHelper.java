package com.example.picture_locator.Firebase;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class FirebaseHelper {
    private Activity context;

    public FirebaseHelper(Activity context) {
        this.context = context;
    }


}

