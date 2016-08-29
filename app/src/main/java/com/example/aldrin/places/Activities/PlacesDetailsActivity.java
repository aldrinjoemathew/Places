package com.example.aldrin.places.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.aldrin.places.Adapters.ImageFragmentPagerAdapter;
import com.example.aldrin.places.CustomClasses.GetPlacesDetails;
import com.example.aldrin.places.PlacesDetailsJsonClasses.GetFromJson;
import com.example.aldrin.places.PlacesDetailsJsonClasses.Result;
import com.example.aldrin.places.R;
import com.google.gson.Gson;

public class PlacesDetailsActivity extends AppCompatActivity implements GetPlacesDetails.AsyncResponse {

    private int NUM_IMAGES;
    private GetPlacesDetails getPlacesDetails;
    private ImageFragmentPagerAdapter imageFragmentPagerAdapter;
    private ViewPager viewPager;
    private Result mPlacesDetails;
    private LruCache<String, Bitmap> mMemoryCache;
    private ImageView imgImageNotAvailable;
    private ImageView imgMakeCall;
    private TextView tvRestaurantTitle;
    private TextView tvRestaurantAddress;
    private TextView tvPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_details);
        initiateViewItems();
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
    }

    @Override
    public void displayProcessDetails(String output) {
        Gson gson = new Gson();
        GetFromJson response = gson.fromJson(output, GetFromJson.class);
        mPlacesDetails = response.getResult();
        try {
            NUM_IMAGES = mPlacesDetails.getPhotos().size();
            imageFragmentPagerAdapter = new ImageFragmentPagerAdapter(getSupportFragmentManager(),
                    NUM_IMAGES, mPlacesDetails, mMemoryCache);
            viewPager.setAdapter(imageFragmentPagerAdapter);
        } catch (NullPointerException e) {
            imgImageNotAvailable.setVisibility(View.VISIBLE);
        }
        tvRestaurantTitle.setText(mPlacesDetails.getName());
        tvRestaurantAddress.setText(mPlacesDetails.getFormatted_address());
        tvPhoneNumber.setText(mPlacesDetails.getFormatted_phone_number());
        imgMakeCall.setOnClickListener(makeCall);
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
        viewPager = (ViewPager) findViewById(R.id.image_view_pager);
        imgImageNotAvailable = (ImageView) findViewById(R.id.image_not_available);
        tvRestaurantTitle = (TextView) findViewById(R.id.text_view_restaurant_title);
        tvRestaurantAddress = (TextView) findViewById(R.id.text_view_restaurant_address);
        imgMakeCall = (ImageView) findViewById(R.id.image_make_call);
        tvPhoneNumber = (TextView) findViewById(R.id.restaurant_phone_number);
    }

}