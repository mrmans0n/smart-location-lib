package com.mobivery.greent.smartlocation.sample;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.DetectedActivity;
import com.mobivery.greent.smartlocation.SmartLocation;
import com.mobivery.greent.smartlocation.SmartLocationOptions;
import com.mobivery.greent.smartlocation.UpdateStrategy;
import com.mobivery.smartlocation.sample.R;

public class MainActivity extends Activity {

    private static final String PACKAGE_NAME = "com.mobivery.smartlocation.greent.sample";
    private static final String LOCATION_UPDATED_INTENT = PACKAGE_NAME + SmartLocation.LOCATION_BROADCAST_INTENT_TRAIL;

    private TextView locationText;
    private boolean isCapturingLocation = false;
    private boolean userWantsLocation = false;

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

    private void startLocation() {

        // Create some custom options for our class. This is not needed unless we want some extra control.
        SmartLocationOptions options = new SmartLocationOptions();
        options.setPackageName(PACKAGE_NAME);
        options.setDefaultUpdateStrategy(UpdateStrategy.BEST_EFFORT);
        options.setOnActivityRecognizerUpdatedNewStrategy(new SmartLocationOptions.OnActivityRecognizerUpdated() {
            @Override
            public UpdateStrategy getUpdateStrategyForActivity(int detectedActivity) {
                switch (detectedActivity) {
                    case DetectedActivity.IN_VEHICLE:
                    case DetectedActivity.ON_BICYCLE:
                        return UpdateStrategy.NAVIGATION;
                    default:
                        return UpdateStrategy.BEST_EFFORT;
                }
            }
        });

        // Init the location with custom options
        SmartLocation.getInstance().start(this, options, new SmartLocation.OnLocationUpdatedListener() {
            @Override
            public void onLocationUpdated(Location location, DetectedActivity detectedActivity) {
                showLocation(location, detectedActivity);
            }
        });

        // Try to restore the cached values, in case the first location takes long
        Location tryLastLocation = SmartLocation.getInstance().getLastKnownLocation(this);
        DetectedActivity tryLastActivity = SmartLocation.getInstance().getLastKnownActivity(this);

        isCapturingLocation = true;
        if (tryLastLocation != null && tryLastActivity != null) {
            showLocation(tryLastLocation, tryLastActivity);
            locationText.setText(locationText.getText() + "\n (from cache)");
        } else {
            locationText.setText("Location started! Getting the first fix...");
        }
    }

    private void stopLocation() {
        isCapturingLocation = false;

        // Stop the location capture
        SmartLocation.getInstance().stop(this);

        // Cleanup so we know we don't want extra activation/deactivation of the locator for the time being.
        SmartLocation.getInstance().cleanup(this);

        locationText.setText("Location stopped!");
    }

    private void showLocation(Location location, DetectedActivity activity) {
        String activityName = getNameFromType(activity.getType()) + " (" + activity.getType() + ") with " + activity.getConfidence() + "% confidence. ";
        if (location != null) {
            locationText.setText(
                    String.format("Latitude %.6f, Longitude %.6f, Activity %s",
                            location.getLatitude(),
                            location.getLongitude(),
                            activityName)
            );
        } else {
            locationText.setText("Null location, Activity " + activityName);
        }
    }

    private String getNameFromType(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.TILTING:
                return "tilting";
            default:
                return "unknown";
        }
    }
}