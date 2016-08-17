package com.example.aldrin.places.Activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
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
import com.example.aldrin.places.CustomClasses.NearbyLocSearch;
import com.example.aldrin.places.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;

public class UserhomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private TextView tvUser;
    private TextView tvEmail;
    private UserManager mUserManager;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private HashMap<String, String> data = new HashMap<String, String>();
    private NearbyLocSearch search;
    private AsyncTask<Void, Void, String> a;
    private Boolean bgProcessComplete;
    private Boolean mRequestingLocationUpdates = true;
    LocationRequest mLocationRequest;
    String lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userhome);
        setViewItems();
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
        // Handle navigation view item clicks here.
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

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
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
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }

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

    private void setViewItems() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_layout);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, NearbyPlacesFragment.newInstance(), NearbyPlacesFragment.TAG).commit();
        mUserManager = new UserManager(getApplicationContext());
        String email = mUserManager.getUserEmail();
        HashMap<String, String> user = new HashMap<String, String>();
        user = mUserManager.getUserDetails(email);
        View headerView = navigationView.inflateHeaderView(R.layout.nav_header);
        tvUser = (TextView) headerView.findViewById(R.id.nav_hdr_name);
        tvEmail = (TextView) headerView.findViewById(R.id.nav_hdr_email);
        String name = user.get(UserManager.KEY_FIRST_NAME) + " " + user.get(UserManager.KEY_LAST_NAME);
        tvUser.setText(name);
        tvEmail.setText(email);
    }

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

    private void getNearbyRestaurants() {
        lat = String.valueOf(mLastLocation.getLatitude());
        lng = String.valueOf(mLastLocation.getLongitude());
        Log.i("Lat",lat);
        Log.i("Long",lng);
        data.put("lat", String.valueOf(lat));
        data.put("lng", String.valueOf(lng));
        data.put("type", "restaurant");
        data.put("radius", "500");
        data.put("key", getString(R.string.google_places_web_key));
        search = new NearbyLocSearch(this, data);
        search.execute();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mLastLocation != null) {
            getNearbyRestaurants();
        }
    }
}
