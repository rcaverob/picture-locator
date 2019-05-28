package com.example.picture_locator.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.picture_locator.Adapters.LocationListAdapter;
import com.example.picture_locator.Models.QuizItem;
import com.example.picture_locator.Models.Quizbank;
import com.example.picture_locator.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class LocationListFragment extends ListFragment {
    private final String TAG = "LocationListFragment";
    private LocationListAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_location_list, container, false);
        Log.d(TAG, "onCreateView: ");
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: ");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: ");
        mAdapter= new LocationListAdapter(Objects.requireNonNull(getContext()), R.layout.location_list_item);
        setListAdapter(mAdapter);

        // Get Firebase Database Reference to User's personal archive
        DatabaseReference userArchiveRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance()
                        .getCurrentUser()).getUid()).child(getString(R.string.archive_title));

        // Get all Quizzes from the Archive
        userArchiveRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: child count: "+dataSnapshot.getChildrenCount());
                for (DataSnapshot child : dataSnapshot.getChildren()){
                    Quizbank quiz = child.getValue(Quizbank.class);
                    mAdapter.add(quiz);
                    Log.d(TAG, "onDataChange: Found Quiz: "+quiz.getUserName());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        mAdapter.notifyDataSetChanged();
    }

}