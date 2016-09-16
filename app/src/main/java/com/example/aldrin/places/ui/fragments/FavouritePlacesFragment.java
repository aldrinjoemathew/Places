package com.example.aldrin.places.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.example.aldrin.places.R;
import com.example.aldrin.places.adapters.FavoritePlacesAdapter;
import com.example.aldrin.places.helpers.RecyclerClickListener;
import com.example.aldrin.places.helpers.InternalStorage;
import com.example.aldrin.places.helpers.UserManager;
import com.example.aldrin.places.models.placesdetails.Result;
import com.example.aldrin.places.ui.activities.PlacesDetailsActivity;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
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
    private List<Result> cardVenueList = new ArrayList<>();
    private List<String> mToDeleteList = new ArrayList<>();
    private List<String> mFavorites = new ArrayList<>();
    private UserManager mUserManager;
    private String mUserEmail;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private InternalStorage mFileStorage;
    private Boolean mLongClickEnabled = false;
    private TextView tvFragmentTitle;

    @BindView(R.id.recycler_favorite)
    RecyclerView mRecyclerView;
    @BindView(R.id.fab_delete_favs)
    FloatingActionButton fabDeleteFavs;
    @BindView(R.id.tv_no_favs_message)
    TextView tvNoFavorites;
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
        mFileStorage = new InternalStorage();
        tvFragmentTitle = (TextView) getActivity().findViewById(R.id.tv_fragment_title);
        tvFragmentTitle.setText("Favorites");
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

    @Override
    public void onResume() {
        super.onResume();
        tvFragmentTitle.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStop() {
        super.onStop();
        tvFragmentTitle.setVisibility(View.GONE);
    }

    RecyclerView.OnItemTouchListener onFavoriteClickListener = new RecyclerClickListener(getContext(), new RecyclerClickListener.OnItemTouchListener() {
        @Override
        public void onItemClick(View view, int position) {
            if (mLongClickEnabled) {
                if (mToDeleteList.contains(cardVenueList.get(position).getPlace_id())) {
                    cardVenueList.get(position).setSelected(false);
                    mAdapter.notifyDataSetChanged();
                    mToDeleteList.remove(cardVenueList.get(position).getPlace_id());
                } else {
                    cardVenueList.get(position).setSelected(true);
                    mAdapter.notifyDataSetChanged();
                    mToDeleteList.add(cardVenueList.get(position).getPlace_id());
                }
                if (mToDeleteList.isEmpty()) {
                    mLongClickEnabled = false;
                    fabDeleteFavs.setVisibility(View.GONE);
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
                mToDeleteList.add(cardVenueList.get(position).getPlace_id());
                Animation lonClickAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.long_click);
                view.startAnimation(lonClickAnimation);
                cardVenueList.get(position).setSelected(true);
                mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onDoubleTap(View childView, int childAdapterPosition) {

        }

        @Override
        public void onFling(View childView1, View childView2, int pos1, int pos2) {
            if (!mLongClickEnabled) {
                Collections.swap(cardVenueList, pos1, pos2);
                cardVenueList.add(pos2, cardVenueList.get(pos1));
                cardVenueList.remove(pos1);
                mAdapter.notifyDataSetChanged();
                mUserManager.swapFavorites(pos1, pos2);
            }
        }
    });

    @OnClick(R.id.fab_delete_favs)
    public void deleteFavs() {
        for (int i = 0; i< mToDeleteList.size(); i++) {
            if (mUserManager.checkFavorite(mUserManager.getUserEmail(), mToDeleteList.get(i))) {
                mUserManager.removeFavorite(mUserManager.getUserEmail(), mToDeleteList.get(i));
                mFileStorage.removeFromSdCard(getContext(), "Favorites/" + mUserManager.getUserEmail(), mToDeleteList.get(i));
            }
        }
        mRecyclerView.removeAllViews();
        FavoritePlacesAdapter fav = (FavoritePlacesAdapter) mRecyclerView.getAdapter();
        fav.clearData();
        getFavoritesFromSdCard();
        displayFavorites();
        mToDeleteList.clear();
        mLongClickEnabled = false;
        fabDeleteFavs.setVisibility(View.GONE);
    }

    /**
     * Display the favorite places in a card view using RecyclerView.
     */
    private void displayFavorites() {
        if (mUserManager.getFavorite() != null) {
            mRecyclerView.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(getContext());
            mAdapter = new FavoritePlacesAdapter(getContext(), cardVenueList);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            tvNoFavorites.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Used to retrieve favorite place details stored in SD card.
     */
    private void getFavoritesFromSdCard() {
        Gson gson = new Gson();
        String favorites = mUserManager.getFavorite();
        if (favorites != null) {
            String[] favs = favorites.split(",");
            for (int i=0; i<favs.length; i++) {
                if (favs[i]!=null && favs[i].length()>0) {
                    mFavorites.add(favs[i]);
                    String placeDetails = mFileStorage.getFromSdCard(getContext(), "Favorites/" + mUserEmail, favs[i]);
                    com.example.aldrin.places.models.placesdetails.Result venue =
                            gson.fromJson(placeDetails, Result.class);
                    cardVenueList.add(venue);
                }
            }
        }
    }
}
