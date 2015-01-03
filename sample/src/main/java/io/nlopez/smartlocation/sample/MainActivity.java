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

    private TextView locationText;
    private TextView activityText;
    private boolean isCapturingLocation = false;
    private boolean userWantsLocation = false;
    private SmartLocation.LocationControl locationControl;
    private SmartLocation.ActivityRecognitionControl activityRecognitionControl;

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
        activityText = (TextView) findViewById(R.id.activity_text);

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
        Location lastLocation = SmartLocation.with(this).location().getLastLocation();
        if (lastLocation != null) {
            locationText.setText(
                    String.format("[From Cache] Latitude %.6f, Longitude %.6f",
                            lastLocation.getLatitude(),
                            lastLocation.getLongitude())
            );
        }

        DetectedActivity detectedActivity = SmartLocation.with(this).activityRecognition().getLastActivity();
        if (detectedActivity != null) {
            activityText.setText(
                    String.format("[From Cache] Activity %s with %d%% confidence",
                            getNameFromType(detectedActivity),
                            detectedActivity.getConfidence())
            );
        }
    }

    private void startLocation() {
        isCapturingLocation = true;
        SmartLocation smartLocation = new SmartLocation.Builder(this).logging(true).build();

        locationControl = smartLocation.location().get();
        locationControl.start(this);

        activityRecognitionControl = smartLocation.activityRecognition().get();
        activityRecognitionControl.start(this);

    }

    private void stopLocation() {
        isCapturingLocation = false;

        locationControl.stop();
        locationText.setText("Location stopped!");

        activityRecognitionControl.stop();
        activityText.setText("Activity Recognition stopped!");
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

    private void showActivity(DetectedActivity detectedActivity) {
        if (detectedActivity != null) {
            activityText.setText(
                    String.format("Activity %s with %d%% confidence",
                            getNameFromType(detectedActivity),
                            detectedActivity.getConfidence())
            );
        } else {
            activityText.setText("Null activity");
        }
    }

    @Override
    public void onLocationUpdated(Location location) {
        showLocation(location);
    }

    @Override
    public void onActivityUpdated(DetectedActivity detectedActivity) {
        showActivity(detectedActivity);
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
}
