package com.example.aldrin.places.ui.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.aldrin.places.R;
import com.example.aldrin.places.helpers.GetImageBitmapFromUrl;
import com.example.aldrin.places.models.placesdetails.Result;

/**
 * Created by aldrin on 26/8/16.
 */

/**
 * Class to implement the swipe view when mutiple images of restaurant are available.
 */
public class SwipeFragment extends Fragment implements GetImageBitmapFromUrl.ImageResponse {
    public ImageView imgRestaurant;
    private ProgressBar mProgressBar;
    private GetImageBitmapFromUrl getImageBitmap;
    private static Result mPlacesDetails;
    private static LruCache<String, Bitmap> mMemoryCache;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_image_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imgRestaurant = (ImageView) view.findViewById(R.id.venue_image);
        mProgressBar = (ProgressBar) view.findViewById(R.id.image_progress);
        mProgressBar.setVisibility(View.VISIBLE);
        Bundle bundle = getArguments();
        int position = bundle.getInt("position");
        String imageReference = mPlacesDetails.getPhotos().get(position).getPhoto_reference();
        String imageUrl = String.format(getString(R.string.image_url), imageReference);
        loadBitmap(imageUrl);
    }

    @Override
    public void loadImage(String imageKey, Bitmap imageBitmap) {
        imgRestaurant.setImageBitmap(imageBitmap);
        addBitmapToMemoryCache(imageKey, imageBitmap);
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
     * Method to create a new instance of SwipeFragment class.
     * @param position
     * @param placesDetails
     * @param memoryCache
     * @return
     */
    public static SwipeFragment newInstance(int position, Result placesDetails, LruCache<String, Bitmap> memoryCache) {
        mPlacesDetails = placesDetails;
        mMemoryCache = memoryCache;
        SwipeFragment swipeFragment = new SwipeFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        swipeFragment.setArguments(bundle);
        return swipeFragment;
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
            mProgressBar.setVisibility(View.GONE);
        } else {
            Log.i("url", url);
            getImageBitmap = new GetImageBitmapFromUrl(mProgressBar);
            getImageBitmap.mImageResponse = this;
            getImageBitmap.execute(url);
        }
    }
}