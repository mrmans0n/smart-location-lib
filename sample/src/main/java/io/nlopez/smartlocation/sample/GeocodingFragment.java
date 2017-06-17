package io.nlopez.smartlocation.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
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
import io.nlopez.smartlocation.geocoding.utils.LocationAddress;

public class GeocodingFragment extends Fragment {

    private EditText mDirectEditText;
    private TextView mDirectResultText;
    private Button mDirectStartButton;

    final View.OnClickListener mDirectSearchClickListener = new View.OnClickListener() {
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
    final TextWatcher mTextWatcher = new TextWatcher() {
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_geocoding, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mDirectStartButton = (Button) view.findViewById(R.id.direct_button);
        mDirectStartButton.setOnClickListener(mDirectSearchClickListener);
        mDirectEditText = (EditText) view.findViewById(R.id.direct_search);
        mDirectResultText = (TextView) view.findViewById(R.id.direct_result);
        mDirectEditText.addTextChangedListener(mTextWatcher);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDirectEditText.removeTextChangedListener(mTextWatcher);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
