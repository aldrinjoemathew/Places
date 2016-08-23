package com.example.aldrin.places.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.aldrin.places.AccountManagement.UserManager;
import com.example.aldrin.places.Adapters.CustomCardArrayAdapter;
import com.example.aldrin.places.NearbyJsonClasses.GetFromJson;
import com.example.aldrin.places.NearbyJsonClasses.Result;
import com.example.aldrin.places.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.List;

import static com.example.aldrin.places.CustomClasses.NearbyServiceSearch.callbackBackgroundThreadCompleted;
import static com.example.aldrin.places.CustomClasses.NearbyServiceSearch.callbackList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment {

    private CustomCardArrayAdapter mCardAdapter;
    private ListView mCardList;
    private Context mContext;
    private UserManager mUserManager;
    private GetFromJson mJsonResponse;
    private LatLng mPosition;

    public ListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContext = getContext();
        mUserManager = new UserManager(getContext());
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCardList = (ListView) view.findViewById(R.id.places_list_view);

        callbackList = new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                showCardList();
                return false;
            }
        };
    }

    private void showCardList() {
        List<Result> results = getLocationData();
        mCardAdapter = new CustomCardArrayAdapter(mContext, R.layout.card_location_details, mPosition);
        if (results != null) {
            for (int i = 0; i < results.size(); i++) {
                Result venue = results.get(i);
                mCardAdapter.add(venue);
            }
        }
        mCardList.setAdapter(mCardAdapter);
    }

    /**
     * Get stored location data from device cache storage.
     */
    private List<Result> getLocationData() {
        String apiResponse = mUserManager.getApiResponse();
        Gson gson = new Gson();
        mJsonResponse = gson.fromJson(apiResponse, GetFromJson.class);
        String loc[] = mUserManager.getLocation();
        mPosition = new LatLng(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]));
        return mJsonResponse.getResults();
    }

    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            showCardList();
        }
    }
}
