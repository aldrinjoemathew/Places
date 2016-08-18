package com.example.aldrin.places.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.aldrin.places.AccountManagement.UserManager;
import com.example.aldrin.places.CustomClasses.InternalStorage;
import com.example.aldrin.places.Fragments.FavouritePlacesFragment;
import com.example.aldrin.places.Fragments.NearbyPlacesFragment;
import com.example.aldrin.places.CustomClasses.NearbyServiceSearch;
import com.example.aldrin.places.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.HashMap;

public class UserhomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private static final int GET_FROM_GALLERY = 1;
    private static final String TAG_ERROR = "error";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LNG = "lng";
    private TextView tvUser;
    private TextView tvEmail;
    private de.hdodenhof.circleimageview.CircleImageView imageViewProfile;
    private UserManager mUserManager;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private HashMap<String, String> mApiUrlData = new HashMap<String, String>();
    private NearbyServiceSearch nearbyServiceSearch;
    private String mUserEmail;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userhome);
        setViewItems();
        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            mUserManager.changeProfilePic(mUserEmail, selectedImage);
            displayProfilePic();
        }
    }

    private void displayProfilePic() {
        Uri profileImage;
        try {
            profileImage = mUserManager.getProfilePic(mUserEmail);
            imageViewProfile.setImageURI(profileImage);
        } catch (NullPointerException e) {
            Log.e(TAG_ERROR, e.toString());
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (item.isChecked()) {
            closeDrawer();
            return true;
        }
        int id = item.getItemId();
        if (id == R.id.nav_nearby_places) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, NearbyPlacesFragment.newInstance(), NearbyPlacesFragment.TAG).commit();
        } else if (id == R.id.nav_fav_places) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, FavouritePlacesFragment.newInstance(), FavouritePlacesFragment.TAG).commit();
        } else if (id == R.id.nav_find_service) {

        } else if (id == R.id.nav_profile) {

        } else if (id == R.id.nav_logout) {
            mUserManager.logoutUser();
        }
        closeDrawer();
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startLocationUpdates();
        if (mLastLocation != null) {
            getNearbyRestaurants();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mLastLocation != null) {
            getNearbyRestaurants();
        }
    }

    /**
     * Method to close the navigation drawer.
     */
    private void closeDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    /**
     * Method to set the view items on screen.
     */
    private void setViewItems() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.inflateHeaderView(R.layout.nav_header);
        tvUser = (TextView) headerView.findViewById(R.id.nav_hdr_name);
        tvEmail = (TextView) headerView.findViewById(R.id.nav_hdr_email);
        imageViewProfile = (de.hdodenhof.circleimageview.CircleImageView) headerView.findViewById(R.id.nav_hdr_profile_image);
        mUserManager = new UserManager(getApplicationContext());
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setCheckedItem(R.id.nav_nearby_places);
        navigationView.setNavigationItemSelectedListener(this);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, NearbyPlacesFragment.newInstance(), NearbyPlacesFragment.TAG).commit();
        mUserEmail = mUserManager.getUserEmail();
        HashMap<String, String> user = mUserManager.getUserDetails(mUserEmail);
        String name = user.get(UserManager.KEY_FIRST_NAME) + " " + user.get(UserManager.KEY_LAST_NAME);
        tvUser.setText(name);
        tvEmail.setText(mUserEmail);
        displayProfilePic();
    }

    /**
     * Method to request current location of the user.
     */
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Method to generate the Places API url and request for nearby services.
     */
    private void getNearbyRestaurants() {
        String lat = String.valueOf(mLastLocation.getLatitude());
        String lng = String.valueOf(mLastLocation.getLongitude());
        try {
            InternalStorage.writeObject(this, KEY_LAT, lat);
            InternalStorage.writeObject(this, KEY_LNG, lng);
        } catch (IOException e) {
            Log.e(TAG_ERROR, e.getMessage());
        }
        mApiUrlData.put("service", "nearbysearch");
        mApiUrlData.put("lat", lat);
        mApiUrlData.put("lng", lng);
        mApiUrlData.put("type", "restaurant");
        mApiUrlData.put("radius", mUserManager.getSearchRadius(mUserEmail));
        nearbyServiceSearch = new NearbyServiceSearch(this, mApiUrlData);
        nearbyServiceSearch.execute();
    }

}
