package io.nlopez.smartlocation.sample;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.LocationController;
import io.nlopez.smartlocation.location.LocationUpdatedListener;

public class LocationFragment extends Fragment {

    private TextView mText;
    private LocationController mController;

    final View.OnClickListener mStartClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mController = SmartLocation.with(getContext())
                    .location()
                    .start(mLocationUpdated);
            updateUi(true);
            mText.setText("Searching location...");
        }
    };

    final View.OnClickListener mStopClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mController != null) {
                mController.stop();
                updateUi(false);
                mText.setText("Location stopped!");
            }
        }
    };

    final LocationUpdatedListener mLocationUpdated = new LocationUpdatedListener() {
        @Override
        public void onLocationUpdated(Location location) {
            mText.setText("Location: "+location.toString());
        }

        @Override
        public void onAllProvidersFailed() {
            mText.setText("All providers failed");
            updateUi(false);
        }
    };
    private Button mStartButton;
    private Button mStopButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mStartButton = (Button) view.findViewById(R.id.start_location);
        mStartButton.setOnClickListener(mStartClickListener);
        mStopButton = (Button) view.findViewById(R.id.stop_location);
        mStopButton.setOnClickListener(mStopClickListener);
        mStopButton.setEnabled(false);
        mText = (TextView) view.findViewById(R.id.location_text);
    }

    private void updateUi(boolean locationEnabled) {
        mStartButton.setEnabled(!locationEnabled);
        mStopButton.setEnabled(locationEnabled);
    }

    @Override
    public void onDestroy() {
        mController.release();
        super.onDestroy();
    }
}
