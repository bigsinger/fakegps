package com.bigsing.fakemap.core;

public final class MapProviderSelector {
    public static final String AUTO = "auto";
    public static final String AMAP = "amap";
    public static final String OSM = "osm";

    private MapProviderSelector() {
    }

    public static String resolve(
            String preference,
            GeoPoint target,
            boolean hasAmapCredentials) {
        if (AMAP.equals(preference)) {
            return hasAmapCredentials && CoordinateTransform.usesGcj02(target) ? AMAP : OSM;
        }
        if (OSM.equals(preference)) {
            return OSM;
        }
        return hasAmapCredentials && CoordinateTransform.usesGcj02(target) ? AMAP : OSM;
    }
}
