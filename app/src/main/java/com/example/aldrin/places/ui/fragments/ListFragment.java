package com.example.aldrin.places.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.aldrin.places.R;
import com.example.aldrin.places.adapters.CustomCardArrayAdapter;
import com.example.aldrin.places.events.ApiResponseUpdatedEvent;
import com.example.aldrin.places.helpers.UserManager;
import com.example.aldrin.places.models.nearby.GetFromJson;
import com.example.aldrin.places.models.nearby.Result;
import com.google.android.gms.maps.model.LatLng;
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
    private CustomCardArrayAdapter mCardAdapter;
    private Context mContext;
    private UserManager mUserManager;
    private GetFromJson mJsonResponse;
    private LatLng mPosition;
    private List<Result> results;
    private static Boolean mIsRestaurant = true;

    @BindView(R.id.tv_nothing_to_display)
    TextView tvNothingToDisplay;
    @BindView(R.id.places_list_view)
    ListView lvCardList;
    @BindView(R.id.progress_list_view)
    ProgressBar progressListView;

    public ListFragment() {
        // Required empty public constructor
    }

    public static ListFragment newInstance(Boolean isRestaurant) {
        mIsRestaurant = isRestaurant;
        return new ListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getContext();
        mUserManager = new UserManager(getContext());
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        if (mUserManager.getNearbyResponse(mIsRestaurant) != null) {
            showCardList();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        mUserManager.clearNearbyResponse();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationUpdateEvent(ApiResponseUpdatedEvent event) {
        showCardList();
    }

    /**
     * Displays venue details as a list of cards.
     */
    private void showCardList() {
        results = getLocationData();
        mCardAdapter = new CustomCardArrayAdapter(mContext, R.layout.layout_card_location_details, mPosition);
        if (results != null) {
            for (int i = 0; i < results.size(); i++) {
                Result venue = results.get(i);
                mCardAdapter.add(venue);
            }
            progressListView.setVisibility(View.INVISIBLE);
        }
        if (results.size() == 0){
            //tvNothingToDisplay.setVisibility(View.VISIBLE);
        }
        lvCardList.setAdapter(mCardAdapter);
    }

    /**
     * Get stored location data from shared preference.
     */
    private List<Result> getLocationData() {
        String apiResponse = mUserManager.getNearbyResponse(mIsRestaurant);
        Gson gson = new Gson();
        mJsonResponse = gson.fromJson(apiResponse, GetFromJson.class);
        String loc[] = mUserManager.getLocation();
        mPosition = new LatLng(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]));
        return mJsonResponse.getResults();
    }
}
