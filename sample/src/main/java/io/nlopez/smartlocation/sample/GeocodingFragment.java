package io.nlopez.smartlocation.sample;

import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.geocoding.GeocodingUpdatedListener;
import io.nlopez.smartlocation.geocoding.ReverseGeocodingUpdatedListener;
import io.nlopez.smartlocation.geocoding.common.LocationAddress;
import io.nlopez.smartlocation.geocoding.providers.android.AndroidGeocodingProviderFactory;
import io.nlopez.smartlocation.geocoding.providers.googlemaps.GoogleMapsApiGeocodingProviderFactory;
import io.nlopez.smartlocation.location.LocationController;
import io.nlopez.smartlocation.location.LocationUpdatedListener;
import io.nlopez.smartlocation.location.config.LocationProviderParams;
import io.nlopez.smartlocation.utils.Nulls;

public class GeocodingFragment extends Fragment {

    // Change this for a Google Maps API key if you want to use it
    // Info on how to do this: https://developers.google.com/maps/documentation/geocoding/get-api-key
    private static final String GOOGLE_MAPS_API_KEY = "your-google-maps-api-key";

    private static final Pattern LATITUDE_REGEX =
            Pattern.compile("^(\\+|-)?(?:90(?:(?:\\.0{1,6})?)|(?:[0-9]|[1-8][0-9])(?:(?:\\.[0-9]{1,6})?))$");

    private static final Pattern LONGITUDE_REGEX =
            Pattern.compile("^(\\+|-)?(?:180(?:(?:\\.0{1,6})?)|(?:[0-9]|[1-9][0-9]|1[0-7][0-9])(?:(?:\\.[0-9]{1,6})?))$");

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);

    private EditText mDirectEditText;
    private TextView mDirectResultText;
    private Button mDirectStartButton;
    private EditText mInverseLatitudeText;
    private EditText mInverseLongitudeText;
    private Button mInverseStartButton;
    private TextView mInverseResultText;
    private Button mCurrentLocationButton;

    private final View.OnClickListener mDirectSearchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SmartLocation.with(requireContext())
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
            final double latitude = Double.parseDouble(latitudeText);
            final String longitudeText = mInverseLongitudeText.getText().toString();
            final double longitude = Double.parseDouble(longitudeText);
            final Location location = new Location("test");
            location.setLatitude(latitude);
            location.setLongitude(longitude);

            // Inverse geocoding retrieval
            SmartLocation.with(requireContext())
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

    private final View.OnClickListener mCurrentLocationListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            final LocationController locationController = SmartLocation.with(requireContext())
                    .location()
                    .config(LocationProviderParams.BEST_EFFORT_ONCE) // get location only once
                    .timeout(30000) // 30 seconds timeout
                    .start(new LocationUpdatedListener() {
                        @Override
                        public void onLocationUpdated(final Location location) {
                            // Populate the text fields with the current location
                            updateFromLocation(location);
                            reenableButton();
                        }

                        @Override
                        public void onAllProvidersFailed() {
                            snackText("Unable to retrieve location");
                            reenableButton();
                        }
                    });
            mCurrentLocationButton.setText("Searching...");
            mCurrentLocationButton.setEnabled(false);

            // If there is a last location we use that as a shortcut
            final Location lastLocation = locationController.getLastLocation();
            if (lastLocation != null) {
                updateFromLocation(lastLocation);
                locationController.stop();
            }
        }
    };

    private void updateFromLocation(@NonNull final Location location) {
        mInverseLatitudeText.setText(NUMBER_FORMAT.format(location.getLatitude()));
        mInverseLongitudeText.setText(NUMBER_FORMAT.format(location.getLongitude()));
    }

    private void reenableButton() {
        mCurrentLocationButton.setText("Current Location");
        mCurrentLocationButton.setEnabled(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_geocoding, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // Direct geocoding stuff
        mDirectStartButton = view.findViewById(R.id.direct_button);
        mDirectStartButton.setOnClickListener(mDirectSearchClickListener);
        mDirectEditText = view.findViewById(R.id.direct_search);
        mDirectResultText = view.findViewById(R.id.direct_result);
        mDirectEditText.addTextChangedListener(mDirectTextWatcher);

        // Inverse geocoding stuff
        mInverseLatitudeText = view.findViewById(R.id.latitude);
        mInverseLongitudeText = view.findViewById(R.id.longitude);
        mInverseStartButton = view.findViewById(R.id.add_geofence_button);
        mInverseStartButton.setOnClickListener(mInverseSearchClickListener);
        mInverseResultText = view.findViewById(R.id.inverse_result);
        mInverseLatitudeText.addTextChangedListener(mInverseTextWatcher);
        mInverseLongitudeText.addTextChangedListener(mInverseTextWatcher);

        // Current location
        mCurrentLocationButton = view.findViewById(R.id.current_location_button);
        mCurrentLocationButton.setOnClickListener(mCurrentLocationListener);
        NUMBER_FORMAT.setMaximumFractionDigits(6);
    }

    @Override
    public void onPause() {
        super.onPause();
        SmartLocation.with(requireContext()).location().stop();
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

    private void snackText(@NonNull String message) {
        Snackbar.make(Nulls.notNull(getView()), message, Snackbar.LENGTH_LONG).show();
    }
}
