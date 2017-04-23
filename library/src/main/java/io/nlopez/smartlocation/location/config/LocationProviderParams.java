package io.nlopez.smartlocation.location.config;

import android.support.annotation.NonNull;

public class LocationProviderParams {
    // Defaults
    public static final LocationProviderParams NAVIGATION = new Builder()
            .accuracy(LocationAccuracy.HIGH)
            .distance(0)
            .interval(500)
            .runOnlyOnce(false)
            .build();
    public static final LocationProviderParams BEST_EFFORT = new Builder()
            .accuracy(LocationAccuracy.MEDIUM)
            .distance(150)
            .interval(2500)
            .runOnlyOnce(false)
            .build();
    public static final LocationProviderParams BEST_EFFORT_ONCE = new Builder(BEST_EFFORT)
            .runOnlyOnce(true)
            .build();
    public static final LocationProviderParams LAZY = new Builder()
            .accuracy(LocationAccuracy.LOW)
            .distance(500)
            .interval(5000)
            .runOnlyOnce(false)
            .build();

    public long interval;
    public float distance;
    @LocationAccuracy
    public int accuracy;
    public boolean runOnlyOnce;
    public boolean checkLocationSettings;

    LocationProviderParams(@NonNull Builder builder) {
        interval = builder.interval;
        distance = builder.distance;
        accuracy = builder.accuracy;
        runOnlyOnce = builder.runOnlyOnce;
        checkLocationSettings = builder.checkLocationSettings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final LocationProviderParams that = (LocationProviderParams) o;

        if (interval != that.interval) return false;
        if (Float.compare(that.distance, distance) != 0) return false;
        if (accuracy != that.accuracy) return false;
        if (runOnlyOnce != that.runOnlyOnce) return false;
        return checkLocationSettings == that.checkLocationSettings;
    }

    @Override
    public int hashCode() {
        int result = (int) (interval ^ (interval >>> 32));
        result = 31 * result + (distance != +0.0f ? Float.floatToIntBits(distance) : 0);
        result = 31 * result + accuracy;
        result = 31 * result + (runOnlyOnce ? 1 : 0);
        result = 31 * result + (checkLocationSettings ? 1 : 0);
        return result;
    }

    public static class Builder {
        @LocationAccuracy
        int accuracy = LAZY.accuracy;
        long interval = LAZY.interval;
        float distance = LAZY.distance;
        boolean runOnlyOnce = false;
        boolean checkLocationSettings = false;

        public Builder() {
        }

        public Builder(@NonNull LocationProviderParams params) {
            accuracy = params.accuracy;
            interval = params.interval;
            distance = params.distance;
            runOnlyOnce = params.runOnlyOnce;
        }

        public Builder runOnlyOnce(boolean value) {
            runOnlyOnce = value;
            return this;
        }

        public Builder accuracy(@LocationAccuracy int accuracy) {
            this.accuracy = accuracy;
            return this;
        }

        public Builder interval(long interval) {
            this.interval = interval;
            return this;
        }

        public Builder distance(float distance) {
            this.distance = distance;
            return this;
        }

        public LocationProviderParams build() {
            return new LocationProviderParams(this);
        }
    }
}
