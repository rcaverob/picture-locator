package com.example.picture_locator.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.picture_locator.QuizActivity;
import com.example.picture_locator.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class StartQuizFragment extends Fragment {
    private ImageView profileImg;
    private FirebaseAuth mAuth;

    private DatabaseReference mDatabaseUsers;
    private TextView scores;

    public StartQuizFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.activity_start_quiz, container, false);
        //Initializing different widgets.
        ImageButton dartBtn = v.findViewById(R.id.dartQuizBtn);
        profileImg = v.findViewById(R.id.quiz_user_image);
        scores = v.findViewById(R.id.quiz_score);
        mAuth = FirebaseAuth.getInstance();
        //Click listener that starts the quiz activity.
        dartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dartmouthqQuiz = new Intent(getActivity(), QuizActivity.class);
                startActivity(dartmouthqQuiz);
            }
        });


        return v;
    }

    //Helper function that load the user information from firebase.
    private void loadUserInfo(){
        //Check if current user is null or not
        if(mAuth.getCurrentUser()!=null){
            mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            //Add a listener to the node. Update the highest scores displayed on the UI thread once the value is updated in the firebase.
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    scores.setText(Integer.toString(-dataSnapshot.child("Highest Score").getValue(Integer.class)));
                    String profileImgUri = dataSnapshot.child("Profile Image").getValue(String.class);
                    if(!profileImgUri.equals("Default")){
                        Picasso.with(getContext()).load(profileImgUri).fit().into(profileImg);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserInfo();
    }

}
