package com.example.aldrin.places.ui.activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.aldrin.places.R;
import com.example.aldrin.places.events.LocationUpdatedEvent;
import com.example.aldrin.places.events.ConfigurationChangedEvent;
import com.example.aldrin.places.events.NavigationItemClickedEvent;
import com.example.aldrin.places.events.ProfileImageUpdatedEvent;
import com.example.aldrin.places.helpers.UserManager;
import com.example.aldrin.places.interfaces.ApiInterface;
import com.example.aldrin.places.models.nearby.GetFromJson;
import com.example.aldrin.places.ui.fragments.FavouritePlacesFragment;
import com.example.aldrin.places.ui.fragments.GalleryGridFragment;
import com.example.aldrin.places.ui.fragments.NearbyPlacesFragment;
import com.example.aldrin.places.ui.fragments.ProfileFragment;
import com.example.aldrin.places.ui.fragments.ServicesFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserhomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ProfileFragment.OnProfilePicChangedListener{

    private static final int GET_FROM_GALLERY = 1;
    private static final int NAVIGATE_UP_FROM_CHILD = 2;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private static final int MY_PERMISSIONS_READ_STORAGE = 2;
    private static final String TAG_INFO = "INFO";
    private static final String TAG_ERROR = "ERROR";
    private static final String []locationPermissions =
            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

    @BindString(R.string.google_places_web_key)
    String mGooglePlacesWebKey;
    @BindString(R.string.google_api_base_url)
    String mGoogleApiBaseUrl;
    @BindView(R.id.nav_hdr_name)
    TextView tvUser;
    @BindView(R.id.nav_hdr_email)
    TextView tvEmail;
    @BindView(R.id.nav_hdr_profile_image)
    de.hdodenhof.circleimageview.CircleImageView imageViewProfile;

    private DrawerLayout mDrawer;
    private NavigationView mNavigationView;
    private UserManager mUserManager;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private Location mLastLocation = new Location("lastLocation");
    private String mUserEmail;
    private LocationRequest mLocationRequest;
    private MenuItem mPreviousMenuItem;
    private Boolean mNavigateUpFromChild = false;
    private Boolean mIsRestaurant = true;
    private Call<GetFromJson> mCallback;
    private Retrofit mRetrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userhome);
        setViewItems();
        getLastLocation();
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean locationEnabled = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!locationEnabled) {
            buildAlertMessageNoGps();
        }
        mRetrofit = new Retrofit.Builder()
                .baseUrl(mGoogleApiBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
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
            mUserManager.changeProfilePic(selectedImage);
            displayProfilePic();
            changeImageInProfileFragment();
        }
        if(requestCode == NAVIGATE_UP_FROM_CHILD && resultCode == RESULT_OK){
            mNavigateUpFromChild = true;
        }
        getSupportFragmentManager().findFragmentById(R.id.content_frame)
                .onActivityResult(requestCode, resultCode, data);
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
    protected void onDestroy() {
        Runtime.getRuntime().gc();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            closeDrawer();
        } else if (getSupportFragmentManager().getBackStackEntryCount()>0) {
            super.onBackPressed();
        } else {
            NearbyPlacesFragment frag = (NearbyPlacesFragment)
                    getSupportFragmentManager().findFragmentByTag(NearbyPlacesFragment.TAG);
            if (frag==null || !frag.isVisible()) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content_frame,
                                NearbyPlacesFragment.newInstance(), NearbyPlacesFragment.TAG).commit();
                mNavigationView.setCheckedItem(R.id.nav_nearby_places);
            } else {
                finish();
                super.onBackPressed();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (item.isChecked()) {
            closeDrawer();
            return true;
        }
        EventBus.getDefault().post(new NavigationItemClickedEvent());
        item.setChecked(true);
        if (mPreviousMenuItem != null) {
            mPreviousMenuItem.setChecked(false);
        }
        mPreviousMenuItem = item;
        int id = item.getItemId();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction();
        if (id == R.id.nav_nearby_places) {
            fragmentTransaction
                    .replace(R.id.content_frame,
                            NearbyPlacesFragment.newInstance(), NearbyPlacesFragment.TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        } else if (id == R.id.nav_fav_places) {
            fragmentTransaction
                    .replace(R.id.content_frame,
                            FavouritePlacesFragment.newInstance(), FavouritePlacesFragment.TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        } else if (id == R.id.nav_find_service) {
            fragmentTransaction
                    .replace(R.id.content_frame,
                            ServicesFragment.newInstance(), ServicesFragment.TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        } else if (id == R.id.nav_profile) {
            fragmentTransaction
                    .replace(R.id.content_frame,
                            ProfileFragment.newInstance(), ProfileFragment.TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        } else if (id == R.id.nav_gallery) {
            fragmentTransaction
                    .replace(R.id.content_frame,
                            GalleryGridFragment.newInstance(), GalleryGridFragment.TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        } else if (id == R.id.nav_logout) {
            mUserManager.logoutUser();
        }
        closeDrawer();
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mNavigateUpFromChild){
            mNavigateUpFromChild = false;
        } else {
            startLocationUpdates();
            if (mCurrentLocation != null) {
                getNearbyRestaurants();
            }
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
        } else if (mUserManager.getNearbyResponse(mIsRestaurant).isEmpty()) {
            getNearbyRestaurants();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        EventBus.getDefault().post(new ConfigurationChangedEvent());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the mPlacesDetails arrays are empty.
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
                    changeProfilePic();
                } else {

                }
                return;
            }
        }
    }

    /**
     * Change profile pic in navigation drawer when profile pic in ProfileFragment is changed.
     * Overrides the onProfilePicChanged method in the
     * OnProfilePicChangedListener interface declared in ProfileFragment.
     */
    @Override
    public void onProfilePicChanged() {
        displayProfilePic();
    }

    /**
     * Method to change the user's profile picture.
     */
    @OnClick(R.id.nav_hdr_profile_image)
    void changeProfilePic() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_READ_STORAGE);
        } else {
            startActivityForResult(new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
        }
    }

    @Subscribe
    public void OnProfileImageChanged(ProfileImageUpdatedEvent event) {
        displayProfilePic();
    }

    /**
     * Method to close the navigation drawer.
     */
    private void closeDrawer() {
        mDrawer.closeDrawer(GravityCompat.START);
    }

    /**
     * Method to set the view items on screen.
     */
    private void setViewItems() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_layout);
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = mNavigationView.inflateHeaderView(R.layout.nav_header);
        ButterKnife.bind(this, headerView);
        mUserManager = new UserManager(getApplicationContext());
        setSupportActionBar(toolbar);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
        mNavigationView.setCheckedItem(R.id.nav_nearby_places);
        mNavigationView.setNavigationItemSelectedListener(this);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame,
                        NearbyPlacesFragment.newInstance(), NearbyPlacesFragment.TAG).commit();
        mUserEmail = mUserManager.getUserEmail();
        HashMap<String, String> user = mUserManager.getUserDetails();
        String name = user.get(UserManager.KEY_FIRST_NAME) + " " + user.get(UserManager.KEY_LAST_NAME);
        tvUser.setText(name);
        tvEmail.setText(mUserEmail);
        displayProfilePic();
    }

    /**
     * Method to request current location of the user.
     */
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, locationPermissions,
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
        try {
            String[] loc = mUserManager.getLocation();
            mLastLocation.setLatitude(Double.parseDouble(loc[0]));
            mLastLocation.setLongitude(Double.parseDouble(loc[1]));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to generate the Places API url and request for nearby services.
     */
    public void getNearbyRestaurants() {
        mLastLocation = mCurrentLocation;
        String lat = String.valueOf(mCurrentLocation.getLatitude());
        String lng = String.valueOf(mCurrentLocation.getLongitude());
        mUserManager.updateLocation(lat, lng);
        String serviceType = getString(R.string.restaurant);
        String location = lat + "," + lng;
        String searchValue = null;
        String radius = mUserManager.getSearchRadius();
        ApiInterface service = mRetrofit.create(ApiInterface.class);
        mCallback = service.getNearbyServices(mGooglePlacesWebKey, serviceType, searchValue,
                        location, radius);
        mCallback.enqueue(startBackgroundThread);
        mCallback = service.getNearbyServices(mGooglePlacesWebKey, UserManager.serviceType, UserManager.searchValue,
                location, radius);
        mCallback.enqueue(startServiceThread);
    }

    Callback<GetFromJson> startBackgroundThread = new Callback<GetFromJson>() {
        @Override
        public void onResponse(Call<GetFromJson> call, Response<GetFromJson> response) {
            Gson gson = new Gson();
            String apiResult = gson.toJson(response.body());
            mUserManager.updateNearbyResponse(true, apiResult);
            EventBus.getDefault().post(new LocationUpdatedEvent(null));
        }

        @Override
        public void onFailure(Call<GetFromJson> call, Throwable t) {
            Log.e(TAG_ERROR, t.toString());
        }
    };

    Callback<GetFromJson> startServiceThread = new Callback<GetFromJson>() {
        @Override
        public void onResponse(Call<GetFromJson> call, Response<GetFromJson> response) {
            Gson gson = new Gson();
            String apiResult = gson.toJson(response.body());
            mUserManager.updateNearbyResponse(false, apiResult);
            EventBus.getDefault().post(new LocationUpdatedEvent(null));
        }

        @Override
        public void onFailure(Call<GetFromJson> call, Throwable t) {
            Log.e(TAG_ERROR, t.toString());
        }
    };

    /**
     * Method to display user's profile image.
     */
    public void displayProfilePic() {
        Uri profileImage;
        try {
            profileImage = mUserManager.getProfilePic(mUserEmail);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_READ_STORAGE);
            }
            Picasso.with(this)
                    .load(profileImage)
                    .fit()
                    .centerCrop()
                    .into(imageViewProfile);
        } catch (NullPointerException e) {
            Log.e(TAG_ERROR, e.toString());
        }
    }

    /**
     * Change the profile image in ProfileFragment when profile image is
     * changed from navigation drawer item.
     */
    private void changeImageInProfileFragment() {
        try {
            ProfileFragment pf = (ProfileFragment) getSupportFragmentManager().findFragmentByTag(ProfileFragment.TAG);
            Boolean vis = pf.isVisible();
            if (vis) {
                pf.displayProfilePic();
            }
        } catch (NullPointerException e) {
            Log.i(TAG_INFO, "ProfileFragment is not loaded");
        }
    }

    /**
     * To show an alert message to enable GPS.
     */
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        finish();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}
