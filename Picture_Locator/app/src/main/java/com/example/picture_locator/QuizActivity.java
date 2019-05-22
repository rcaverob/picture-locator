package com.example.picture_locator;

import android.content.Intent;
import android.support.annotation.LongDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;


public class QuizActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private List<Quizbank> quizList ;
    private int childCount;
    private int randArr[];
    private int quizCounter;
    ViewPager viewpager;
    CustomSwipeAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewpager);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Quizbank");

        quizList = new ArrayList<>();
        randArr = new int[10];
        quizCounter = 0;
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
                childCount = (int) dataSnapshot.getChildrenCount();
//                    //Generating 10 unique randome number;
                    if(childCount>10){
                        String rn="";
                        Set<Integer> randNum = new HashSet<>();
                        Random random  = new Random();
                        while (randNum.size() <9){
                            randNum.add(random.nextInt(childCount));
                        }
                        int counter = 0;
                        for (Integer integer:randNum){
                            randArr[counter] = integer;
                            rn = rn+" "+integer;
                            counter++;
                        }
                        Log.d("FAB","Generated random numbers: "+rn);
                    }

                    mDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(childCount > 10){
                                int currentCounter = 0;
                                for(DataSnapshot sp:dataSnapshot.getChildren()){
                                    if(quizCounter > 10){
                                        break;
                                    }
                                    if(currentCounter == randArr[quizCounter]){
                                        quizList.add(sp.getValue(Quizbank.class));
                                        quizCounter++;
                                    }
                                    currentCounter++;
                                }

                            }
                            else{
                                for(DataSnapshot sp:dataSnapshot.getChildren()) {
                                    quizList.add(sp.getValue(Quizbank.class));
                                    quizCounter++;
                                }
                            }
                            mDatabase.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("FAB","Finish loading: "+quizList.size());
                        String resultedQuiz = "";
                        for (int i = 0; i<quizList.size();i++){
                            resultedQuiz = resultedQuiz + " " +quizList.get(i).getUserName();
                        }
                        Log.d("FAB",resultedQuiz);
                        viewpager = findViewById(R.id.view_pager);
                        adapter = new CustomSwipeAdapter(getApplicationContext(),quizList);
                        viewpager.setAdapter(adapter);
                        viewpager.setOffscreenPageLimit(5);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
