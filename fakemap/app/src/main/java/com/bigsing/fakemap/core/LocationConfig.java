package com.bigsing.fakemap.core;

public final class LocationConfig {
    public static final double DEFAULT_LATITUDE = 39.904200;
    public static final double DEFAULT_LONGITUDE = 116.407400;
    public static final float DEFAULT_ACCURACY = 8.0f;
    public static final double DEFAULT_ALTITUDE = 45.0;

    public final boolean authorized;
    public final boolean enabled;
    public final double latitude;
    public final double longitude;
    public final float accuracy;
    public final double altitude;
    public final float speed;
    public final float bearing;
    public final long updatedAt;

    public LocationConfig(
            boolean authorized,
            boolean enabled,
            double latitude,
            double longitude,
            float accuracy,
            double altitude,
            float speed,
            float bearing,
            long updatedAt) {
        this.authorized = authorized;
        this.enabled = enabled;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.altitude = altitude;
        this.speed = speed;
        this.bearing = normalizeBearing(bearing);
        this.updatedAt = updatedAt;
    }

    public static LocationConfig unavailable() {
        return new LocationConfig(
                false,
                false,
                DEFAULT_LATITUDE,
                DEFAULT_LONGITUDE,
                DEFAULT_ACCURACY,
                DEFAULT_ALTITUDE,
                0.0f,
                0.0f,
                0L);
    }

    public GeoPoint point() {
        return new GeoPoint(latitude, longitude);
    }

    public boolean isValid() {
        return point().isValid()
                && Float.isFinite(accuracy)
                && accuracy > 0.0f
                && Double.isFinite(altitude)
                && Float.isFinite(speed)
                && speed >= 0.0f
                && Float.isFinite(bearing);
    }

    public static float normalizeBearing(float value) {
        float normalized = value % 360.0f;
        return normalized < 0.0f ? normalized + 360.0f : normalized;
    }
}
