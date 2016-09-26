package com.example.aldrin.places.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.example.aldrin.places.R;
import com.example.aldrin.places.adapters.FavoritePlacesAdapter;
import com.example.aldrin.places.helpers.InternalStorage;
import com.example.aldrin.places.helpers.RecyclerClickListener;
import com.example.aldrin.places.helpers.UserManager;
import com.example.aldrin.places.models.placesdetails.Result;
import com.example.aldrin.places.ui.activities.PlaceDetailsActivity;
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
    private static final int NAVIGATE_UP_FROM_CHILD = 2;
    private List<Result> cardVenueList = new ArrayList<>();
    private List<String> mToDeleteList = new ArrayList<>();
    private List<String> mFavorites = new ArrayList<>();
    private Gson gson = new Gson();
    private UserManager mUserManager;
    private String mUserEmail;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private InternalStorage mFileStorage;
    private Boolean mLongClickEnabled = false;
    private Boolean mDragEnabled = false;
    private Result venue;
    private Intent placesDetailsIntent;
    private Animation lonClickAnimation;
    private Handler handler = new Handler();

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
        getFavoritesFromSdCard();
        displayFavorites();
        mRecyclerView.addOnItemTouchListener(onFavoriteClickListener);
        placesDetailsIntent = new Intent(getContext(), PlacesDetailsActivity.class);
        lonClickAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.long_click);
        ItemTouchHelper itemTouch = new ItemTouchHelper(ithCallback);
        itemTouch.attachToRecyclerView(mRecyclerView);
    }

    /**
     * The callback recieved on recycler item touch is used to manage
     * dragging and swiping actions on recycler item views.
     * On dragging cards are interchanged.
     * On swiping cards are deleted from recycler view.
     */
    ItemTouchHelper.Callback ithCallback = new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView,
                              RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            mDragEnabled = true;
            int pos1 = viewHolder.getAdapterPosition();
            int pos2 = target.getAdapterPosition();
            Collections.swap(cardVenueList, pos1, pos2);
            mAdapter.notifyItemMoved(pos1, pos2);
            mUserManager.swapFavorites(pos1, pos2);
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int pos = viewHolder.getAdapterPosition();
            String placeId = cardVenueList.get(pos).getPlace_id();
            if (mUserManager.checkFavorite(mUserEmail, placeId)) {
                mUserManager.removeFavorite(mUserEmail, placeId);
                mFileStorage.removeFromSdCard(getContext(),
                        getString(R.string.favorites_path,mUserEmail), placeId);
            }
            cardVenueList.remove(pos);
            mAdapter.notifyItemRemoved(pos);
        }
    };

    /**
     * If a favorite place is marked not favorite from PlaceDetails activity
     * it is deleted from the recycler view.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NAVIGATE_UP_FROM_CHILD) {
            if(resultCode == Activity.RESULT_OK){
                Boolean changed = data.getBooleanExtra("valueChanged", false);
                if (changed) {
                    int pos = data.getIntExtra("pos", 0);
                    cardVenueList.remove(pos);
                    mAdapter.notifyItemRemoved(pos);
                }
            }
        }
    }

    RecyclerView.OnItemTouchListener onFavoriteClickListener =
            new RecyclerClickListener(getContext(), new RecyclerClickListener.OnItemTouchListener() {
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
                venue = cardVenueList.get(position);
                String placeId = venue.getPlace_id();
                placesDetailsIntent.putExtra("place_id", placeId);
                placesDetailsIntent.putExtra("pos", position);
                getActivity().startActivityForResult(placesDetailsIntent, NAVIGATE_UP_FROM_CHILD);
            }
        }

        @Override
        public void onItemLongClick(final View view, final int position) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mDragEnabled) {
                        mDragEnabled  = false;
                        return;
                    }
                    mLongClickEnabled = true;
                    fabDeleteFavs.setVisibility(View.VISIBLE);
                    mToDeleteList.add(cardVenueList.get(position).getPlace_id());
                    view.startAnimation(lonClickAnimation);
                    cardVenueList.get(position).setSelected(true);
                    mAdapter.notifyDataSetChanged();
                }
            }, 800);

        }

        @Override
        public void onDoubleTap(View childView, int childAdapterPosition) {

        }
    });

    @OnClick(R.id.fab_delete_favs)
    public void deleteFavs() {
        int length = mToDeleteList.size();
        String venueId;
        for (int i = 0; i<length; i++) {
            venueId = mToDeleteList.get(i);
            if (mUserManager.checkFavorite(mUserEmail, venueId)) {
                mUserManager.removeFavorite(mUserEmail, venueId);
                mFileStorage.removeFromSdCard(getContext(), getString(R.string.favorites_path,mUserEmail), venueId);
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
        String favorites = mUserManager.getFavorite();
        if (favorites != null) {
            Log.i("fav", favorites);
            String[] favs = favorites.split(",");
            int length = favs.length;
            for (int i=0; i<length; i++) {
                String favorite = favs[i];

                if (favorite!=null && favorite.length()>0) {
                    mFavorites.add(favorite);
                    String placeDetails = mFileStorage.getFromSdCard(getContext(),
                            getString(R.string.favorites_path,mUserEmail), favorite);
                    com.example.aldrin.places.models.placesdetails.Result venue =
                            gson.fromJson(placeDetails, Result.class);
                    cardVenueList.add(venue);
                }
            }
        }
    }
}
