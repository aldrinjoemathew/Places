package com.example.aldrin.places.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.aldrin.places.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavouritePlacesFragment extends Fragment {
    public static final String TAG = FavouritePlacesFragment.class.getSimpleName();
    public static FavouritePlacesFragment newInstance() {
        return new FavouritePlacesFragment();
    }
    public FavouritePlacesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favourite_places, container, false);
    }

}
