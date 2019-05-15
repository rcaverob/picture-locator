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
import android.widget.Button;

public class startQuizFragment extends Fragment {
    public startQuizFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.activity_start_quiz, container, false);
        Button dartBtn = v.findViewById(R.id.dartmouthBtn);
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
