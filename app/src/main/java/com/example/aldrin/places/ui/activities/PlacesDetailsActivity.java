package com.example.aldrin.places.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.aldrin.places.R;
import com.example.aldrin.places.adapters.ReviewAdapter;
import com.example.aldrin.places.helpers.GetImageBitmapFromUrl;
import com.example.aldrin.places.helpers.GetPlacesDetails;
import com.example.aldrin.places.helpers.UserManager;
import com.example.aldrin.places.models.placesdetails.GetFromJson;
import com.example.aldrin.places.models.placesdetails.Result;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PlacesDetailsActivity extends AppCompatActivity
        implements GetPlacesDetails.AsyncResponse,
        GetImageBitmapFromUrl.ImageResponse{

    private static final String SD_PATH = Environment.getExternalStorageDirectory().getPath();
    private static final String TAG_ERROR = "error";
    private GetPlacesDetails getPlacesDetails;
    private GetImageBitmapFromUrl getImageBitmap;
    private Result mPlacesDetails;
    private LruCache<String, Bitmap> mMemoryCache;
    private UserManager mUserManager;
    private ViewStub vsPlacesDetailsContent;
    private View mView;
    private Toolbar mToolbar;
    private Drawable mDivider;
    private Boolean mIsFavorite = false;

    @BindView(R.id.my_recycler_view)
    RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_details);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        mToolbar.setNavigationOnClickListener(navigateUp);
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
        String placeId = mPlacesDetails.getPlace_id();
        if (mUserManager.checkFavorite(mUserManager.getUserEmail(), placeId)) {
            mIsFavorite = true;
            ivAddFavorite.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
        }
        mToolbar.setTitle(restTitle);
        tvRestaurantTitle.setText(restTitle);
        tvRestaurantAddress.setText(restAddress);
        tvDistance.setText("~ " + distance + " km");
        if (restPhoneNumber != null) {
            tvPhoneNumber.setText(restPhoneNumber);
        } else {
            mLayoutPhoneNumber.setClickable(false);
        }
        if (restWebUrl != null) {
            tvWebsiteAddress.setText(restWebUrl);
        } else {
            mLayoutWebAddress.setClickable(false);
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
        displayReviews();
    }

    @Override
    public void onBackPressed() {
        if (mIsFavorite != mUserManager.checkFavorite(mUserManager.getUserEmail(),
                mPlacesDetails.getPlace_id())) {
            Intent intent = new Intent();
            intent.putExtra("valueChanged",true);
            if (getParent() == null) {
                setResult(1, intent);
            }
            else {
                getParent().setResult(1, intent);
            }
        }
        super.onBackPressed();
    }

    /**
     * On navigation up arrow clicked.
     */
    View.OnClickListener navigateUp = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onBackPressed();
        }
    };

    /**
     * Displays reviews in a recycler view.
     */
    private void displayReviews() {
        if (mPlacesDetails.getReviews() != null) {
            mRecyclerView.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(this);
            mAdapter = new ReviewAdapter(mPlacesDetails.getReviews());
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                    final int left = parent.getPaddingLeft();
                    final int right = parent.getWidth() - parent.getPaddingRight();
                    final TypedArray a = obtainStyledAttributes(new int[]{
                            android.R.attr.listDivider});
                    mDivider = a.getDrawable(0);
                    final int childCount = parent.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        final View child = parent.getChildAt(i);
                        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                                .getLayoutParams();
                        final int top = child.getBottom() + params.bottomMargin;
                        final int bottom = top + mDivider.getIntrinsicHeight();
                        mDivider.setBounds(left, top, right, bottom);
                        mDivider.draw(c);
                    }
                }
            });
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @OnClick(R.id.layout_diretion)
    public void showDirections() {
        com.example.aldrin.places.models.placesdetails.Location location =
                mPlacesDetails.getGeometry().getLocation();
        String loc[] = mUserManager.getLocation();
        final Intent intent = new
                Intent(Intent.ACTION_VIEW,Uri.parse("http://maps.google.com/maps?" +
                "saddr="+ loc[0] + "," + loc[1] + "&daddr=" + location.getLat() + "," +
                location.getLng()));
        intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
        startActivity(intent);
    }

    @OnClick(R.id.layout_web_address)
    public void goToWebsite() {
        Intent viewIntent = new Intent("android.intent.action.VIEW",
                Uri.parse(mPlacesDetails.getWebsite()));
        startActivity(viewIntent);
    }

    @OnClick(R.id.layout_phone_number)
    public void makecall(){
        String phoneNumber = mPlacesDetails.getFormatted_phone_number();
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    @OnClick(R.id.iv_add_favorite)
    public void addOrRemoveFavorite() {
        String placeId = mPlacesDetails.getPlace_id();
        if (mUserManager.checkFavorite(mUserManager.getUserEmail(), placeId)) {
            mUserManager.removeFavorite(mUserManager.getUserEmail(), placeId);
            ivAddFavorite.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            removeFromSdCard(placeId);
        } else {
            mUserManager.addFavorite(mUserManager.getUserEmail(), placeId);
            ivAddFavorite.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
            addToSdCard(placeId);
        }
    }

    private void addToSdCard(String placeId) {
        try {
            Gson gson = new Gson();
            String placeDetails = gson.toJson(mPlacesDetails);
            File favDir = new File(SD_PATH, "Favorites");
            favDir.mkdirs();
            File myFile = new File(favDir, placeId);
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter =
                    new OutputStreamWriter(fOut);
            myOutWriter.append(placeDetails);
            myOutWriter.close();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeFromSdCard(String placeId) {
        File favDir = new File(SD_PATH, "Favorites");
        favDir.mkdirs();
        File myFile = new File(favDir, placeId);
        myFile.delete();
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