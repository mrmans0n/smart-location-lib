package io.nlopez.smartlocation.utils;

import android.util.Log;

/**
 * Created by mrm on 20/12/14.
 */
public class LoggerFactory {

    public static Logger buildLogger(boolean loggingEnabled) {
        return loggingEnabled ? new Blabber() : new Sssht();
    }

    private static class Sssht implements Logger {

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
    }

    private static class Blabber implements Logger {

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
    }
}
