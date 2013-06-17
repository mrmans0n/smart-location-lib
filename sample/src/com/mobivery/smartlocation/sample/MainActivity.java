package com.mobivery.smartlocation.sample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.DetectedActivity;
import com.mobivery.smartlocation.SmartLocation;
import com.mobivery.smartlocation.SmartLocationOptions;

public class MainActivity extends Activity {

    private static final String PACKAGE_NAME = "com.mobivery.smartlocation.sample";
    private static final String LOCATION_UPDATED_INTENT = PACKAGE_NAME + SmartLocation.LOCATION_BROADCAST_INTENT_TRAIL;

    private TextView locationText;
    private boolean isCapturingLocation = false;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);

        Button startLocation = (Button) findViewById(R.id.start_location);
        startLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocation(MainActivity.this);
            }
        });

        Button stopLocation = (Button) findViewById(R.id.stop_location);
        stopLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopLocation(MainActivity.this);
            }
        });

        locationText = (TextView) findViewById(R.id.location_text);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isCapturingLocation) {
            startLocation(this);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isCapturingLocation) {
            stopLocation(this);
        }
    }

    private void startLocation(Context context) {
        SmartLocationOptions options = new SmartLocationOptions();
        options.setPackageName(PACKAGE_NAME);
        SmartLocation.getInstance().start(context, options);

        captureIntent();
        isCapturingLocation = true;
        locationText.setText("Location started! Getting the first fix...");
    }

    private void stopLocation(Context context) {
        isCapturingLocation = false;
        releaseIntent();
        SmartLocation.getInstance().stop(context);
        SmartLocation.getInstance().cleanup(context);
        locationText.setText("Location stopped!");
    }

    private void captureIntent() {
        IntentFilter locationUpdatesIntentFilter = new IntentFilter(LOCATION_UPDATED_INTENT);
        registerReceiver(locationUpdatesReceiver, locationUpdatesIntentFilter);
    }

    private void releaseIntent() {
        try {
            unregisterReceiver(locationUpdatesReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver locationUpdatesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(SmartLocation.DETECTED_LOCATION_KEY);
            int activity = intent.getIntExtra(SmartLocation.DETECTED_ACTIVITY_KEY, DetectedActivity.UNKNOWN);
            showLocation(location, activity);
        }
    };

    private void showLocation(Location location, int activityType) {
        String activityName = getNameFromType(activityType);
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
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
        }
        return "unknown";
    }
}