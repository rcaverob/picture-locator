package com.example.picture_locator;

import android.content.Intent;
import android.support.annotation.LongDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.picture_locator.Models.Quizbank;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class QuizActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private List<Quizbank> quizList ;
    private ImageView quizImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Quizbank");
        quizImage = findViewById(R.id.image);
        quizImage.setClipToOutline(true);
        quizList = new ArrayList<>();
        loadQuizFromFb();

    }

    public void answerQuiz(View view) {
        Intent map = new Intent(this,MapsActivity.class);
        startActivity(map);
    }

    private void loadQuizFromFb(){

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("FAB","Finish loading: "+quizList.size());
                Picasso.with(QuizActivity.this).load(quizList.get(0).getImageUrl()).into(quizImage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("FAB","addChild called");
                Quizbank quizItem = dataSnapshot.getValue(Quizbank.class);
                quizList.add(quizItem);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
