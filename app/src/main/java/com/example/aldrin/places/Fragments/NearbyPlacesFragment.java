package com.example.aldrin.places.Fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.aldrin.places.Adapters.PagerAdapter;
import com.example.aldrin.places.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class NearbyPlacesFragment extends Fragment {

    public static final String TAG = NearbyPlacesFragment.class.getSimpleName();
    PagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    public static NearbyPlacesFragment newInstance() {
        return new NearbyPlacesFragment();
    }

    public NearbyPlacesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_nearby_places, container, false);
        mSectionsPagerAdapter = new PagerAdapter(
                getChildFragmentManager(), 3);
        TabLayout mTabLayout = (TabLayout) v.findViewById(R.id.tabLayout);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        mViewPager = (ViewPager) v.findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        return v;
    }
}
