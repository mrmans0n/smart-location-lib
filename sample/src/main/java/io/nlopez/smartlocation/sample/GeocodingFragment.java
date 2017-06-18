package io.nlopez.smartlocation.sample;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;
import java.util.regex.Pattern;

import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.geocoding.GeocodingUpdatedListener;
import io.nlopez.smartlocation.geocoding.ReverseGeocodingUpdatedListener;
import io.nlopez.smartlocation.geocoding.common.LocationAddress;
import io.nlopez.smartlocation.geocoding.providers.android.AndroidGeocodingProviderFactory;
import io.nlopez.smartlocation.geocoding.providers.googlemaps.GoogleMapsApiGeocodingProviderFactory;

public class GeocodingFragment extends Fragment {

    // Change this for a Google Maps API key if you want to use it
    // Info on how to do this: https://developers.google.com/maps/documentation/geocoding/get-api-key
    private static final String GOOGLE_MAPS_API_KEY = "your-google-maps-api-key";

    private static final Pattern LATITUDE_REGEX =
            Pattern.compile("^(\\+|-)?(?:90(?:(?:\\.0{1,6})?)|(?:[0-9]|[1-8][0-9])(?:(?:\\.[0-9]{1,6})?))$");

    private static final Pattern LONGITUDE_REGEX =
            Pattern.compile("^(\\+|-)?(?:180(?:(?:\\.0{1,6})?)|(?:[0-9]|[1-9][0-9]|1[0-7][0-9])(?:(?:\\.[0-9]{1,6})?))$");

    private EditText mDirectEditText;
    private TextView mDirectResultText;
    private Button mDirectStartButton;
    private EditText mInverseLatitudeText;
    private EditText mInverseLongitudeText;
    private Button mInverseStartButton;
    private TextView mInverseResultText;

    private final View.OnClickListener mDirectSearchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SmartLocation.with(getContext())
                    .geocoding(new AndroidGeocodingProviderFactory(), new GoogleMapsApiGeocodingProviderFactory(GOOGLE_MAPS_API_KEY))
                    .findLocationByName(
                            mDirectEditText.getText().toString(),
                            new GeocodingUpdatedListener.SimpleGeocodingUpdatedListener() {
                                @Override
                                public void onLocationResolved(String name, List<LocationAddress> results) {
                                    if (results.isEmpty()) {
                                        mDirectResultText.setText("None found");
                                    } else {
                                        LocationAddress address = results.get(0);
                                        mDirectResultText.setText(address.getLocation().toString());
                                    }
                                }

                                @Override
                                public void onAllProvidersFailed() {
                                    mDirectResultText.setText("All providers failed");
                                }

                            });
            mDirectResultText.setText("Searching...");
        }
    };

    private final TextWatcher mDirectTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mDirectStartButton.setEnabled(s.length() > 3);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private final View.OnClickListener mInverseSearchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Convert text input to location if possible
            final String latitudeText = mInverseLatitudeText.getText().toString();
            final Double latitude = Double.parseDouble(latitudeText);
            final String longitudeText = mInverseLongitudeText.getText().toString();
            final Double longitude = Double.parseDouble(longitudeText);
            final Location location = new Location("test");
            location.setLatitude(latitude);
            location.setLongitude(longitude);

            // Inverse geocoding retrieval
            SmartLocation.with(getContext())
                    .geocoding(new AndroidGeocodingProviderFactory(), new GoogleMapsApiGeocodingProviderFactory(GOOGLE_MAPS_API_KEY))
                    .findNameByLocation(location, new ReverseGeocodingUpdatedListener.SimpleReverseGeocodingUpdatedListener() {
                        @Override
                        public void onAddressResolved(Location original, List<LocationAddress> results) {
                            if (results.isEmpty()) {
                                mInverseResultText.setText("None found");
                            } else {
                                final LocationAddress address = results.get(0);
                                mInverseResultText.setText(address.getFormattedAddress());
                            }
                        }

                        @Override
                        public void onAllProvidersFailed() {
                            mInverseResultText.setText("All providers failed");
                        }
                    });
            mInverseResultText.setText("Searching...");
        }
    };
    private final TextWatcher mInverseTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            final String latitudeText = mInverseLatitudeText.getText().toString();
            final String longitudeText = mInverseLongitudeText.getText().toString();

            final boolean hasLatitude = !TextUtils.isEmpty(latitudeText)
                    && LATITUDE_REGEX.matcher(latitudeText).matches();
            final boolean hasLongitude = !TextUtils.isEmpty(longitudeText)
                    && LONGITUDE_REGEX.matcher(longitudeText).matches();

            mInverseStartButton.setEnabled(hasLatitude && hasLongitude);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_geocoding, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // Direct geocoding stuff
        mDirectStartButton = (Button) view.findViewById(R.id.direct_button);
        mDirectStartButton.setOnClickListener(mDirectSearchClickListener);
        mDirectEditText = (EditText) view.findViewById(R.id.direct_search);
        mDirectResultText = (TextView) view.findViewById(R.id.direct_result);
        mDirectEditText.addTextChangedListener(mDirectTextWatcher);

        // Inverse geocoding stuff
        mInverseLatitudeText = (EditText) view.findViewById(R.id.latitude_inverse);
        mInverseLongitudeText = (EditText) view.findViewById(R.id.longitude_inverse);
        mInverseStartButton = (Button) view.findViewById(R.id.inverse_button);
        mInverseStartButton.setOnClickListener(mInverseSearchClickListener);
        mInverseResultText = (TextView) view.findViewById(R.id.inverse_result);
        mInverseLatitudeText.addTextChangedListener(mInverseTextWatcher);
        mInverseLongitudeText.addTextChangedListener(mInverseTextWatcher);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDirectEditText.removeTextChangedListener(mDirectTextWatcher);
        mInverseLatitudeText.removeTextChangedListener(mInverseTextWatcher);
        mInverseLongitudeText.removeTextChangedListener(mInverseTextWatcher);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
