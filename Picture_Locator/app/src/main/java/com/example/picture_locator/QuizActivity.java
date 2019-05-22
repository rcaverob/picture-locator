package com.example.picture_locator;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class QuizActivity extends AppCompatActivity {
    private static final String TAG = "QuizActivity";

    private static final int REQUEST_CODE_SCORE = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        ImageView quizImage = findViewById(R.id.image);
        quizImage.setClipToOutline(true);
    }

    public void answerQuiz(View view) {
        Intent map = new Intent(this,MapsActivity.class);
        startActivityForResult(map, REQUEST_CODE_SCORE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_SCORE && resultCode == RESULT_OK){
            if (data != null) {
                int score = data.getIntExtra(MapsActivity.EXTRA_MAP_SCORE, -1);
                Log.d(TAG, "onActivityResult: Score is: "+score);
            }
        }
    }
}
