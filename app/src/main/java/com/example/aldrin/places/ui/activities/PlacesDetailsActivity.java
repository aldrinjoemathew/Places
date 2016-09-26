package com.example.aldrin.places.ui.activities;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.aldrin.places.R;
import com.example.aldrin.places.adapters.ReviewAdapter;
import com.example.aldrin.places.helpers.InternalStorage;
import com.example.aldrin.places.helpers.UserManager;
import com.example.aldrin.places.interfaces.ApiInterface;
import com.example.aldrin.places.models.placesdetails.GetFromJson;
import com.example.aldrin.places.models.placesdetails.Result;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PlacesDetailsActivity extends AppCompatActivity{

    private final String TAG_ERROR = "error";
    private InternalStorage mFileStorage = new InternalStorage();
    private Result mPlacesDetails;
    private UserManager mUserManager;
    private String mUserEmail;
    private ViewStub vsPlacesDetailsContent;
    private View mView;
    private Toolbar mToolbar;
    private Drawable mDivider;
    private Boolean mIsFavorite = false;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Call<GetFromJson> mCallback;
    private Gson gson = new Gson();
    private Animation mFavAnimation;
    private WeakReference<ImageView> imageViewWeakReference;
    private WeakReference<RecyclerView.Adapter> recyclerViewWeakReference;

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
    @BindView(R.id.iv_venue)
    ImageView ivRestaurant;
    @BindView(R.id.iv_add_favorite_image)
    ImageView ivAddFavoriteImage;
    @BindView(R.id.recycler_view_review)
    RecyclerView mRecyclerView;
    @BindView(R.id.tv_review_not_available)
    TextView tvNoReviews;

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
        mUserEmail = mUserManager.getUserEmail();
        mPlacesDetailsProgress = (ProgressBar) findViewById(R.id.progressbar_places_details);
        String placeId = getIntent().getStringExtra("place_id");
        String mGooglePlacesWebKey = getString(R.string.google_places_web_key);
        String mGoogleApiBaseUrl = getString(R.string.google_api_base_url);
        Retrofit retrofit = new Retrofit
                .Builder()
                .baseUrl(mGoogleApiBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiInterface service = retrofit.create(ApiInterface.class);
        mCallback = service.getPlaceDetails(mGooglePlacesWebKey, placeId);
        mCallback.enqueue(startBackgroundThread);
        mPlacesDetailsProgress.setVisibility(View.VISIBLE);
        mFavAnimation = AnimationUtils.loadAnimation(this, R.anim.favorite);
        imageViewWeakReference = new WeakReference<ImageView>(ivRestaurant);
        recyclerViewWeakReference = new WeakReference<RecyclerView.Adapter>(mAdapter);
    }

    @Override
    public void onBackPressed() {
        mCallback.cancel();
        Picasso.with(this)
                .cancelRequest(ivRestaurant);
        Intent intent = new Intent();
        try {
            if (mIsFavorite != mUserManager.checkFavorite(mUserManager.getUserEmail(),
                    mPlacesDetails.getPlace_id())) {
                intent.putExtra("valueChanged",true);
                intent.putExtra("pos", getIntent().getIntExtra("pos", 0));
            }
        } catch (NullPointerException e) {
        }
        if (getParent() == null) {
            setResult(RESULT_OK, intent);
        } else {
            getParent().setResult(RESULT_OK, intent);
        }
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        Runtime.getRuntime().gc();
        super.onDestroy();
    }

    Callback<GetFromJson> startBackgroundThread = new Callback<GetFromJson>() {
        @Override
        public void onResponse(Call<GetFromJson> call, Response<GetFromJson> response) {
            Gson gson = new Gson();
            String apiResult = gson.toJson(response.body());
            displayProcessDetails(apiResult);
        }
        @Override
        public void onFailure(Call<GetFromJson> call, Throwable t) {
            Log.e(TAG_ERROR, t.toString());
        }
    };

    @OnClick(R.id.iv_venue)
    public void addToFavorite() {
        try {
            String photoRef = mPlacesDetails.getPhotos().get(0).getPhoto_reference();
            ivAddFavoriteImage.setVisibility(View.VISIBLE);
            ivAddFavoriteImage.startAnimation(mFavAnimation);
            ivAddFavoriteImage.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            ivAddFavoriteImage.setVisibility(View.GONE);
            mUserManager.addFavoriteImage(photoRef);
        } catch (NullPointerException e) {
            Log.d(TAG_ERROR, "No image available");
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
        ivAddFavorite.startAnimation(mFavAnimation);
        if (mUserManager.checkFavorite(mUserEmail, placeId)) {
            mUserManager.removeFavorite(mUserEmail, placeId);
            ivAddFavorite.clearColorFilter();
            mFileStorage.removeFromSdCard(this, getString(R.string.favorites_path,mUserEmail),placeId);
        } else {
            mUserManager.addFavorite(mUserEmail, placeId);
            ivAddFavorite.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
            String placeDetails = gson.toJson(mPlacesDetails);
            mFileStorage.addToSdCard(this, "Favorites/" + mUserEmail, placeId, placeDetails);
        }
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
     * Display place details.
     * @param output
     */
    public void displayProcessDetails(String output) {
        mPlacesDetailsProgress.setVisibility(View.GONE);
        vsPlacesDetailsContent = (ViewStub) findViewById(R.id.content_places_details);
        mView = vsPlacesDetailsContent.inflate();
        ButterKnife.bind(this, mView);
        Gson gson = new Gson();
        GetFromJson response = gson.fromJson(output, GetFromJson.class);
        mPlacesDetails = response.getResult();
        try{
            String photoRef = mPlacesDetails.getPhotos().get(0).getPhoto_reference();
            String imageUrl = String.format(getString(R.string.image_url), photoRef);
            Picasso.with(this)
                    .load(imageUrl)
                    .fit()
                    .centerCrop()
                    .into(ivRestaurant);
        } catch (NullPointerException e) {
            mImageProgressBar.setVisibility(View.GONE);
            ivRestaurant.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                    R.drawable.image_no_image_available));
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
            Log.d("out of memory error", "errror "+error.getMessage());
        }
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
            tvWeekdayText.setText(R.string.weekday_info_not_available);
        }
        try {
            ratingVenue.setRating(mPlacesDetails.getRating());
        } catch (NullPointerException e) {
            Log.i(TAG_ERROR, getString(R.string.rating_not_available));
        }
        displayReviews();
    }


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
        } else {
            tvNoReviews.setVisibility(View.VISIBLE);
        }
    }

     /**
     * To find the distance from current position.
     * @return
     */
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