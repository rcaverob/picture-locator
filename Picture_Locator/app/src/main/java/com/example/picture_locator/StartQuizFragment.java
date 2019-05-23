package com.example.picture_locator;

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

public class StartQuizFragment extends Fragment {

    public StartQuizFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.activity_start_quiz, container, false);
        ImageButton dartBtn = v.findViewById(R.id.dartQuizBtn);
        dartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAGP","clicked");
                Intent dartmouthqQuiz = new Intent(getActivity(),QuizActivity.class);
                startActivity(dartmouthqQuiz);
            }
        });



        return v;
    }


}
