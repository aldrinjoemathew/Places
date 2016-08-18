package com.example.aldrin.places.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.aldrin.places.Fragments.GridFragment;
import com.example.aldrin.places.Fragments.ListFragment;
import com.example.aldrin.places.Fragments.MapFragment;

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
                ListFragment tab2 = new ListFragment();
                return tab2;
            case 2:
                GridFragment tab3 = new GridFragment();
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