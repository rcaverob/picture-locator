package com.example.picture_locator.Adapters;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.picture_locator.Models.User;
import com.example.picture_locator.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BoardListViewAdapter extends ArrayAdapter {
    //Reference to the Activity
    private final Activity context;

    private final List<User> users;
    //Constructor of the BoardListViewAdapter
    public BoardListViewAdapter(@NonNull Activity context, List<User> users) {
        super(context, R.layout.leaderboard_list_item,users);
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = context.getLayoutInflater().inflate(R.layout.leaderboard_list_item,null,true);

        //Setting the values to different widgets in each row of the list view.
        TextView userNameInput = v.findViewById(R.id.board_username);
        TextView scoreInput = v.findViewById(R.id.board_score);
        TextView rankInput = v.findViewById(R.id.board_rank);
        ImageView profileImg = v.findViewById(R.id.board_profile_img);
        profileImg.setClipToOutline(true);
        String userName = users.get(position).getUserName();
        String score = "Highest Scores: "+users.get(position).getScores();
        String rank = "Rank: "+users.get(position).getRank();
        String profileImgUri = users.get(position).getProfileImgUri();
        userNameInput.setText(userName);
        scoreInput.setText(score);
        rankInput.setText(rank);
        if(!profileImgUri.equals("Default")){
            Picasso.with(this.context).load(profileImgUri).fit().into(profileImg);
        }
        return v;
    }
}
