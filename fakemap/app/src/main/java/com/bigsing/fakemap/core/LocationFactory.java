package com.bigsing.fakemap.core;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;

@SuppressLint("ObsoleteSdkInt")
public final class LocationFactory {
    private LocationFactory() {
    }

    public static Location create(String provider, LocationConfig config) {
        return replace(new Location(provider == null ? "gps" : provider), config);
    }

    public static Location replace(Location source, LocationConfig config) {
        String provider = source == null || source.getProvider() == null
                ? "gps"
                : source.getProvider();
        Location location = source == null ? new Location(provider) : new Location(source);
        location.setProvider(provider);
        location.setLatitude(config.latitude);
        location.setLongitude(config.longitude);
        location.setAccuracy(config.accuracy);
        location.setAltitude(config.altitude);
        location.setSpeed(config.speed);
        location.setBearing(config.bearing);
        location.setTime(System.currentTimeMillis());
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            float secondaryAccuracy = Math.max(1.0f, config.accuracy);
            location.setVerticalAccuracyMeters(secondaryAccuracy);
            location.setSpeedAccuracyMetersPerSecond(Math.max(0.1f, secondaryAccuracy / 10.0f));
            location.setBearingAccuracyDegrees(Math.min(180.0f, secondaryAccuracy));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.setMock(true);
        }

        Bundle extras = location.getExtras() == null
                ? new Bundle()
                : new Bundle(location.getExtras());
        extras.putBoolean(AppConstants.EXTRA_FAKEMAP_SOURCE, true);
        location.setExtras(extras);
        return location;
    }
}
