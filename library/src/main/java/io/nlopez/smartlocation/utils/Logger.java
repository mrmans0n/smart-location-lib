package io.nlopez.smartlocation.utils;

/**
 * Created by mrm on 20/12/14.
 */
public interface Logger {
    void v(String message, Object... args);

    void v(Throwable t, String message, Object... args);

    void d(String message, Object... args);

    void d(Throwable t, String message, Object... args);

    void i(String message, Object... args);

    void i(Throwable t, String message, Object... args);

    void w(String message, Object... args);

    void w(Throwable t, String message, Object... args);

    void e(String message, Object... args);

    void e(Throwable t, String message, Object... args);
}