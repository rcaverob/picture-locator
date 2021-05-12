package com.example.picture_locator.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.picture_locator.Models.Quizbank;
import com.example.picture_locator.R;
import com.squareup.picasso.Picasso;

public class LocationListAdapter extends ArrayAdapter<Quizbank> {
    private Context mContext;
    private final String TAG = "LocationListAdapter";

    // Adapter to be used for the ListView in LocationList Fragment
    public LocationListAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        this.mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Log.d(TAG, "getView: ");
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
        Quizbank item = getItem(position);
        if (item != null) {
            addressView.setText(item.getAddressName());
            Log.d(TAG, "item Address is: "+item.getAddressName());

            Picasso.with(mContext).load(item.getImageUrl()).fit().into(imageView);
        }
        return view;
    }
}
