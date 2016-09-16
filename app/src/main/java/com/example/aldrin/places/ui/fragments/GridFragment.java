package com.example.aldrin.places.ui.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.aldrin.places.R;
import com.example.aldrin.places.helpers.UserManager;
import com.example.aldrin.places.models.nearby.GetFromJson;
import com.example.aldrin.places.models.nearby.Result;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class GridFragment extends Fragment {

    public static final String TAG = ListFragment.class.getSimpleName();
    private Context mContext;
    private UserManager mUserManager;
    private GetFromJson mJsonResponse;
    private LatLng mPosition;
    private List<Result> results;

    public GridFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_grid, container, false);
    }

}
