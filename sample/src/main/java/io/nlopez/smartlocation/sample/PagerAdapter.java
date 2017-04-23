package io.nlopez.smartlocation.sample;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class PagerAdapter extends FragmentPagerAdapter {
    private final LocationFragment mLocationFragment = new LocationFragment();
    private final LocationFragment mLocationFragment2 = new LocationFragment();
    private final LocationFragment mLocationFragment3 = new LocationFragment();

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return mLocationFragment;
            case 1:
                return mLocationFragment2;
            case 2:
                return mLocationFragment3;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
