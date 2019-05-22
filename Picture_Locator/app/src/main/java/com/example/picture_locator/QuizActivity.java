package com.example.picture_locator;

import android.content.Intent;


import android.support.annotation.Nullable;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.picture_locator.Models.LatLng;
import com.example.picture_locator.Models.Quizbank;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;


public class QuizActivity extends AppCompatActivity {
    private static final String TAG = "QuizActivity";
    private static final int REQUEST_CODE_SCORE = 0;


    private DatabaseReference mDatabase;
    private List<Quizbank> quizList ;
    private int childCount;
    private int randArr[];
    private int quizCounter;
    ViewPager viewpager;
    CustomSwipeAdapter adapter;

    


    // For Scoring
    private int mTotal_score = 0;
    private TextView mScoreText;
    private Set<Integer> mAnsweredItems;

    private MenuItem mFinishButton;
    private Button mAnswerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewpager);

        viewpager = findViewById(R.id.view_pager);
        mAnswerButton = findViewById(R.id.quiz_answer_id);

        // Display the back button on the App bar
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAnsweredItems = new HashSet<>();

        mScoreText = findViewById(R.id.viewpager_score);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Quizbank");

        quizList = new ArrayList<>();
        randArr = new int[10];
        quizCounter = 0;

        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
//                Log.d(TAG, "onPageScrolled: "+i);
            }

            @Override
            public void onPageSelected(int i) {
                Log.d(TAG, "onPageSelected: "+i);
                // Disable the Answer Button on Already Answered Pictures
                if (mAnsweredItems.contains(i)){
                    mAnswerButton.setEnabled(false);
                } else {
                    mAnswerButton.setEnabled(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
        loadQuizFromFb();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.quiz, menu);

        mFinishButton = menu.findItem(R.id.menu_quiz_finish);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_quiz_finish:
                finishQuiz();
                return true;
            default:
                return  true;
        }
    }

    private void finishQuiz() {
        Toast.makeText(this, "Finished Quiz! Final Score is: "+mTotal_score,
                Toast.LENGTH_LONG).show();
        finish();
    }

    public void answerQuiz(View view) {
        Intent mapIntent = new Intent(this,MapsActivity.class);


        double[] latLngArr = getLocationFromCurrentPicture();
        if (latLngArr != null){
            mapIntent.putExtra(getString(R.string.key_latitude), latLngArr[0]);
            mapIntent.putExtra(getString(R.string.key_longitude), latLngArr[1]);
            startActivityForResult(mapIntent, REQUEST_CODE_SCORE);
        }
    }

    private double[] getLocationFromCurrentPicture() {
        int curr = viewpager.getCurrentItem();
        Log.d(TAG, "current item is : "+curr);

        Quizbank currentQuiz = quizList.get(curr);
        if (currentQuiz != null){
            LatLng location = currentQuiz.getLocationCoord();
            double lat = location.getLatitude();
            double longit = location.getLongitude();
            return new double[] {lat, longit};
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: Called");
        if (requestCode == REQUEST_CODE_SCORE && resultCode == RESULT_OK){
            if (data != null) {
                int score = data.getIntExtra(MapsActivity.EXTRA_MAP_SCORE, -1);
                mTotal_score += score;
                mScoreText.setText(MessageFormat.format("Score : {0}", mTotal_score));
                Log.d(TAG, "onActivityResult: Score is: "+score);
                mAnsweredItems.add(viewpager.getCurrentItem());
                mAnswerButton.setEnabled(false);
            }
        }
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
                        while (randNum.size() <10){
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
                                    if(quizCounter >= 10){
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
