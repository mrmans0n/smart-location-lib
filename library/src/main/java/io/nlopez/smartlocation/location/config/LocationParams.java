package io.nlopez.smartlocation.location.config;

/**
 * Created by mrm on 2/1/15.
 */
public class LocationParams {
    // Defaults
    public static final LocationParams PRESET_NAVIGATION = new Builder().setAccuracy(LocationAccuracy.HIGH).setDistance(50).setInterval(500).build();
    public static final LocationParams PRESET_MEDIUM = new Builder().setAccuracy(LocationAccuracy.MEDIUM).setDistance(250).setInterval(2500).build();
    public static final LocationParams PRESET_LOW = new Builder().setAccuracy(LocationAccuracy.LOW).setDistance(1000).setInterval(5000).build();

    private long interval;
    private float distance;
    private LocationAccuracy accuracy;

    LocationParams(LocationAccuracy accuracy, long interval, float distance) {
        this.interval = interval;
        this.distance = distance;
        this.accuracy = accuracy;
    }

    public long getInterval() {
        return interval;
    }

    public float getDistance() {
        return distance;
    }

    public LocationAccuracy getAccuracy() {
        return accuracy;
    }

    public static class Builder {
        private LocationAccuracy accuracy;
        private long interval;
        private float distance;

        public Builder setAccuracy(LocationAccuracy accuracy) {
            this.accuracy = accuracy;
            return this;
        }

        public Builder setInterval(long interval) {
            this.interval = interval;
            return this;
        }

        public Builder setDistance(float distance) {
            this.distance = distance;
            return this;
        }

        public LocationParams build() {
            return new LocationParams(accuracy, interval, distance);
        }
    }
}
