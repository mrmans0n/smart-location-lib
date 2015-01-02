package io.nlopez.smartlocation.sample;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import io.nlopez.smartlocation.SmartLocation;

public class MainActivity extends Activity implements SmartLocation.OnLocationUpdatedListener {

    private static final String PACKAGE_NAME = "io.nlopez.smartlocation.sample";

    private TextView locationText;
    private boolean isCapturingLocation = false;
    private boolean userWantsLocation = false;
    private SmartLocation.LocationControl locationControl;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);

        // Bind event clicks
        Button startLocation = (Button) findViewById(R.id.start_location);
        startLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userWantsLocation = true;
                startLocation();
            }
        });

        Button stopLocation = (Button) findViewById(R.id.stop_location);
        stopLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userWantsLocation = false;
                stopLocation();
            }
        });

        // bind textviews
        locationText = (TextView) findViewById(R.id.location_text);

        // Keep the screen always on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();

        showLast();

        if (userWantsLocation && !isCapturingLocation) {
            startLocation();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isCapturingLocation) {
            stopLocation();
        }
    }

    private void showLast() {
        Location lastLocation = SmartLocation.with(this).location(null).getLastLocation();
        if (lastLocation != null) {
            locationText.setText(
                    String.format("[Last Location] Latitude %.6f, Longitude %.6f",
                            lastLocation.getLatitude(),
                            lastLocation.getLongitude())
            );
        }
    }

    private void startLocation() {
        isCapturingLocation = true;
        SmartLocation smartLocation = new SmartLocation.Builder(this).logging(true).build();

        locationControl = smartLocation.location(this).get();
        locationControl.start();

    }

    private void stopLocation() {
        isCapturingLocation = false;

        locationControl.stop();
        // Stop the location capture
        //SmartLocation.getInstance().stopUpdates(this);

        // Cleanup so we know we don't want extra activation/deactivation of the locator for the time being.
        //SmartLocation.getInstance().cleanup(this);

        locationText.setText("Location stopped!");
    }

    private void showLocation(Location location) {
        if (location != null) {
            locationText.setText(
                    String.format("Latitude %.6f, Longitude %.6f",
                            location.getLatitude(),
                            location.getLongitude())
            );
        } else {
            locationText.setText("Null location");
        }
    }

    @Override
    public void onLocationUpdated(Location location) {
        showLocation(location);
    }
}
