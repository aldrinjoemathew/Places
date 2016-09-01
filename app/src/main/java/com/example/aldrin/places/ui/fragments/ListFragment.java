package com.example.aldrin.places.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.aldrin.places.helpers.UserManager;
import com.example.aldrin.places.adapters.CustomCardArrayAdapter;
import com.example.aldrin.places.models.nearby.GetFromJson;
import com.example.aldrin.places.models.nearby.Result;
import com.example.aldrin.places.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Displays the nearby places as a list of cards.
 */
public class ListFragment extends Fragment {

    private CustomCardArrayAdapter mCardAdapter;
    private ListView mCardList;
    private Context mContext;
    private UserManager mUserManager;
    private GetFromJson mJsonResponse;
    private LatLng mPosition;
    private List<Result> results;

    public ListFragment() {
        // Required empty public constructor
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

        mCardList = (ListView) view.findViewById(R.id.places_list_view);

        if (mUserManager.getNearbyResponse() != null) {
            showCardList();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mContext.registerReceiver(locationUpdateReceiver, new IntentFilter("location_update"));
    }

    @Override
    public void onStop() {
        super.onStop();
        mContext.unregisterReceiver(locationUpdateReceiver);
    }

    /**
     * Broadcast receiver to get notification when location is updated.
     */
    BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showCardList();
        }
    };

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
        }
        mCardList.setAdapter(mCardAdapter);
    }

    /**
     * Get stored location data from shared preference.
     */
    private List<Result> getLocationData() {
        String apiResponse = mUserManager.getNearbyResponse();
        Gson gson = new Gson();
        mJsonResponse = gson.fromJson(apiResponse, GetFromJson.class);
        String loc[] = mUserManager.getLocation();
        mPosition = new LatLng(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]));
        return mJsonResponse.getResults();
    }
}
