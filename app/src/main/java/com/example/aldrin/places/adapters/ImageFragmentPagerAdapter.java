package com.example.aldrin.places.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.aldrin.places.ui.fragments.SwipeFragment;

import java.io.Serializable;
import java.util.List;

/**
 * Created by aldrin on 26/8/16.
 */

public class ImageFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private int imageCount;
    private List<String> mImageRefs;

    public ImageFragmentPagerAdapter(FragmentManager fm, List<String> imageRefs) {
        super(fm);
        imageCount = imageRefs.size();
        mImageRefs = imageRefs;
    }

    @Override
    public int getCount() {
        return imageCount;
    }

    @Override
    public Fragment getItem(int position) {
        SwipeFragment fr = new SwipeFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putSerializable("imagerefs", (Serializable) mImageRefs);
        fr.setArguments(bundle);
        return fr;
    }
}