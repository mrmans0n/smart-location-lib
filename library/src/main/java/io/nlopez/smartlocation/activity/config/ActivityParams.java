package io.nlopez.smartlocation.activity.config;

/**
 * Created by mrm on 3/1/15.
 */
public class ActivityParams {
    // Defaults
    public static final ActivityParams NORMAL = new Builder().setInterval(500).build();

    private long interval;

    ActivityParams(long interval) {
        this.interval = interval;
    }

    public long getInterval() {
        return interval;
    }

    public static class Builder {
        private long interval;

        public Builder setInterval(long interval) {
            this.interval = interval;
            return this;
        }

        public ActivityParams build() {
            return new ActivityParams(interval);
        }
    }
}
