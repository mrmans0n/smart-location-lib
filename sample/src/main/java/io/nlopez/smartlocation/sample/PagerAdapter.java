package io.nlopez.smartlocation.sample;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import io.nlopez.smartlocation.utils.Nulls;

public class PagerAdapter extends FragmentPagerAdapter {

    private LocationFragment mLocationFragment;
    private GeocodingFragment mGeocodingFragment;
    private GeofencingFragment mGeofencingFragment;

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                mLocationFragment = Nulls.orDefault(mLocationFragment, new LocationFragment());
                return mLocationFragment;
            case 1:
                mGeocodingFragment = Nulls.orDefault(mGeocodingFragment, new GeocodingFragment());
                return mGeocodingFragment;
            case 2:
                mGeofencingFragment = Nulls.orDefault(mGeofencingFragment, new GeofencingFragment());
                return mGeofencingFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Location";
            case 1:
                return "Geocoding";
            case 2:
                return "Geofencing";
            default:
                return "Not implemented";
        }
    }
}
