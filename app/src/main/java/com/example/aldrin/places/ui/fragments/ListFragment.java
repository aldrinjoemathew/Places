package com.example.aldrin.places.ui.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.aldrin.places.R;
import com.example.aldrin.places.adapters.CardListAdapter;
import com.example.aldrin.places.events.ApiResponseUpdatedEvent;
import com.example.aldrin.places.events.NavigationItemClickedEvent;
import com.example.aldrin.places.helpers.RecyclerClickListener;
import com.example.aldrin.places.helpers.InternalStorage;
import com.example.aldrin.places.helpers.UserManager;
import com.example.aldrin.places.models.nearby.GetFromJson;
import com.example.aldrin.places.models.nearby.Result;
import com.example.aldrin.places.ui.activities.PlacesDetailsActivity;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Displays the nearby places as a list of cards.
 */
public class ListFragment extends Fragment {

    public static final String TAG = ListFragment.class.getSimpleName();
    private static final int NAVIGATE_UP_FROM_CHILD = 2;
    private UserManager mUserManager;
    private String mUserEmail;
    private GetFromJson mJsonResponse;
    private List<Result> results;
    private Boolean mIsRestaurant = true;
    private Boolean mIsGrid = false;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ImageView ivAddFavorite;
    private InternalStorage mSdCard;
    private Gson gson = new Gson();
    private Intent placesDetailsIntent;
    private Animation favAnimation;

    @BindView(R.id.tv_nothing_to_display)
    TextView tvNothingToDisplay;
    @BindView(R.id.recycler_card_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.progress_list_view)
    ProgressBar progressListView;

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mUserManager = new UserManager(getContext());
        mUserEmail = mUserManager.getUserEmail();
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mIsRestaurant = this.getArguments().getBoolean("isRestaurant");
        if (!mIsRestaurant) {
            String serviceName = this.getArguments().getString("service");
        }
        mIsGrid = this.getArguments().getBoolean("isGrid");
        if (mUserManager.getNearbyResponse(mIsRestaurant) != null) {
            showCardList();
        }
        placesDetailsIntent = new Intent(getContext(), PlacesDetailsActivity.class);
        favAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.favorite);
        mRecyclerView.addOnItemTouchListener(listItemListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        mUserManager.clearNearbyResponse();
    }

    @Subscribe
    public void onNavigationItemEvent(NavigationItemClickedEvent event) {
        getFragmentManager().popBackStack();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationUpdateEvent(ApiResponseUpdatedEvent event) {
        showCardList();
    }

    RecyclerView.OnItemTouchListener listItemListener =
            new RecyclerClickListener(getContext(), new RecyclerClickListener.OnItemTouchListener() {
        @Override
        public void onItemClick(View view, int position) {
            String placeId = results.get(position).getPlace_id();
            placesDetailsIntent.putExtra("place_id", placeId);
            getActivity().startActivityForResult(placesDetailsIntent, NAVIGATE_UP_FROM_CHILD);
        }

        @Override
        public void onItemLongClick(View view, int position) {

        }

        @Override
        public void onDoubleTap(View childView, int childAdapterPosition) {
            ivAddFavorite = (ImageView) childView.findViewById(R.id.iv_add_favorite);
            ivAddFavorite.setVisibility(View.VISIBLE);
            ivAddFavorite.startAnimation(favAnimation);
            ivAddFavorite.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
            ivAddFavorite.setVisibility(View.GONE);
            String placeId = results.get(childAdapterPosition).getPlace_id();
            mSdCard = new InternalStorage();
            if (!mUserManager.checkFavorite(mUserEmail, placeId)) {
                mUserManager.addFavorite(mUserEmail, placeId);
                String placeDetails = gson.toJson(results.get(childAdapterPosition));
                mSdCard.addToSdCard(getContext(),
                        getString(R.string.favorites_path,mUserEmail), placeId, placeDetails);
            }
        }

        @Override
        public void onFling(View childView1, View childView2, int pos1, int pos2) {

        }
    });

    /**
     * Displays venue details as a list of cards.
     */
    private void showCardList() {
        results = getLocationData();
        if (results != null) {
            progressListView.setVisibility(View.INVISIBLE);
            mRecyclerView.setHasFixedSize(true);
            if (mIsGrid) {
                mLayoutManager = new GridLayoutManager(getContext(), 2);
            } else {
                mLayoutManager = new LinearLayoutManager(getContext());
            }
            mAdapter = new CardListAdapter(getContext(), results, mIsGrid);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mAdapter);
        }
        if (results.size() == 0){
            //tvNothingToDisplay.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Get stored location data from shared preference.
     */
    private List<Result> getLocationData() {
        String apiResponse = mUserManager.getNearbyResponse(mIsRestaurant);
        mJsonResponse = gson.fromJson(apiResponse, GetFromJson.class);
        return mJsonResponse.getResults();
    }
}
