package com.example.aldrin.places.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.aldrin.places.ui.fragments.GridFragment;
import com.example.aldrin.places.ui.fragments.ListFragment;
import com.example.aldrin.places.ui.fragments.MapFragment;

public class PagerAdapter extends FragmentStatePagerAdapter {

    private int mTabCount;
    private String mTabTitles[] = new String[]{"MAP", "LIST", "GRID"};

    public PagerAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.mTabCount = tabCount;
    }

    /**
     * Returns the fragment to display on changing tabs.
     * @param position
     * @return tab fragment
     */
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                MapFragment tab1 = new MapFragment();
                return tab1;
            case 1:
                Boolean isRestaurant = true;
                Boolean isGrid = false;
                Bundle bundle = new Bundle();
                bundle.putBoolean("isRestaurant", isRestaurant);
                bundle.putBoolean("isGrid", isGrid);
                ListFragment tab2 = new ListFragment();
                tab2.setArguments(bundle);
                return tab2;
            case 2:
                isRestaurant = true;
                isGrid = true;
                bundle = new Bundle();
                bundle.putBoolean("isRestaurant", isRestaurant);
                bundle.putBoolean("isGrid", isGrid);
                ListFragment tab3 = new ListFragment();
                tab3.setArguments(bundle);
                return tab3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mTabCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabTitles[position];
    }
}