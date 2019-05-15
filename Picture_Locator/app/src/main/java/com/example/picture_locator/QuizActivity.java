package com.example.picture_locator;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class QuizActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        ImageView quizImage = findViewById(R.id.image);
        quizImage.setClipToOutline(true);
    }

    public void answerQuiz(View view) {
        Intent map = new Intent(this,MapsActivity.class);
        startActivity(map);
    }
}
