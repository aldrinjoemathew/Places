package com.example.aldrin.places.Fragments;

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

import static com.example.aldrin.places.CustomClasses.NearbyLocSearch.locationDetailsAvailable;
import static com.example.aldrin.places.CustomClasses.NearbyLocSearch.callbackBackgroundThreadCompleted;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment {
    private static final String KEY_LAT = "lat";
    private static final String KEY_LNG = "lng";
    private static final String KEY_JSON = "key";
    private GoogleMap mGoogleMap;
    private LatLng mPosition;
    private GetFromJson mJsonResponse;

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
    }

    @Override
    public void onResume() {
        super.onResume();
        if (locationDetailsAvailable) {
            updateMap();
        } else {
            setUpMap();
        }
    }

    /**
     * Set up google map with a dummy location if current LatLng value is not available.
     * Check whether NearbyLocSearch has completed.
     * Call updateMap when background process is completed.
     */
    private void setUpMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap gMap) {
                Log.e("error","inside onmapready");
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
                updateMap();
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
            mJsonResponse = (GetFromJson) InternalStorage.readObject(getContext(), KEY_JSON);
            String lat = (String) InternalStorage.readObject(getContext(), KEY_LAT);
            String lng = (String) InternalStorage.readObject(getContext(), KEY_LNG);
            mPosition = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        } catch (IOException e) {
            Log.e("error", e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e("error", e.getMessage());
        }
    }
}
