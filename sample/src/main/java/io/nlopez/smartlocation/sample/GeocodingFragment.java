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

import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.geocoding.GeocodingUpdatedListener;
import io.nlopez.smartlocation.geocoding.ReverseGeocodingUpdatedListener;
import io.nlopez.smartlocation.geocoding.utils.LocationAddress;

public class GeocodingFragment extends Fragment {

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
                    .geocoding()
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
            final String latitudeText = mInverseLatitudeText.getText().toString();
            final Double latitude = Double.parseDouble(latitudeText);
            final String longitudeText = mInverseLongitudeText.getText().toString();
            final Double longitude = Double.parseDouble(longitudeText);
            final Location location = new Location("test");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            SmartLocation.with(getContext())
                    .geocoding()
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
            final boolean hasLatitude = !TextUtils.isEmpty(mInverseLatitudeText.getText().toString());
            final boolean hasLongitude = !TextUtils.isEmpty(mInverseLongitudeText.getText().toString());
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
