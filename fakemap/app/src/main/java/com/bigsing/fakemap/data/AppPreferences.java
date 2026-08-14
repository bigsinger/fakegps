package com.bigsing.fakemap.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import com.bigsing.fakemap.BuildConfig;
import com.bigsing.fakemap.core.AppConstants;
import com.bigsing.fakemap.core.LocationConfig;
import com.bigsing.fakemap.core.MapProviderSelector;
import com.bigsing.fakemap.core.TargetPackages;

import java.util.Collections;
import java.util.Set;

public final class AppPreferences {
    public static final String DEFAULT_TILE_URL = "https://tile.openstreetmap.org/{z}/{x}/{y}.png";
    public static final String DEFAULT_ATTRIBUTION = "© OpenStreetMap contributors";
    public static final String DEFAULT_ATTRIBUTION_URL =
            "https://www.openstreetmap.org/copyright";

    private static final String PRIVATE_PREFERENCES_NAME = "fakemap_private_preferences";

    static final String KEY_ENABLED = "xposed_enabled";
    static final String KEY_TARGET_PACKAGES = "target_packages";
    static final String KEY_LATITUDE = "latitude";
    static final String KEY_LONGITUDE = "longitude";
    static final String KEY_ACCURACY = "accuracy";
    static final String KEY_ALTITUDE = "altitude";
    static final String KEY_SPEED = "speed";
    static final String KEY_BEARING = "bearing";
    static final String KEY_UPDATED_AT = "updated_at";
    private static final String KEY_MAP_PROVIDER = "map_provider";
    private static final String KEY_AMAP_KEY = "amap_key";
    private static final String KEY_AMAP_SECURITY = "amap_security";
    private static final String KEY_TILE_URL = "tile_url";
    private static final String KEY_TILE_ATTRIBUTION = "tile_attribution";
    private static final String KEY_TILE_ATTRIBUTION_URL = "tile_attribution_url";
    private static final String KEY_MOCK_RUNNING = "mock_running";

    private final Context context;
    private final SharedPreferences preferences;
    private final SharedPreferences privatePreferences;

