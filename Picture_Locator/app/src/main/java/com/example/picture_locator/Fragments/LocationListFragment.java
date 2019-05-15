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
import com.example.picture_locator.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class LocationListFragment extends ListFragment {
    private final String TAG = "LocationListFragment";
    private LocationListAdapter mAdapter;

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

        List<QuizItem> items = new ArrayList<>();
        for (int i = 0; i < 10; i ++){
            items.add(new QuizItem("Address"+i));
        }
        mAdapter.addAll(items);

    }

}