package io.nlopez.smartlocation.sample;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.DetectedActivity;

import io.nlopez.smartlocation.SmartLocation;

public class MainActivity extends Activity implements SmartLocation.OnLocationUpdatedListener, SmartLocation.OnActivityUpdatedListener {

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

        SmartLocation smartLocation = new SmartLocation.Builder(this).logging(true).build();

        locationControl = smartLocation.location(this).oneFix().get();
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

    private void showLocation(Location location, DetectedActivity activity) {
        String activityName = getNameFromType(activity) + " (" + activity.getType() + ") with " + activity.getConfidence() + "% confidence. ";
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

    private String getNameFromType(DetectedActivity activityType) {
        switch (activityType.getType()) {
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

    @Override
    public void onLocationUpdated(Location location) {
        showLocation(location, new DetectedActivity(DetectedActivity.UNKNOWN, 100));
    }

    @Override
    public void onActivityUpdated(DetectedActivity activity) {

    }
}
