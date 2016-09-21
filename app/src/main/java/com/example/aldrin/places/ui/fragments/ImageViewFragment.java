package com.example.aldrin.places.ui.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.aldrin.places.R;
import com.example.aldrin.places.adapters.ImageFragmentPagerAdapter;
import com.example.aldrin.places.helpers.UserManager;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImageViewFragment extends Fragment {

    private ImageFragmentPagerAdapter mImageSwipeAdapter;
    private ViewPager mViewPager;
    private UserManager mUserManager;
    private List<String> mUrls;

    public ImageViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getImageUrls();
        int position = getArguments().getInt("pos");
        mImageSwipeAdapter = new ImageFragmentPagerAdapter(getFragmentManager(), mUrls);
        mViewPager = (ViewPager) view.findViewById(R.id.vp_image_view);
        mViewPager.setAdapter(mImageSwipeAdapter);
        mViewPager.setCurrentItem(position);
    }

    private void getImageUrls() {
        mUserManager = new UserManager(getContext());
        String urlString = mUserManager.getFavoriteImages();
        String[] urls = urlString.split("\\s");
        mUrls = new LinkedList<>(Arrays.asList(urls));
        mUrls.remove("null");
        mUrls.remove("");
    }
}
