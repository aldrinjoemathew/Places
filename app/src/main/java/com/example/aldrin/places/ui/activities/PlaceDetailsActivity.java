package com.example.aldrin.places.ui.activities;

import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.aldrin.places.R;
import com.example.aldrin.places.adapters.ReviewAdapter;
import com.example.aldrin.places.helpers.UserManager;
import com.example.aldrin.places.interfaces.ApiInterface;
import com.example.aldrin.places.models.placesdetails.GetFromJson;
import com.example.aldrin.places.models.placesdetails.Result;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PlaceDetailsActivity extends AppCompatActivity {

    private static final String TAG_ERROR = "error";
    private UserManager mUserManager;
    private Call<GetFromJson> call;
    private Result mPlacesDetails;
    private Drawable mDivider;
    private Boolean mIsFavorite = false;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Gson gson = new Gson();

    @BindView(R.id.tb_details)
    Toolbar mToolbar;
    @BindView(R.id.ctb_layout)
    CollapsingToolbarLayout mToolbarLayout;
    @BindView(R.id.layout_phone_number)
    LinearLayout mLayoutPhoneNumber;
    @BindView(R.id.layout_web_address)
    LinearLayout mLayoutWebAddress;
    @BindView(R.id.layout_diretion)
    LinearLayout mLayoutDirection;
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
    @BindView(R.id.iv_venue)
    ImageView ivRestaurant;
    @BindView(R.id.recycler_view_review)
    RecyclerView mRecyclerView;
    @BindView(R.id.tv_review_not_available)
    TextView tvNoReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        mToolbar.setNavigationOnClickListener(navigateUp);
        mUserManager = new UserManager(getApplicationContext());
        String placeId = getIntent().getStringExtra("place_id");
        String mGooglePlacesWebKey = getString(R.string.google_places_web_key);
        String mGoogleApiBaseUrl = getString(R.string.google_api_base_url);
        Retrofit retrofit = new Retrofit
                .Builder()
                .baseUrl(mGoogleApiBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiInterface service = retrofit.create(ApiInterface.class);
        call = service.getPlaceDetails(mGooglePlacesWebKey, placeId);
        call.enqueue(startBackgroundThread);
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

    Callback<GetFromJson> startBackgroundThread = new Callback<GetFromJson>() {
        @Override
        public void onResponse(Call<GetFromJson> call, Response<GetFromJson> response) {
            String apiResult = gson.toJson(response.body());
            displayProcessDetails(apiResult);
        }
        @Override
        public void onFailure(Call<GetFromJson> call, Throwable t) {
            Log.e(TAG_ERROR, t.toString());
        }
    };

    private void displayProcessDetails(String apiResult) {
        GetFromJson response = gson.fromJson(apiResult, GetFromJson.class);
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
            ivRestaurant.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                    R.drawable.image_no_image_available));
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
        String restTitle = mPlacesDetails.getName();
        mToolbarLayout.setTitle(restTitle);
        mToolbar.setTitle(restTitle);
        String restAddress = mPlacesDetails.getFormatted_address();
        String restPhoneNumber = mPlacesDetails.getInternational_phone_number();
        String restWebUrl = mPlacesDetails.getWebsite();
        String distance = distanceFromCurrentPosition();
        String placeId = mPlacesDetails.getPlace_id();
        if (mUserManager.checkFavorite(mUserManager.getUserEmail(), placeId)) {
            mIsFavorite = true;
        }
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
            int length = mPlacesDetails.getOpening_hours().getWeekday_text().length;
            for (int i=0; i<length; i++) {
                sb.append(mPlacesDetails.getOpening_hours().getWeekday_text()[i] + "\n");
            }
            String weekdayText = sb.toString() + "\b";
            tvWeekdayText.setText(weekdayText);
        } catch (NullPointerException e) {
            tvWeekdayText.setText(R.string.weekday_info_not_available);
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
