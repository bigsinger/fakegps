package com.bigsing.fakemap.data;

import android.content.SharedPreferences;
import android.os.SystemClock;

import com.bigsing.fakemap.core.LocationConfig;
import com.bigsing.fakemap.core.TargetPackages;

import java.util.Collections;
import java.util.Set;

public final class RemoteConfigClient {
    private static final long REFRESH_INTERVAL_MS = 1_000L;
    private static final Object LOCK = new Object();

    private static volatile SharedPreferences preferences;
    private static volatile String targetPackage = "";
    private static volatile Runnable reloadAction;
    private static volatile LocationConfig cached = LocationConfig.unavailable();
    private static volatile long lastRefreshElapsed;
    private static final SharedPreferences.OnSharedPreferenceChangeListener CHANGE_LISTENER =
            (source, key) -> lastRefreshElapsed = 0L;

    private RemoteConfigClient() {
    }

    public static LocationConfig bindAndRefresh(
            SharedPreferences remotePreferences,
            String packageName,
            Runnable reload) {
        SharedPreferences previous = preferences;
        if (previous != null) {
            try {
                previous.unregisterOnSharedPreferenceChangeListener(CHANGE_LISTENER);
            } catch (RuntimeException ignored) {
            }
        }
        preferences = remotePreferences;
        if (remotePreferences != null) {
            try {
                remotePreferences.registerOnSharedPreferenceChangeListener(CHANGE_LISTENER);
            } catch (RuntimeException ignored) {
            }
        }
        targetPackage = packageName == null ? "" : packageName;
        reloadAction = reload;
        lastRefreshElapsed = 0L;
        return refresh(true);
    }

    public static LocationConfig current() {
        return refresh(false);
    }

    private static LocationConfig refresh(boolean force) {
        SharedPreferences currentPreferences = preferences;
        if (currentPreferences == null) {
            return LocationConfig.unavailable();
        }
        long now = SystemClock.elapsedRealtime();
        if (!force && now - lastRefreshElapsed < REFRESH_INTERVAL_MS) {
            return cached;
        }
        synchronized (LOCK) {
            now = SystemClock.elapsedRealtime();
            if (!force && now - lastRefreshElapsed < REFRESH_INTERVAL_MS) {
                return cached;
            }
            Runnable currentReload = reloadAction;
            if (currentReload != null) {
                try {
                    currentReload.run();
                } catch (RuntimeException ignored) {
                    cached = LocationConfig.unavailable();
                    lastRefreshElapsed = now;
                    return cached;
                }
            }
            cached = read(currentPreferences, targetPackage);
            lastRefreshElapsed = now;
            return cached;
        }
    }

    private static LocationConfig read(SharedPreferences source, String packageName) {
        try {
            Set<String> targets = parseTargets(source.getString(
                    AppPreferences.KEY_TARGET_PACKAGES,
                    ""));
            return new LocationConfig(
                    targets.contains(packageName),
                    source.getBoolean(AppPreferences.KEY_ENABLED, false),
                    readDouble(source, AppPreferences.KEY_LATITUDE,
                            LocationConfig.DEFAULT_LATITUDE),
                    readDouble(source, AppPreferences.KEY_LONGITUDE,
                            LocationConfig.DEFAULT_LONGITUDE),
                    source.getFloat(AppPreferences.KEY_ACCURACY,
                            LocationConfig.DEFAULT_ACCURACY),
                    readDouble(source, AppPreferences.KEY_ALTITUDE,
                            LocationConfig.DEFAULT_ALTITUDE),
                    source.getFloat(AppPreferences.KEY_SPEED, 0.0f),
                    source.getFloat(AppPreferences.KEY_BEARING, 0.0f),
                    source.getLong(AppPreferences.KEY_UPDATED_AT, 0L));
        } catch (RuntimeException ignored) {
            return LocationConfig.unavailable();
        }
    }

    private static Set<String> parseTargets(String value) {
        TargetPackages.ParseResult result = TargetPackages.parse(value);
        return result.valid ? result.packages : Collections.emptySet();
    }

    private static double readDouble(
            SharedPreferences source,
            String key,
            double fallback) {
        try {
            return Double.parseDouble(source.getString(key, Double.toString(fallback)));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
