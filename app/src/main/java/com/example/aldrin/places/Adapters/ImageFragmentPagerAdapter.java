package com.example.aldrin.places.Adapters;

import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.LruCache;

import com.example.aldrin.places.Fragments.SwipeFragment;
import com.example.aldrin.places.PlacesDetailsJsonClasses.Result;

/**
 * Created by aldrin on 26/8/16.
 */

public class ImageFragmentPagerAdapter extends FragmentPagerAdapter {
    int imageCount;
    private Result mPlacesDetails;
    private LruCache<String, Bitmap> mMemoryCache;
    public ImageFragmentPagerAdapter(FragmentManager fm, int count, Result result, LruCache<String, Bitmap> memoryCache) {
        super(fm);
        imageCount = count;
        mPlacesDetails = result;
        mMemoryCache = memoryCache;
    }

    @Override
    public int getCount() {
        return imageCount;
    }

    @Override
    public Fragment getItem(int position) {
        return SwipeFragment.newInstance(position, mPlacesDetails, mMemoryCache);
    }
}