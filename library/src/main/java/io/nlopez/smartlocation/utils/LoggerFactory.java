package io.nlopez.smartlocation.utils;

import android.util.Log;

import io.nlopez.smartlocation.BuildConfig;
import io.nlopez.smartlocation.common.Factory0;

public class LoggerFactory implements Factory0<Logger> {
    private static Logger sLogger;
    private static final Logger MUTED = new Logger() {
        @Override
        public void v(String message, Object... args) {

        }

        @Override
        public void v(Throwable t, String message, Object... args) {

        }

        @Override
        public void d(String message, Object... args) {

        }

        @Override
        public void d(Throwable t, String message, Object... args) {

        }

        @Override
        public void i(String message, Object... args) {

        }

        @Override
        public void i(Throwable t, String message, Object... args) {

        }

        @Override
        public void w(String message, Object... args) {

        }

        @Override
        public void w(Throwable t, String message, Object... args) {

        }

        @Override
        public void e(String message, Object... args) {

        }

        @Override
        public void e(Throwable t, String message, Object... args) {

        }
    };

    private static final Logger ANDROID_LOGGER = new Logger() {
        private String getTag() {
            return new Exception().getStackTrace()[3].getMethodName();
        }

        private String formatMessage(String message, Object... args) {
            return args.length == 0 ? message : String.format(message, args);
        }

        @Override
        public void v(String message, Object... args) {
            Log.v(getTag(), formatMessage(message, args));
        }

        @Override
        public void v(Throwable t, String message, Object... args) {
            Log.v(getTag(), formatMessage(message, args), t);
        }

        @Override
        public void d(String message, Object... args) {
            Log.d(getTag(), formatMessage(message, args));

        }

        @Override
        public void d(Throwable t, String message, Object... args) {
            Log.d(getTag(), formatMessage(message, args), t);

        }

        @Override
        public void i(String message, Object... args) {
            Log.i(getTag(), formatMessage(message, args));

        }

        @Override
        public void i(Throwable t, String message, Object... args) {
            Log.i(getTag(), formatMessage(message, args), t);

        }

        @Override
        public void w(String message, Object... args) {
            Log.w(getTag(), formatMessage(message, args));

        }

        @Override
        public void w(Throwable t, String message, Object... args) {
            Log.w(getTag(), formatMessage(message, args), t);

        }

        @Override
        public void e(String message, Object... args) {
            Log.e(getTag(), formatMessage(message, args));

        }

        @Override
        public void e(Throwable t, String message, Object... args) {
            Log.e(getTag(), formatMessage(message, args), t);
        }
    };

    public static Logger get() {
        if (sLogger == null) {
            sLogger = new LoggerFactory().create();
        }
        return sLogger;
    }

    @Override
    public Logger create() {
        return BuildConfig.DEBUG ? ANDROID_LOGGER : MUTED;
    }
}
