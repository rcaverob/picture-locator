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
import com.google.firebase.auth.FirebaseAuth;
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


    private DatabaseReference mQuizBankReference;
    FirebaseAuth  mAuth;

    private List<Quizbank> quizList ;
    private int childCount;
    private int randArr[];
    private int quizCounter;
    ViewPager viewpager;
    CustomSwipeAdapter adapter;
    String mUsername = "";


    // For Scoring
    private int mTotal_score = 0;
    private TextView mScoreText;
    private Set<Integer> mAnsweredItems;

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

        //For Firebase
        mAuth = FirebaseAuth.getInstance();
        mUsername = Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName();

        mAnsweredItems = new HashSet<>();

        mScoreText = findViewById(R.id.viewpager_score);
        mQuizBankReference = FirebaseDatabase.getInstance().getReference().child("Quizbank");

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
                // Disable the quiz_answer Button on Already Answered Pictures
                if (mAnsweredItems.contains(i)){
                    mAnswerButton.setEnabled(false);
                   // mAnswerButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.buttonColorGrey)));

                } else {
                    mAnswerButton.setEnabled(true);
                    //mAnswerButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.buttonColor)));
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
        updateHighestScore();
        finish();

    }

    private void  updateHighestScore(){
        final DatabaseReference mDatabaseUsers  = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        mDatabaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int currentHighestScore = dataSnapshot.child("Highest Score").getValue(Integer.class);
                if (currentHighestScore>-mTotal_score){
                    mDatabaseUsers.child("Highest Score").setValue(-mTotal_score);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void answerQuiz(View view) {
        if (viewpager.getChildCount() > 0) {
            Intent mapIntent = new Intent(this, MapsActivity.class);

            int curr = viewpager.getCurrentItem();
            Quizbank currentQuiz = quizList.get(curr);

            double[] latLngArr = getLocationFromCurrentPicture(currentQuiz);
            mapIntent.putExtra(getString(R.string.key_latitude), latLngArr[0]);
            mapIntent.putExtra(getString(R.string.key_longitude), latLngArr[1]);
            mapIntent.putExtra(getString(R.string.key_guess_users), currentQuiz.getUsernamesAnswered());
            mapIntent.putExtra(getString(R.string.key_guess_coords), currentQuiz.getLocationsAnswered());

            startActivityForResult(mapIntent, REQUEST_CODE_SCORE);
        }
    }

    // Archives the current quiz into the current User's personal archive
    public void archiveQuizItem(View view) {
        // Get Firebase Database References
        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
        DatabaseReference userArchiveRef = userRef.child(getString(R.string.archive_title));
        final DatabaseReference archivedQuiz = userArchiveRef.push();

        // Get current Quiz
        int curr = viewpager.getCurrentItem();
        final Quizbank currentQuiz = quizList.get(curr);

        if (currentQuiz == null){
            return;
        }

        // Archive the quiz only if it hasn't been archived before

        userRef.child("AlreadyArchived").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Set<Integer> archivedIDs = new HashSet<>();
                for (DataSnapshot child : dataSnapshot.getChildren()){
                    String idString = child.getKey();
                    int idNum = Integer.parseInt(Objects.requireNonNull(idString));
                    archivedIDs.add(idNum);
                }

                boolean isAlreadyArchived = archivedIDs.contains(currentQuiz.getImageUrl().hashCode());

                // If it hasn't been archived before, save it to user's personal archive
                if (!isAlreadyArchived){
                    archivedQuiz.setValue(currentQuiz, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                System.out.println("Error Archiving Quiz " + databaseError.getMessage());
                            } else {
                                userRef.child("AlreadyArchived").child(""+currentQuiz.getImageUrl().hashCode()).setValue(true);
                                Toast.makeText(QuizActivity.this, "Successfully Archived", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    // If has been archived before, display error message.
                    Toast.makeText(QuizActivity.this, "Quiz has already been archived before",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Returns an array containing the latitude and longitude of the given Quizbank
    private double[] getLocationFromCurrentPicture(Quizbank currentQuiz) {
        if (currentQuiz != null){
            LatLng location = currentQuiz.getLocationCoord();
            double lat = location.getLatitude();
            double longit = location.getLongitude();
            return new double[] {lat, longit};
        }
        return null;
    }

    // Receives information from MapActivity once the user has made a guess
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: Called");
        if (requestCode == REQUEST_CODE_SCORE && resultCode == RESULT_OK){
            if (data != null) {
                int curr_num = viewpager.getCurrentItem();

                // Update Score
                int score = data.getIntExtra(MapsActivity.EXTRA_SCORE, -1);
                Log.d(TAG, "onActivityResult: Received Score :"+score);

                mTotal_score += score;
                mScoreText.setText(MessageFormat.format("Score : {0}", mTotal_score));
                Log.d(TAG, "onActivityResult: Score is: "+score);
                mAnsweredItems.add(curr_num);
                mAnswerButton.setEnabled(false);

                // Add guess location to QuizBank
                double[] guess = data.getDoubleArrayExtra(MapsActivity.EXTRA_GUESS_LOCATION);
                Quizbank quiz = quizList.get(curr_num);
                quiz.addGuess(mUsername, new com.google.android.gms.maps.model.LatLng(guess[0], guess[1]));
                mQuizBankReference.child(""+quiz.getImageUrl().hashCode()).setValue(quiz);

            }
        }
    }

    // Loads 10 of the Quiz items from the firebase database and adds them to the Swipe Adapter
    private void loadQuizFromFb(){

        mQuizBankReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                childCount = (int) dataSnapshot.getChildrenCount();
//                    //Generating 10 unique random numbers;
                    if(childCount>10){
                        StringBuilder rn= new StringBuilder();
                        Set<Integer> randNum = new HashSet<>();
                        Random random  = new Random();
                        while (randNum.size() <10){
                            randNum.add(random.nextInt(childCount));
                        }
                        int counter = 0;
                        for (Integer integer:randNum){
                            randArr[counter] = integer;
                            rn.append(" ").append(integer);
                            counter++;
                        }
                        Log.d("FAB","Generated random numbers: "+rn);
                    }

                    mQuizBankReference.addValueEventListener(new ValueEventListener() {
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
                            mQuizBankReference.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                mQuizBankReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("FAB","Finish loading: "+quizList.size());
                        StringBuilder resultedQuiz = new StringBuilder();
                        for (int i = 0; i<quizList.size();i++){
                            resultedQuiz.append(" ").append(quizList.get(i).getUserName());
                        }
                        Log.d("FAB", resultedQuiz.toString());
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
