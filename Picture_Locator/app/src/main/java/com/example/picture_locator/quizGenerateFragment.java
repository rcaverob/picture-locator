package com.example.picture_locator;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

public class quizGenerateFragment extends Fragment {
    private boolean isFabOpen = false;
    private FloatingActionButton fab,takeImage,locate;
    private Animation fab_open,fab_close;
    public quizGenerateFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.activity_generate_quiz, container, false);
        ImageView quizImage = v.findViewById(R.id.quiz_image_id);
        quizImage.setClipToOutline(true);
        setHasOptionsMenu(true);


        fab = v.findViewById(R.id.fab);
        takeImage = v.findViewById(R.id.fab1);
        locate = v.findViewById(R.id.fab2);

        fab_open = AnimationUtils.loadAnimation(getContext(),R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getContext(),R.anim.fab_close);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFab();
                Log.d("FAB","FAB CLICKED");
            }
        });
        return v;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.sync).setVisible(true);
        super.onPrepareOptionsMenu(menu);

    }


    private void animateFab(){
        if(isFabOpen){
            takeImage.startAnimation(fab_close);
            locate.startAnimation(fab_close);
            takeImage.setClickable(false);
            locate.setClickable(false);
            isFabOpen = false;
        }else{
            takeImage.startAnimation(fab_open);
            locate.startAnimation(fab_open);
            takeImage.setClickable(true);
            locate.setClickable(true);
            isFabOpen = true;
        }
    }
}
