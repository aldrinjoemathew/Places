package com.example.aldrin.places.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.aldrin.places.R;
import com.example.aldrin.places.adapters.FavoritePlacesAdapter;
import com.example.aldrin.places.helpers.UserManager;
import com.example.aldrin.places.models.placesdetails.Result;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Displays the favorite places of the user.
 */
public class FavouritePlacesFragment extends Fragment {

    public static final String TAG = FavouritePlacesFragment.class.getSimpleName();
    private static final String SD_PATH = Environment.getExternalStorageDirectory().getPath();
    private List<Result> cardVenueList = new ArrayList<Result>();
    private UserManager mUserManger;
    private String mUserEmail;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @BindView(R.id.recycler_favorite)
    RecyclerView mRecyclerView;

    /**
     * Creates a new instance of the FavouritePlacesFragment.
     * @return
     */
    public static FavouritePlacesFragment newInstance() {
        return new FavouritePlacesFragment();
    }

    /**
     * Default constructor.
     */
    public FavouritePlacesFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favourite_places, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mUserManger = new UserManager(getContext());
        mUserEmail = mUserManger.getUserEmail();
        getFavoritesFromSdCard();
        displayFavorites();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("info", " " + requestCode + " " + resultCode + " ");
        if (requestCode == 1) {
            if(resultCode == 1){
                Boolean changed = data.getBooleanExtra("valueChanged", false);
                if (changed) {
                    mRecyclerView.removeAllViews();
                    FavoritePlacesAdapter fav = (FavoritePlacesAdapter) mRecyclerView.getAdapter();
                    fav.clearData();
                    getFavoritesFromSdCard();
                    displayFavorites();
                }
            }
        }
    }

    /**
     * Display the favorite places in a card view using RecyclerView.
     */
    private void displayFavorites() {
        if (mUserManger.getFavorite(mUserEmail) != null) {
            mRecyclerView.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(getContext());
            mAdapter = new FavoritePlacesAdapter(getContext(), cardVenueList, this);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    /**
     * Used to retrieve favorite place details stored in SD card.
     */
    private void getFavoritesFromSdCard() {
        Gson gson = new Gson();
        String favorites = mUserManger.getFavorite(mUserEmail);
        File favDir = new File(SD_PATH, "Favorites");
        favDir.mkdirs();
        if (favorites != null) {
            String[] favs = favorites.split(",");
            for (int i=0; i<favs.length; i++) {
                if (favs[i]!=null) {
                    try {
                        File myFile = new File(favDir, favs[i]);
                        FileInputStream fIn = new FileInputStream(myFile);
                        BufferedReader myReader = new BufferedReader(
                                new InputStreamReader(fIn));
                        String aDataRow = "";
                        String placeDetails = "";
                        while ((aDataRow = myReader.readLine()) != null) {
                            placeDetails += aDataRow + "\n";
                        }
                        com.example.aldrin.places.models.placesdetails.Result venue =
                                gson.fromJson(placeDetails, Result.class);
                        cardVenueList.add(venue);
                        myReader.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
    }
}
