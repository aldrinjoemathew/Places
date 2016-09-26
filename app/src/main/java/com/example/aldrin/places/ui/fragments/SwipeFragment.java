package com.example.aldrin.places.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.aldrin.places.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by aldrin on 26/8/16.
 */

/**
 * Class to implement the swipe view when mutiple images of restaurant are available.
 */
public class SwipeFragment extends Fragment{

    public ImageView imgRestaurant;
    private List<String> mImageRefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.image_view_fullscreen, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imgRestaurant = (ImageView) view.findViewById(R.id.iv_fullscreen);
        Bundle bundle = getArguments();
        int position = bundle.getInt("position");
        mImageRefs = (List<String>) bundle.getSerializable("imagerefs");
        String imageReference = mImageRefs.get(position);
        String imageUrl = String.format(getString(R.string.image_url), imageReference);
        loadBitmap(imageUrl);
    }


    /**
     * Method retrieve bitmap from cache if present.
     * Otherwise executes a background task inorder to load the image from a remote URL.
     * @param url
     */
    public void loadBitmap(String url) {
        Picasso.with(getContext())
                .load(url)
                .into(imgRestaurant);
    }
}