    @SuppressLint("WorldReadableFiles")
    public AppPreferences(Context context) {
        this.context = context.getApplicationContext() == null
                ? context
                : context.getApplicationContext();
        // Original Xposed on Android 6 has no remote-preferences service. Only the scoped
        // location configuration (never map credentials) uses its legacy readable file.
        this.preferences = openXposedPreferences();
        this.privatePreferences = this.context.getSharedPreferences(
                PRIVATE_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public LocationConfig locationConfig(boolean authorized) {
        return new LocationConfig(
                authorized,
                preferences.getBoolean(KEY_ENABLED, false),
                readDouble(KEY_LATITUDE, LocationConfig.DEFAULT_LATITUDE),
                readDouble(KEY_LONGITUDE, LocationConfig.DEFAULT_LONGITUDE),
                preferences.getFloat(KEY_ACCURACY, LocationConfig.DEFAULT_ACCURACY),
                readDouble(KEY_ALTITUDE, LocationConfig.DEFAULT_ALTITUDE),
                preferences.getFloat(KEY_SPEED, 0.0f),
                preferences.getFloat(KEY_BEARING, 0.0f),
                preferences.getLong(KEY_UPDATED_AT, 0L));
    }

    public boolean saveLocationConfig(LocationConfig config, Set<String> targetPackages) {
        preferences.edit()
                .putBoolean(KEY_ENABLED, config.enabled)
                .putString(KEY_TARGET_PACKAGES, TargetPackages.format(targetPackages))
                .putString(KEY_LATITUDE, Double.toString(config.latitude))
                .putString(KEY_LONGITUDE, Double.toString(config.longitude))
                .putFloat(KEY_ACCURACY, config.accuracy)
                .putString(KEY_ALTITUDE, Double.toString(config.altitude))
                .putFloat(KEY_SPEED, config.speed)
                .putFloat(KEY_BEARING, config.bearing)
                .putLong(KEY_UPDATED_AT, System.currentTimeMillis())
                .apply();
        return XposedPreferenceBridge.sync(context, config, targetPackages);
    }

    public Set<String> targetPackages() {
        TargetPackages.ParseResult result = TargetPackages.parse(
                preferences.getString(KEY_TARGET_PACKAGES, ""));
        return result.valid ? result.packages : Collections.emptySet();
    }

    public String targetPackagesText() {
        return TargetPackages.format(targetPackages());
    }

    public boolean targetsAny(String[] packageNames) {
        if (packageNames == null || packageNames.length == 0) {
            return false;
        }
        Set<String> targets = targetPackages();
        for (String packageName : packageNames) {
            if (targets.contains(packageName)) {
                return true;
            }
        }
        return false;
    }

    public String mapProvider() {
        String provider = privatePreferences.getString(
                KEY_MAP_PROVIDER, MapProviderSelector.AUTO);
        if (MapProviderSelector.AMAP.equals(provider) || MapProviderSelector.OSM.equals(provider)) {
            return provider;
        }
        return MapProviderSelector.AUTO;
    }

    public void saveMapSettings(
            String provider,
            String amapKey,
            String amapSecurity,
            String tileUrl,
            String attribution,
            String attributionUrl) {
        privatePreferences.edit()
                .putString(KEY_MAP_PROVIDER, provider)
                .putString(KEY_AMAP_KEY, safeTrim(amapKey))
                .putString(KEY_AMAP_SECURITY, safeTrim(amapSecurity))
                .putString(KEY_TILE_URL, safeTrim(tileUrl))
                .putString(KEY_TILE_ATTRIBUTION, safeTrim(attribution))
                .putString(KEY_TILE_ATTRIBUTION_URL, safeTrim(attributionUrl))
                .apply();
    }

    public String amapKey() {
        return privatePreferences.getString(KEY_AMAP_KEY, "");
    }

    public String amapSecurityCode() {
        return privatePreferences.getString(KEY_AMAP_SECURITY, "");
    }

    public String tileUrl() {
        String value = privatePreferences.getString(KEY_TILE_URL, DEFAULT_TILE_URL);
        return value == null || value.trim().isEmpty() ? DEFAULT_TILE_URL : value.trim();
    }

    public String tileAttribution() {
        String value = privatePreferences.getString(
                KEY_TILE_ATTRIBUTION, DEFAULT_ATTRIBUTION);
        return value == null || value.trim().isEmpty() ? DEFAULT_ATTRIBUTION : value.trim();
    }

    public String tileAttributionUrl() {
        String value = privatePreferences.getString(
                KEY_TILE_ATTRIBUTION_URL, DEFAULT_ATTRIBUTION_URL);
        return value == null || value.trim().isEmpty()
                ? DEFAULT_ATTRIBUTION_URL
                : value.trim();
    }

    public boolean isMockRunning() {
        return privatePreferences.getBoolean(KEY_MOCK_RUNNING, false);
    }

    public void setMockRunning(boolean running) {
        privatePreferences.edit().putBoolean(KEY_MOCK_RUNNING, running).apply();
    }

    private double readDouble(String key, double fallback) {
        String value = preferences.getString(key, Double.toString(fallback));
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    @SuppressLint("WorldReadableFiles")
    private SharedPreferences openXposedPreferences() {
        if ("legacy".equals(BuildConfig.XPOSED_API_KIND)) {
            try {
                // New XSharedPreferences implementations intercept this exact mode and route
                // the small, credential-free location file through the framework safe zone.
                return context.getSharedPreferences(
                        AppConstants.PREFERENCES_NAME,
                        Context.MODE_WORLD_READABLE);
            } catch (SecurityException ignored) {
                // Original Xposed on Android 7/8 has no safe preference transport. The hook
                // then fails closed; standard Android mock mode remains available.
            }
        }
        return context.getSharedPreferences(AppConstants.PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
