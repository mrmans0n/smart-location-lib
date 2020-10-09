package io.nlopez.smartlocation.geocoding.common;

import android.location.Address;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

/**
 * Wrapper for addresses for when what we really want most of the time is the Location class instead (ie in a geocoding operation).
 */
public class LocationAddress implements Parcelable {
    private final Location mLocation;
    private final Address mAddress;

    public LocationAddress(@NonNull Address address) {
        mAddress = address;
        mLocation = new Location(LocationAddress.class.getSimpleName());
        mLocation.setLatitude(address.getLatitude());
        mLocation.setLongitude(address.getLongitude());
    }

    public LocationAddress(@NonNull Parcel in) {
        mLocation = in.readParcelable(Location.class.getClassLoader());
        mAddress = in.readParcelable(Address.class.getClassLoader());
    }

    public Location getLocation() {
        return mLocation;
    }

    public Address getAddress() {
        return mAddress;
    }

    public String getFormattedAddress() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i <= mAddress.getMaxAddressLineIndex(); i++) {
            builder.append(mAddress.getAddressLine(i));
        }
        return builder.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mLocation, flags);
        dest.writeParcelable(this.mAddress, flags);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public LocationAddress createFromParcel(Parcel in) {
            return new LocationAddress(in);
        }

        public LocationAddress[] newArray(int size) {
            return new LocationAddress[size];
        }
    };
}
