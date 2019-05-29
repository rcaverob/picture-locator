package com.example.picture_locator.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.example.picture_locator.Adapters.BoardListViewAdapter;
import com.example.picture_locator.Models.User;
import com.example.picture_locator.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class LeaderBoardFragment extends Fragment {
    private List<User> users;
    private BoardListViewAdapter adapter;
    private ListView listView;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFireDatabase;

    public LeaderBoardFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        setHasOptionsMenu(true);
        users = new ArrayList<>();
        //Setting up the list view of the board fragment
        listView = v.findViewById(R.id.board_listView);
        adapter = new BoardListViewAdapter(getActivity(), users);
        listView.setAdapter(adapter);

        //Setting up the firebase variables.
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFireDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        return v;
    }

    //Helper function that fetch the top 25 users that have the highest scores from the firebase.
    private void loadRanks() {
        //Order the user ndoes by their highest score.
        mFireDatabase.orderByChild("Highest Score").limitToFirst(20).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                for(DataSnapshot obj:dataSnapshot.getChildren()){
                    String userName = obj.child("Username").getValue().toString();
                    String rank = Integer.toString(users.size()+1);
                    int scores = -obj.child("Highest Score").getValue(Integer.class);
                    String uri =obj.child("Profile Image").getValue().toString();
                    users.add(new User(userName,rank,scores,uri));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRanks();
    }
}
