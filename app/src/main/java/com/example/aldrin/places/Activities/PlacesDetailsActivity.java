package com.example.aldrin.places.Activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.aldrin.places.CustomClasses.GetImageBitmapFromUrl;
import com.example.aldrin.places.CustomClasses.GetPlacesDetails;
import com.example.aldrin.places.PlacesDetailsJsonClasses.GetFromJson;
import com.example.aldrin.places.PlacesDetailsJsonClasses.Result;
import com.example.aldrin.places.R;
import com.google.gson.Gson;

public class PlacesDetailsActivity extends AppCompatActivity implements GetPlacesDetails.AsyncResponse {

    private static int NUM_IMAGES;
    private GetPlacesDetails getPlacesDetails;
    private static GetImageBitmapFromUrl getImageBitmap;
    ImageFragmentPagerAdapter imageFragmentPagerAdapter;
    ViewPager viewPager;
    private static Result result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_details);
        String placeId = getIntent().getStringExtra("place_id");
        getPlacesDetails = new GetPlacesDetails(this, placeId);
        getImageBitmap = new GetImageBitmapFromUrl();
        getPlacesDetails.delegate = this;
        getPlacesDetails.execute();
    }

    @Override
    public void displayProcessDetails(String output) {
        Gson gson = new Gson();
        GetFromJson response = gson.fromJson(output, GetFromJson.class);
        result = response.getResult();
        NUM_IMAGES = result.getPhotos().size();
        imageFragmentPagerAdapter = new ImageFragmentPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.image_view_pager);
        viewPager.setAdapter(imageFragmentPagerAdapter);
    }

    public static class ImageFragmentPagerAdapter extends FragmentPagerAdapter {
        public ImageFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_IMAGES;
        }

        @Override
        public Fragment getItem(int position) {
            SwipeFragment fragment = new SwipeFragment();
            return SwipeFragment.newInstance(position);
        }
    }

    public static class SwipeFragment extends Fragment implements GetImageBitmapFromUrl.ImageResponse {
        public ImageView imgRestaurant;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View swipeView = inflater.inflate(R.layout.image_view_layout, container, false);

            imgRestaurant = (ImageView) swipeView.findViewById(R.id.venue_image);
            Bundle bundle = getArguments();
            int position = bundle.getInt("position");
            String imageReference = result.getPhotos().get(position).getPhoto_reference();
            String imageUrl = String.format(getString(R.string.image_url), imageReference);
            Log.i("url", imageUrl);
            getImageBitmap = new GetImageBitmapFromUrl();
            getImageBitmap.mImageResponse = this;
            getImageBitmap.execute(imageUrl);
            return swipeView;
        }

        static SwipeFragment newInstance(int position) {
            SwipeFragment swipeFragment = new SwipeFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            swipeFragment.setArguments(bundle);
            return swipeFragment;
        }

        @Override
        public void loadImage(Bitmap output) {
            imgRestaurant.setImageBitmap(output);
        }
    }
}