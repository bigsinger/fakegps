package com.bigsing.fakemap.core;

import android.location.Location;
import android.location.LocationManager;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Array;

/** Copies and replaces public location values at delivery boundaries. */
public final class LocationBoundary {
    private LocationBoundary() {
    }

    public static Object replaceValue(Object value, LocationConfig config) {
        if (!active(config) || value == null) {
            return value;
        }
        if (value instanceof Location) {
            return LocationFactory.replace((Location) value, config);
        }
        if (value instanceof List<?>) {
            List<?> source = (List<?>) value;
            ArrayList<Object> output = new ArrayList<>(source.size());
            boolean containedLocation = false;
            for (Object item : source) {
                if (item instanceof Location) {
                    output.add(LocationFactory.replace((Location) item, config));
                    containedLocation = true;
                } else {
                    output.add(item);
                }
            }
            return containedLocation ? output : value;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            Object output = Array.newInstance(value.getClass().getComponentType(), length);
            boolean containedLocation = false;
            for (int i = 0; i < length; i++) {
                Object item = Array.get(value, i);
                if (item instanceof Location) {
                    Array.set(output, i, LocationFactory.replace((Location) item, config));
                    containedLocation = true;
                } else {
                    Array.set(output, i, item);
                }
            }
            return containedLocation ? output : value;
        }
        return value;
    }

    public static Location synthetic(LocationConfig config) {
        return LocationFactory.create(LocationManager.GPS_PROVIDER, config);
    }

    public static boolean isLocationExtraKey(Object value) {
        if (!(value instanceof String)) {
            return false;
        }
        String key = (String) value;
        // KEY_LOCATIONS was added in API 31, but its public constant value is inlined.
        return LocationManager.KEY_LOCATION_CHANGED.equals(key) || "locations".equals(key);
    }

    public static boolean active(LocationConfig config) {
        return config != null && config.authorized && config.enabled && config.isValid();
    }
}
