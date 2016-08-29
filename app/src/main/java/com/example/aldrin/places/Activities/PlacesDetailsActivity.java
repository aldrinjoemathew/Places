package com.example.aldrin.places.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.aldrin.places.AccountManagement.UserManager;
import com.example.aldrin.places.Adapters.ImageFragmentPagerAdapter;
import com.example.aldrin.places.CustomClasses.GetImageBitmapFromUrl;
import com.example.aldrin.places.CustomClasses.GetPlacesDetails;
import com.example.aldrin.places.PlacesDetailsJsonClasses.GetFromJson;
import com.example.aldrin.places.PlacesDetailsJsonClasses.Result;
import com.example.aldrin.places.R;
import com.google.gson.Gson;

import java.text.DecimalFormat;

public class PlacesDetailsActivity extends AppCompatActivity
        implements GetPlacesDetails.AsyncResponse,
        GetImageBitmapFromUrl.ImageResponse{

    private int NUM_IMAGES;
    private GetPlacesDetails getPlacesDetails;
    private GetImageBitmapFromUrl getImageBitmap;
    private ImageFragmentPagerAdapter imageFragmentPagerAdapter;
    private ViewPager viewPager;
    private Result mPlacesDetails;
    private LruCache<String, Bitmap> mMemoryCache;
    private ImageView imgImageNotAvailable;
    private ImageView imgMakeCall;
    private TextView tvRestaurantTitle;
    private TextView tvRestaurantAddress;
    private TextView tvDistance;
    private TextView tvPhoneNumber;
    public ImageView imgRestaurant;
    private ProgressBar mPlacesDetailsProgress;
    private ProgressBar mImageProgressBar;
    private UserManager mUserManager;
    private ViewStub vsPlacesDetailsContent;
    private View mView;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_details);

        mUserManager = new UserManager(getApplicationContext());
        mPlacesDetailsProgress = (ProgressBar) findViewById(R.id.places_details_progress);
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        String placeId = getIntent().getStringExtra("place_id");
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
        getPlacesDetails = new GetPlacesDetails(this, placeId);
        getPlacesDetails.delegate = this;
        getPlacesDetails.execute();
        mPlacesDetailsProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void displayProcessDetails(String output) {
        mPlacesDetailsProgress.setVisibility(View.GONE);
        vsPlacesDetailsContent = (ViewStub) findViewById(R.id.places_details_content);
        mView = vsPlacesDetailsContent.inflate();
        initiateViewItems();
        Gson gson = new Gson();
        GetFromJson response = gson.fromJson(output, GetFromJson.class);
        mPlacesDetails = response.getResult();
        String photoRef = mPlacesDetails.getPhotos().get(0).getPhoto_reference();
        String imageUrl = String.format(getString(R.string.image_url), photoRef);
        loadBitmap(imageUrl);
        String restTitle = mPlacesDetails.getName();
        String restAddress = mPlacesDetails.getFormatted_address();
        String restPhoneNumber = mPlacesDetails.getInternational_phone_number();
        String distance = distanceFromCurrentPosition();
        tvRestaurantTitle.setText(restTitle);
        tvRestaurantAddress.setText(restAddress);
        tvPhoneNumber.setText(restPhoneNumber);
        tvDistance.setText(distance);
    }

    ImageView.OnClickListener makeCall = new ImageView.OnClickListener() {
        @Override
        public void onClick(View view) {
            String phoneNumber = mPlacesDetails.getFormatted_phone_number();
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        }
    };

    void initiateViewItems() {
        imgRestaurant = (ImageView) mView.findViewById(R.id.venue_image);
        tvRestaurantTitle = (TextView) mView.findViewById(R.id.restaurant_title);
        tvRestaurantAddress = (TextView) mView.findViewById(R.id.tv_address);
        tvDistance = (TextView) mView.findViewById(R.id.tv_distance);
        tvPhoneNumber = (TextView) mView.findViewById(R.id.tv_phone_number);
        mImageProgressBar = (ProgressBar) mView.findViewById(R.id.image_progress);
    }

    @Override
    public void loadImage(String imageKey, Bitmap imageBitmap) {
        imgRestaurant.setImageBitmap(imageBitmap);
        addBitmapToMemoryCache(imageKey, imageBitmap);
    }

    /**
     * Method retrieve bitmap from cache if present.
     * Otherwise executes a background task inorder to load the image from a remote URL.
     * @param url
     */
    public void loadBitmap(String url) {
        final String imageKey = url;

        final Bitmap bitmap = getBitmapFromMemCache(imageKey);
        if (bitmap != null) {
            loadImage(imageKey, bitmap);
            mImageProgressBar.setVisibility(View.GONE);
        } else {
            Log.i("url", url);
            getImageBitmap = new GetImageBitmapFromUrl(mImageProgressBar);
            getImageBitmap.mImageResponse = this;
            getImageBitmap.execute(url);
        }
    }

    /**
     * Method to add a bitmap image to cache if available.
     * @param key
     * @param bitmap
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    /**
     * Method to get the bitmap of image stored in the cache storage.
     * Returns null if bitmap is not available in the cache.
     * @param key
     * @return imageBitmap
     */
    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    private String distanceFromCurrentPosition() {
        Location userLocation = new Location("user_location");
        Location venueLocation = new Location("venue_location");
        String loc[] = mUserManager.getLocation();
        userLocation.setLatitude(Double.parseDouble(loc[0]));
        userLocation.setLongitude(Double.parseDouble(loc[1]));
        venueLocation.setLatitude(mPlacesDetails.getGeometry().getLocation().getLat());
        venueLocation.setLongitude(mPlacesDetails.getGeometry().getLocation().getLng());
        DecimalFormat df = new DecimalFormat("#.####");
        Float distance = userLocation.distanceTo(venueLocation); //distance in meter
        return String.valueOf(Double.parseDouble(df.format(distance/1000))); //distance in km
    }
}