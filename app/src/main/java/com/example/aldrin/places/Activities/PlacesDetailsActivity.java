package com.example.aldrin.places.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.aldrin.places.AccountManagement.UserManager;
import com.example.aldrin.places.CustomClasses.GetImageBitmapFromUrl;
import com.example.aldrin.places.CustomClasses.GetPlacesDetails;
import com.example.aldrin.places.PlacesDetailsJsonClasses.GetFromJson;
import com.example.aldrin.places.PlacesDetailsJsonClasses.Result;
import com.example.aldrin.places.R;
import com.google.gson.Gson;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PlacesDetailsActivity extends AppCompatActivity
        implements GetPlacesDetails.AsyncResponse,
        GetImageBitmapFromUrl.ImageResponse{

    private static final String TAG_ERROR = "error";
    private GetPlacesDetails getPlacesDetails;
    private GetImageBitmapFromUrl getImageBitmap;
    private Result mPlacesDetails;
    private LruCache<String, Bitmap> mMemoryCache;

    @BindView(R.id.layout_phone_number)
    LinearLayout mLayoutPhoneNumber;
    @BindView(R.id.layout_web_address)
    LinearLayout mLayoutWebAddress;
    @BindView(R.id.layout_diretion)
    LinearLayout mLayoutDirection;
    @BindView(R.id.restaurant_title)
    TextView tvRestaurantTitle;
    @BindView(R.id.tv_address)
    TextView tvRestaurantAddress;
    @BindView(R.id.tv_distance)
    TextView tvDistance;
    @BindView(R.id.tv_phone_number)
    TextView tvPhoneNumber;
    @BindView(R.id.tv_website_address)
    TextView tvWebsiteAddress;
    @BindView(R.id.tv_weekday_text)
    TextView tvWeekdayText;
    @BindView(R.id.iv_add_favorite)
    ImageView ivAddFavorite;
    ProgressBar mPlacesDetailsProgress;
    @BindView(R.id.image_progress)
    ProgressBar mImageProgressBar;
    @BindView(R.id.ratingbar_venue)
    RatingBar ratingVenue;
    @BindView(R.id.venue_image)
    ImageView imgRestaurant;

    private UserManager mUserManager;
    private ViewStub vsPlacesDetailsContent;
    private View mView;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_details);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.ic_move_backward);
        mUserManager = new UserManager(getApplicationContext());
        mPlacesDetailsProgress = (ProgressBar) findViewById(R.id.progressbar_places_details);
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
        vsPlacesDetailsContent = (ViewStub) findViewById(R.id.content_places_details);
        mView = vsPlacesDetailsContent.inflate();
        ButterKnife.bind(this, mView);
        Gson gson = new Gson();
        GetFromJson response = gson.fromJson(output, GetFromJson.class);
        mPlacesDetails = response.getResult();
        String photoRef = mPlacesDetails.getPhotos().get(0).getPhoto_reference();
        String imageUrl = String.format(getString(R.string.image_url), photoRef);
        loadBitmap(imageUrl);
        String restTitle = mPlacesDetails.getName();
        String restAddress = mPlacesDetails.getFormatted_address();
        String restPhoneNumber = mPlacesDetails.getInternational_phone_number();
        String restWebUrl = mPlacesDetails.getWebsite();
        String distance = distanceFromCurrentPosition();
        mToolbar.setTitle(restTitle);
        tvRestaurantTitle.setText(restTitle);
        tvRestaurantAddress.setText(restAddress);
        tvDistance.setText("~ " + distance + " km");
        mLayoutDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDirections(mPlacesDetails.getGeometry().getLocation());
            }
        });
        try {
            tvPhoneNumber.setText(restPhoneNumber);
        } catch (NullPointerException e) {
            Log.i(TAG_ERROR, "Phone number not available");
        }
        try {
            tvWebsiteAddress.setText(restWebUrl);
            mLayoutWebAddress.setOnClickListener(gotoWebsite);
        } catch (NullPointerException e) {
            Log.i(TAG_ERROR, "Website url not available");
        }
        StringBuilder sb = new StringBuilder();
        try{
            for (int i=0; i<mPlacesDetails.getOpening_hours().getWeekday_text().length; i++) {
                sb.append(mPlacesDetails.getOpening_hours().getWeekday_text()[i] + "\n");
            }
            String weekdayText = sb.toString() + "\b";
            tvWeekdayText.setText(weekdayText);
        } catch (NullPointerException e) {
            tvWeekdayText.setText("Weekday information is not available");
        }
        try {
            ratingVenue.setRating(mPlacesDetails.getRating());
        } catch (NullPointerException e) {
            Log.i(TAG_ERROR, "Rating is not available");
        }
        ivAddFavorite.setOnClickListener(addOrRemoveFavorite);
    }

    LinearLayout.OnClickListener gotoWebsite = new LinearLayout.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent viewIntent = new Intent("android.intent.action.VIEW",
                            Uri.parse(mPlacesDetails.getWebsite()));
            startActivity(viewIntent);
        }
    };

    @OnClick(R.id.layout_phone_number)
    public void makecall(){
        String phoneNumber = mPlacesDetails.getFormatted_phone_number();
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }
    LinearLayout.OnClickListener makeCall = new LinearLayout.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };

    ImageView.OnClickListener addOrRemoveFavorite = new ImageView.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };

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

    public void showDirections(com.example.aldrin.places.PlacesDetailsJsonClasses.Location location) {
        String loc[] = mUserManager.getLocation();
        final Intent intent = new
                Intent(Intent.ACTION_VIEW,Uri.parse("http://maps.google.com/maps?" +
                "saddr="+ loc[0] + "," + loc[1] + "&daddr=" + location.getLat() + "," +
                location.getLng()));
        intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
        startActivity(intent);
    }
}