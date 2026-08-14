package com.bigsing.fakemap.data;

import android.content.Context;

import com.bigsing.fakemap.core.LocationConfig;

import java.util.Set;

/** Legacy builds persist through the framework-compatible SharedPreferences file directly. */
public final class XposedPreferenceBridge {
    private XposedPreferenceBridge() {
    }

    public static void initialize(Context context) {
    }

    public static boolean sync(
            Context context,
            LocationConfig config,
            Set<String> targetPackages) {
        return true;
    }

    public static boolean isConnected() {
        return false;
    }
}
