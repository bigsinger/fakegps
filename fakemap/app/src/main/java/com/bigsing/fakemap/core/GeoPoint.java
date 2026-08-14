package com.bigsing.fakemap.core;

import java.util.Locale;

public final class GeoPoint {
    public final double latitude;
    public final double longitude;

    public GeoPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public boolean isValid() {
        return Double.isFinite(latitude)
                && Double.isFinite(longitude)
                && latitude >= -90.0
                && latitude <= 90.0
                && longitude >= -180.0
                && longitude <= 180.0;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%.6f,%.6f", latitude, longitude);
    }
}
