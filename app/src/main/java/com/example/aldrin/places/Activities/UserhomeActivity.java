package com.example.aldrin.places.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.aldrin.places.AccountManagement.UserManager;
import com.example.aldrin.places.CustomClasses.NearbyServiceSearch;
import com.example.aldrin.places.Fragments.FavouritePlacesFragment;
import com.example.aldrin.places.Fragments.NearbyPlacesFragment;
import com.example.aldrin.places.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Arrays;
import java.util.HashMap;

public class UserhomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private static final int GET_FROM_GALLERY = 1;
    private static final String TAG_ERROR = "error";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private static final int MY_PERMISSIONS_READ_STORAGE = 2;
    private Button btnSubmitRadius;
    private NumberPicker mPickRadius;
    private TextView tvUser;
    private TextView tvEmail;
    private de.hdodenhof.circleimageview.CircleImageView imageViewProfile;
    private UserManager mUserManager;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private Location mLastLocation;
    private HashMap<String, String> mApiUrlData = new HashMap<String, String>();
    private NearbyServiceSearch mNearbyServiceSearch;
    private String mUserEmail;
    private LocationRequest mLocationRequest;
    private PopupWindow mPopupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userhome);
        setViewItems();
        getLastLocation();
        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
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
        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            mUserManager.changeProfilePic(mUserEmail, selectedImage);
            displayProfilePic();
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
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction();
        if (id == R.id.nav_nearby_places) {
            fragmentTransaction
                    .replace(R.id.content_frame, NearbyPlacesFragment.newInstance(), NearbyPlacesFragment.TAG).commit();
        } else if (id == R.id.nav_fav_places) {
            fragmentTransaction
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
        startLocationUpdates();
        if (mCurrentLocation != null) {
            getNearbyRestaurants();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG_ERROR, "Connection failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        if (mCurrentLocation == null) {
            startLocationUpdates();
        } else if (mLastLocation == null) {
            getLastLocation();
        } else if (mLastLocation.distanceTo(mCurrentLocation) > 50) {
            getNearbyRestaurants();
        } else if (mUserManager.getApiResponse() == null) {
            getNearbyRestaurants();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                    finish();
                }
                return;
            }
            case MY_PERMISSIONS_READ_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {

                }
                return;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.userhome, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_update_radius:
                initiatePopupWindow();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Initiate and display popup window.
     */
    private void initiatePopupWindow() {
        try {
            LayoutInflater inflater = (LayoutInflater) UserhomeActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.popup_update_radius,
                    (ViewGroup) findViewById(R.id.popup_radius));
            mPickRadius = (NumberPicker) layout.findViewById(R.id.numberPicker);
            mPickRadius.setDisplayedValues(new String[] {"100", "500", "1000", "2500", "5000"});
            mPickRadius.setMinValue(0);
            mPickRadius.setMaxValue(4);
            String radius = mUserManager.getSearchRadius(mUserEmail);
            int currentRadiusIndex = Arrays.asList(mPickRadius.getDisplayedValues()).indexOf(radius);
            mPickRadius.setValue(currentRadiusIndex);
            mPopupWindow = new PopupWindow(layout);
            mPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
            mPopupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
            mPopupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
            btnSubmitRadius = (Button) layout.findViewById(R.id.radius_update_button);
            btnSubmitRadius.setOnClickListener(submitRadius);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View.OnClickListener submitRadius = new View.OnClickListener() {
        public void onClick(View v) {
            mPopupWindow.dismiss();

            String radius = mPickRadius.getDisplayedValues()[mPickRadius.getValue()];
            mUserManager.updateSearchRadius(mUserEmail, radius);
            getNearbyRestaurants();
        }
    };

    /**
     * Sends a broadcast on location update.
     */
    void broadcastLocationUpdate() {
        Intent locationUpdate = new Intent("location_update");
        this.sendBroadcast(locationUpdate);
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
        imageViewProfile = (de.hdodenhof.circleimageview.CircleImageView)
                headerView.findViewById(R.id.nav_hdr_profile_image);
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
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Method to access the user's last location.
     */
    private void getLastLocation() {
        Location lastLocation = new Location("last_location");
        lastLocation.setLatitude(0);
        lastLocation.setLongitude(0);
        try {
            String[] loc = mUserManager.getLocation();
            lastLocation.setLatitude(Double.parseDouble(loc[0]));
            lastLocation.setLongitude(Double.parseDouble(loc[1]));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        mLastLocation = lastLocation;
    }

    /**
     * Method to generate the Places API url and request for nearby services.
     */
    private void getNearbyRestaurants() {
        mLastLocation = mCurrentLocation;
        String lat = String.valueOf(mCurrentLocation.getLatitude());
        String lng = String.valueOf(mCurrentLocation.getLongitude());
        mUserManager.updateLocation(lat, lng);
        mApiUrlData.put("service", "nearbysearch");
        mApiUrlData.put("lat", lat);
        mApiUrlData.put("lng", lng);
        mApiUrlData.put("type", "restaurant");
        mApiUrlData.put("radius", mUserManager.getSearchRadius(mUserEmail));
        mNearbyServiceSearch = new NearbyServiceSearch(this, mApiUrlData);
        mNearbyServiceSearch.execute();
    }

    /**
     * Method to display user's profile image.
     */
    private void displayProfilePic() {
        Uri profileImage;
        try {
            profileImage = mUserManager.getProfilePic(mUserEmail);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_READ_STORAGE);
            }
            imageViewProfile.setImageURI(profileImage);
        } catch (NullPointerException e) {
            Log.e(TAG_ERROR, e.toString());
        }
    }
}
