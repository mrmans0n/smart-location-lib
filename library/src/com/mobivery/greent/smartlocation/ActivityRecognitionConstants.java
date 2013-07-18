package com.mobivery.greent.smartlocation;

/**
 * Created by Nacho L. on 12/06/13.
 */
public class ActivityRecognitionConstants {

    public static final String ACTIVITY_CHANGED_INTENT = "com.mobivery.greent.smartlocation.ACTIVITY_CHANGED";
    public static final String ACTIVITY_KEY = "ACTIVITY";

    // Intervalo de deteccion entre actividades
    public static final int ACTIVITY_DETECTION_INTERVAL = 2000;

    // El % de confianza minima que usamos como umbral para validar una nueva actividad
    public static final int MINIMUM_ACTIVITY_CONFIDENCY = 50;
}
