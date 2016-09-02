package com.example.aldrin.places.ui.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.aldrin.places.R;
import com.example.aldrin.places.adapters.PagerAdapter;

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
        return inflater.inflate(R.layout.fragment_nearby_places, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        TabLayout mTabLayout = (TabLayout) view.findViewById(R.id.tabLayout);
        int tabCount = getResources().getInteger(R.integer.tab_count);
        mSectionsPagerAdapter = new PagerAdapter(
                getChildFragmentManager(), tabCount);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mViewPager.setCurrentItem(1);
    }
}
