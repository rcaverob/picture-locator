package com.example.picture_locator;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class quizGenerateFragment extends Fragment {

    public quizGenerateFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.activity_generate_quiz, container, false);
        ImageView quizImage = v.findViewById(R.id.quiz_image_id);
        quizImage.setClipToOutline(true);
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.sync).setVisible(true);
        super.onPrepareOptionsMenu(menu);

    }
}
