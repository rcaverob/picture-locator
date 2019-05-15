package com.example.picture_locator.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.picture_locator.Models.QuizItem;
import com.example.picture_locator.R;

public class LocationListAdapter extends ArrayAdapter<QuizItem> {
    private Context mContext;

    // Adapter to be used for the ListView in LocationList Fragment
    public LocationListAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        this.mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Inflate custom item layout
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.location_list_item, parent, false);
        }

        // Find references to Views
        TextView addressView = view.findViewById(R.id.loc_list_item_address);
        ImageView imageView = view.findViewById(R.id.loc_list_item_img);

        // Set values
        QuizItem item = getItem(position);
        if (item != null) {
            addressView.setText(item.getAddress());
            imageView.setImageDrawable(mContext.getDrawable(R.drawable.photo_square));
        }
        return view;
    }
}
