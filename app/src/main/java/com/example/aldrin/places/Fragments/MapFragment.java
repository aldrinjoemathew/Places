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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.aldrin.places.AccountManagement.UserInformation;
import com.example.aldrin.places.AccountManagement.UserManager;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

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
    private Marker marker;
    private Context mContext;
    private SupportMapFragment mapFragment;
    private Boolean loadMap;
    private UserManager mUserManager;

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
        mUserManager = new UserManager(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMap = true;
        if (mUserManager.getApiResponse() != null) {
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
                        new CameraPosition.Builder().target(mPosition).zoom(12).build();
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
        getDataInternalStorage();
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap gMap) {
                mGoogleMap = gMap;
                mGoogleMap.clear();
                addLocationMarkers();
                CameraPosition cameraPosition =
                        new CameraPosition.Builder().target(mPosition).zoom(12).build();
                mGoogleMap.addMarker(new MarkerOptions().position(mPosition)).setTitle("My position");
                //mGoogleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
                mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });
    }

    /**
     * Add markers on google map.
     */
    private void addLocationMarkers() {
        String apiResponse = mUserManager.getApiResponse();
        Gson gson = new Gson();
        mJsonResponse = gson.fromJson(apiResponse, GetFromJson.class);
        List<Result> results = mJsonResponse.getResults();
        if (results != null) {
            LatLng latLng;
            for (int i = 0; i < results.size(); i++) {
                Result venue = results.get(i);
                Geometry geometry = venue.getGeometry();
                String venueName = venue.getName();
                com.example.aldrin.places.NearbyJsonClasses.Location location = geometry.getLocation();
                latLng = new LatLng(location.getLat(), location.getLng());
                String venueDetails = gson.toJson(venue);
                MarkerOptions marker = new MarkerOptions().position(latLng).snippet(venueDetails);
                mGoogleMap.addMarker(marker).setTitle(venueName);
                mGoogleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
            }
        }
    }

    /**
     * Get stored location data from device cache storage.
     */
    private void getDataInternalStorage() {
        String apiResponse = mUserManager.getApiResponse();
        Gson gson = new Gson();
        mJsonResponse = gson.fromJson(apiResponse, GetFromJson.class);
        String loc[] = mUserManager.getLocation();
        mPosition = new LatLng(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]));
    }


    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private View view;
        private Result venue;

        public CustomInfoWindowAdapter() {
        }

        @Override
        public View getInfoContents(Marker marker) {

            /*if (MapFragment.this.marker != null
                    && MapFragment.this.marker.isInfoWindowShown()) {
                MapFragment.this.marker.hideInfoWindow();
                MapFragment.this.marker.showInfoWindow();
            }*/

            return null;
        }

        @Override
        public View getInfoWindow(final Marker marker) {
            MapFragment.this.marker = marker;
            view = getLayoutInflater(null).inflate(R.layout.card_location_details,
                    null);
            String venueDetails = marker.getSnippet();
            Gson gson = new Gson();
            venue = gson.fromJson(venueDetails, Result.class);
            final ImageView image = ((ImageView) view.findViewById(R.id.card_image));
            final TextView tvTitle = ((TextView) view.findViewById(R.id.rest_name_text_view));
            final TextView tvDistance = ((TextView) view.findViewById(R.id.distance_text_view));
            final TextView tvAddress = ((TextView) view.findViewById(R.id.address_text_view));
            String title = venue.getName();
            String address = venue.getVicinity();
            tvTitle.setText(title);
            tvAddress.setText(address);
            tvDistance.setText("86548");
            return view;
        }
    }
}
