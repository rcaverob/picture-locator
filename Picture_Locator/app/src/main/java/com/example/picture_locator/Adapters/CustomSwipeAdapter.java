package com.example.picture_locator.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.picture_locator.Models.Quizbank;
import com.example.picture_locator.R;
import com.squareup.picasso.Picasso;

import java.util.List;


public class CustomSwipeAdapter extends PagerAdapter {

    private List<Quizbank> quizList ;
    private Context ctx;
    private LayoutInflater layoutInflater;

    //Constructor of the adapter
    public CustomSwipeAdapter(Context ctx,List<Quizbank> quizList ) {
        this.ctx = ctx;
        this.quizList = quizList;
    }

    @Override
    public int getCount() {
        return quizList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == (ConstraintLayout)o;
    }

    //Initialzing item for each page in the view pager.
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View item_view = layoutInflater.inflate(R.layout.activity_quiz, container,false);
        ImageView imageView = item_view.findViewById(R.id.image);
        imageView.setClipToOutline(true);
        Picasso.with(this.ctx).load(quizList.get(position).getImageUrl()).fit().into(imageView);
        container.addView(item_view);
        return item_view;
    }

    //Destory the current view after user swipe to the next page.
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ConstraintLayout)object);
    }



}
