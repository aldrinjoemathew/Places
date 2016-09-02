package com.example.aldrin.places.ui.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.aldrin.places.R;
import com.example.aldrin.places.helpers.NearbyServiceSearch;
import com.example.aldrin.places.helpers.UserManager;
import com.example.aldrin.places.models.nearby.Geometry;
import com.example.aldrin.places.models.nearby.GetFromJson;
import com.example.aldrin.places.models.nearby.Result;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment {
    private static final String TAG_ERROR = "error";
    private GoogleMap mGoogleMap;
    private LatLng mPosition;
    private GetFromJson mJsonResponse;
    private Marker marker;
    private Context mContext;
    private SupportMapFragment mapFragment;
    private UserManager mUserManager;
    private Bitmap smallMarker;

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
        createMarkerBitmap();
        if (mUserManager.getNearbyResponse() != null) {
            updateMap();
        } else {
            setUpMap();
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
        mGoogleMap.clear();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationUpdateEvent(NearbyServiceSearch.LocationUpdateEvent event) {
            updateMap();
    }

    /**
     * Create a custom marker to mark nearby locations.
     */
    private void createMarkerBitmap() {
        int height = 32;
        int width = 32;
        Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.ic_location);
        smallMarker = Bitmap.createScaledBitmap(icon, width, height, false);
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

    }

    /**
     * Update Google map with user's current location.
     */
    private void updateMap() {
        getLocationData();
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap gMap) {
                mGoogleMap = gMap;
                mGoogleMap.clear();
                mGoogleMap.addMarker(new MarkerOptions().position(mPosition).title("Me"));
                addLocationMarkers();
                CameraPosition cameraPosition =
                        new CameraPosition.Builder().target(mPosition).zoom(16).build();
                mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });
    }

    /**
     * Add markers on google map.
     */
    private void addLocationMarkers() {
        mGoogleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        String apiResponse = mUserManager.getNearbyResponse();
        Gson gson = new Gson();
        mJsonResponse = gson.fromJson(apiResponse, GetFromJson.class);
        List<Result> results = mJsonResponse.getResults();
        if (results != null) {
            LatLng latLng;
            for (int i = 0; i < results.size(); i++) {
                Result venue = results.get(i);
                Geometry geometry = venue.getGeometry();
                String venueName = venue.getName();
                com.example.aldrin.places.models.nearby.Location location = geometry.getLocation();
                latLng = new LatLng(location.getLat(), location.getLng());
                String venueDetails = gson.toJson(venue);
                MarkerOptions marker = new MarkerOptions().position(latLng).snippet(venueDetails);
                /*mGoogleMap.addMarker(marker.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))).setTitle(venueName);*/
                mGoogleMap.addMarker(marker.icon(BitmapDescriptorFactory.defaultMarker(173))).setTitle(venueName);
            }
        }
    }

    /**
     * Get stored location data from device cache storage.
     */
    private void getLocationData() {
        String apiResponse = mUserManager.getNearbyResponse();
        Gson gson = new Gson();
        mJsonResponse = gson.fromJson(apiResponse, GetFromJson.class);
        String loc[] = mUserManager.getLocation();
        mPosition = new LatLng(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]));
    }

    /**
     * A custom class to display info window on clicking on marker.
     */
    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private View view;

        public CustomInfoWindowAdapter() {
        }

        @Override
        public View getInfoContents(Marker marker) {
            if (MapFragment.this.marker != null
                    && MapFragment.this.marker.isInfoWindowShown()) {
                MapFragment.this.marker.hideInfoWindow();
                MapFragment.this.marker.showInfoWindow();
            }
            return null;
        }

        @Override
        public View getInfoWindow(final Marker marker) {
            MapFragment.this.marker = marker;
            Result venue;
            view = getLayoutInflater(null).inflate(R.layout.layout_card_location_details,
                    null);
            try {
                String venueDetails = marker.getSnippet();
                if (venueDetails == null) {
                    return null;
                }
                Gson gson = new Gson();
                venue = gson.fromJson(venueDetails, Result.class);
                ImageView image = (ImageView) view.findViewById(R.id.iv_venue_icon);
                TextView tvTitle = (TextView) view.findViewById(R.id.tv_rest_name);
                TextView tvDistance = (TextView) view.findViewById(R.id.tv_distance);
                TextView tvAddress = (TextView) view.findViewById(R.id.tv_address);
                RatingBar ratingVenue = (RatingBar) view.findViewById(R.id.ratingbar_venue);
                String title = venue.getName();
                String address = venue.getVicinity();
                String distance = distanceFromCurrentPosition();
                tvTitle.setText(title);
                tvAddress.setText(address);
                tvDistance.setText(distance + " km");
                ratingVenue.setRating(venue.getRating());
            } catch (NullPointerException e) {
                Log.e(TAG_ERROR, e.toString());
            }
            return view;
        }

        /**
         * Method to find the distance to a venue from current position.
         * @return distance
         */
        private String distanceFromCurrentPosition() {
            Location userLocation = new Location("user_location");
            Location venueLocation = new Location("venue_location");
            userLocation.setLatitude(mPosition.latitude);
            userLocation.setLongitude(mPosition.longitude);
            venueLocation.setLatitude(marker.getPosition().latitude);
            venueLocation.setLongitude(marker.getPosition().longitude);
            DecimalFormat df = new DecimalFormat("#.##");
            Float distance = userLocation.distanceTo(venueLocation); //distance in meter
            return String.valueOf(Double.parseDouble(df.format(distance/1000))); //distance in km
        }
    }
}
