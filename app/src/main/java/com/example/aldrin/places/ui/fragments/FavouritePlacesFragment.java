package com.example.aldrin.places.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.example.aldrin.places.R;
import com.example.aldrin.places.adapters.FavoritePlacesAdapter;
import com.example.aldrin.places.helpers.RecyclerClickListener;
import com.example.aldrin.places.helpers.StorageOnSdCard;
import com.example.aldrin.places.helpers.UserManager;
import com.example.aldrin.places.models.placesdetails.Result;
import com.example.aldrin.places.ui.activities.PlacesDetailsActivity;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Displays the favorite places of the user.
 */
public class FavouritePlacesFragment extends Fragment {

    public static final String TAG = FavouritePlacesFragment.class.getSimpleName();
    private static final String SD_PATH = Environment.getExternalStorageDirectory().getPath();
    private static final int NAVIGATE_UP_FROM_CHILD = 2;
    private List<Result> cardVenueList = new ArrayList<Result>();
    private List<String> mFavList = new ArrayList<String>();
    private UserManager mUserManager;
    private String mUserEmail;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private StorageOnSdCard mSdCard;
    private Boolean mLongClickEnabled = false;

    @BindView(R.id.recycler_favorite)
    RecyclerView mRecyclerView;
    @BindView(R.id.fab_delete_favs)
    FloatingActionButton fabDeleteFavs;
    @BindColor(R.color.cardHighlightColor)
    int mCardHighlightColor;
    @BindColor(R.color.cardNormalColor)
    int mCardNormalColor;

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
        mUserManager = new UserManager(getContext());
        mUserEmail = mUserManager.getUserEmail();
        getFavoritesFromSdCard();
        displayFavorites();
        mRecyclerView.addOnItemTouchListener(onFavoriteClickListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NAVIGATE_UP_FROM_CHILD) {
            if(resultCode == Activity.RESULT_OK){
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

    RecyclerView.OnItemTouchListener onFavoriteClickListener = new RecyclerClickListener(getContext(), new RecyclerClickListener.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            if (mLongClickEnabled) {
                if (mFavList.contains(cardVenueList.get(position).getPlace_id())) {
                    view.getBackground().clearColorFilter();
                    mFavList.remove(cardVenueList.get(position).getPlace_id());
                } else {
                    view.getBackground().setColorFilter(mCardHighlightColor, PorterDuff.Mode.MULTIPLY );
                    mFavList.add(cardVenueList.get(position).getPlace_id());
                }
                if (mFavList.size() == 0) {
                    mLongClickEnabled = false;
                    fabDeleteFavs.setVisibility(View.INVISIBLE);
                }
            } else {
                Result venue = cardVenueList.get(position);
                String placeId = venue.getPlace_id();
                Intent placesDetailsIntent = new Intent(getContext(), PlacesDetailsActivity.class);
                placesDetailsIntent.putExtra("place_id", placeId);
                getActivity().startActivityForResult(placesDetailsIntent, NAVIGATE_UP_FROM_CHILD);
            }
        }

        @Override
        public void onItemLongClick(View view, int position) {
                mLongClickEnabled = true;
                fabDeleteFavs.setVisibility(View.VISIBLE);
                mFavList.add(cardVenueList.get(position).getPlace_id());
                Animation lonClickAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.long_click);
                view.startAnimation(lonClickAnimation);
                view.getBackground().setColorFilter(mCardHighlightColor, PorterDuff.Mode.MULTIPLY );
        }
    });

    @OnClick(R.id.fab_delete_favs)
    public void deleteFavs() {
        mSdCard = new StorageOnSdCard();
        for (int i=0; i<mFavList.size(); i++) {
            if (mUserManager.checkFavorite(mUserManager.getUserEmail(), mFavList.get(i))) {
                mUserManager.removeFavorite(mUserManager.getUserEmail(), mFavList.get(i));
                mSdCard.removeFromSdCard("Favorites/" + mUserManager.getUserEmail(),mFavList.get(i));
            }
        }
        mRecyclerView.removeAllViews();
        FavoritePlacesAdapter fav = (FavoritePlacesAdapter) mRecyclerView.getAdapter();
        fav.clearData();
        getFavoritesFromSdCard();
        displayFavorites();
        mLongClickEnabled = false;
    }

    /**
     * Display the favorite places in a card view using RecyclerView.
     */
    private void displayFavorites() {
        if (mUserManager.getFavorite(mUserEmail) != null) {
            mRecyclerView.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(getContext());
            mAdapter = new FavoritePlacesAdapter(getContext(), cardVenueList);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    /**
     * Used to retrieve favorite place details stored in SD card.
     */
    private void getFavoritesFromSdCard() {
        Gson gson = new Gson();
        String favorites = mUserManager.getFavorite(mUserEmail);
        File favDir = new File(SD_PATH, "Favorites/" + mUserEmail);
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
