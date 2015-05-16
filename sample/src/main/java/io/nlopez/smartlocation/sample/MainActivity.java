package io.nlopez.smartlocation.sample;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.Geofence;

import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.OnGeofencingTransitionListener;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.geofencing.model.GeofenceModel;

public class MainActivity extends Activity implements OnLocationUpdatedListener, OnActivityUpdatedListener, OnGeofencingTransitionListener {

    private TextView locationText;
    private TextView activityText;
    private TextView geofenceText;
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
        activityText = (TextView) findViewById(R.id.activity_text);
        geofenceText = (TextView) findViewById(R.id.geofence_text);

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

        smartLocation.location().start(this);
        smartLocation.activity().start(this);

        // Create some geofences
        GeofenceModel mestalla = new GeofenceModel.Builder("1").setTransition(Geofence.GEOFENCE_TRANSITION_ENTER).setLatitude(39.47453120000001).setLongitude(-0.358065799999963).setRadius(500).build();
        smartLocation.geofencing().add(mestalla).start(this);
    }

    private void stopLocation() {
        isCapturingLocation = false;

        SmartLocation.with(this).location().stop();
        locationText.setText("Location stopped!");

        SmartLocation.with(this).activityRecognition().stop();
        activityText.setText("Activity Recognition stopped!");

        SmartLocation.with(this).geofencing().stop();
        geofenceText.setText("Geofencing stopped!");
    }

    private void showLocation(Location location) {
        if (location != null) {
            final String text = String.format("Latitude %.6f, Longitude %.6f",
                    location.getLatitude(),
                    location.getLongitude());
            locationText.setText(text);
            
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

    private void showGeofence(Geofence geofence, int transitionType) {
        if (geofence != null) {
            geofenceText.setText("Transition " + getTransitionNameFromType(transitionType) + " for Geofence with id = " + geofence.getRequestId());
        } else {
            geofenceText.setText("Null geofence");
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

    @Override
    public void onGeofenceTransition(Geofence geofence, int transitionType) {
        showGeofence(geofence, transitionType);
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

    private String getTransitionNameFromType(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "enter";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "exit";
            default:
                return "dwell";
        }
    }
}
