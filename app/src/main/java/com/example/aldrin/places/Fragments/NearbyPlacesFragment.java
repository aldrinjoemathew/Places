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
 * This fragment holds a tab layout.
 * This fragment shows 3 tabs.
 *  Map view
 *  List view
 *  Grid view
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
        mViewPager = (ViewPager) v.findViewById(R.id.pager);
        TabLayout mTabLayout = (TabLayout) v.findViewById(R.id.tabLayout);
        mSectionsPagerAdapter = new PagerAdapter(
                getChildFragmentManager(), 3);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        return v;
    }
}
