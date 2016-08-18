package com.example.aldrin.places.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.aldrin.places.CustomClasses.InternalStorage;
import com.example.aldrin.places.NearbyJsonClasses.Geometry;
import com.example.aldrin.places.NearbyJsonClasses.GetFromJson;
import com.example.aldrin.places.NearbyJsonClasses.Result;
import com.example.aldrin.places.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import static com.example.aldrin.places.CustomClasses.NearbyServiceSearch.locationDetailsAvailable;
import static com.example.aldrin.places.CustomClasses.NearbyServiceSearch.callbackBackgroundThreadCompleted;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment {
    private static final String KEY_LAT = "lat";
    private static final String KEY_LNG = "lng";
    private static final String KEY_JSON = "key";
    private static final String TAG_ERROR = "error";
    private GoogleMap mGoogleMap;
    private LatLng mPosition;
    private GetFromJson mJsonResponse;
    private Context mContext;
    private SupportMapFragment mapFragment;
    private Boolean loadMap;

    public MapFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getContext();
        mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMap = true;
        if (locationDetailsAvailable) {
            updateMap();
        } else {
            setUpMap();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        loadMap = false;
    }

    /**
     * Set up google map with a dummy location if current LatLng value is not available.
     * Check whether NearbyServiceSearch has completed.
     * Call updateMap when background process is completed.
     */
    private void setUpMap() {
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap gMap) {
                mGoogleMap = gMap;
                mPosition = new LatLng(8.5241,76.9366);
                CameraPosition cameraPosition =
                        new CameraPosition.Builder().target(mPosition).zoom(18).build();
                mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });
        callbackBackgroundThreadCompleted = new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if (loadMap) {
                    updateMap();
                }
                return false;
            }
        };
    }

    /**
     * Update Google map with user's current location.
     */
    private void updateMap() {
        getDataFromCache();
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap gMap) {
                mGoogleMap = gMap;
                addLocationMarkers();
                CameraPosition cameraPosition =
                        new CameraPosition.Builder().target(mPosition).zoom(18).build();
                mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });
    }

    /**
     * Add markers on google map.
     */
    private void addLocationMarkers() {
        List<Result> results = mJsonResponse.getResults();
        if (results != null) {
            LatLng latLng;
            for (int i = 0; i < results.size(); i++) {
                Result venue = results.get(i);
                Geometry geometry = venue.getGeometry();
                String venueName = venue.getName();
                com.example.aldrin.places.NearbyJsonClasses.Location location = geometry.getLocation();
                latLng = new LatLng(location.getLat(), location.getLng());
                mGoogleMap.addMarker(new MarkerOptions().position(latLng)).setTitle(venueName);
            }
        }
    }

    /**
     * Get stored location data from device cache storage.
     */
    private void getDataFromCache() {
        try {

            Log.i("hbghdsjmc", "dskhjdfcdfsf");
            mJsonResponse = (GetFromJson) InternalStorage.readObject(mContext, KEY_JSON);
            String lat = (String) InternalStorage.readObject(mContext, KEY_LAT);
            String lng = (String) InternalStorage.readObject(mContext, KEY_LNG);
            mPosition = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG_ERROR, e.getMessage());
        }
    }
}
