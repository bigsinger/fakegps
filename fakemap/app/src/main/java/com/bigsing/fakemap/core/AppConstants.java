package com.bigsing.fakemap.core;

import com.bigsing.fakemap.BuildConfig;

public final class AppConstants {
    public static final String PREFERENCES_NAME = "fakemap_preferences";
    public static final String ACTION_START_MOCK = BuildConfig.APPLICATION_ID + ".action.START_MOCK";
    public static final String ACTION_STOP_MOCK = BuildConfig.APPLICATION_ID + ".action.STOP_MOCK";
    public static final String ACTION_MOCK_STATE_CHANGED = BuildConfig.APPLICATION_ID + ".action.MOCK_STATE_CHANGED";
    public static final String EXTRA_MOCK_RUNNING = "mock_running";
    public static final String EXTRA_MOCK_ERROR = "mock_error";
    public static final String EXTRA_FAKEMAP_SOURCE = "fakemap_simulated_location";

    private AppConstants() {
    }

    public static String internalPermission() {
        return BuildConfig.APPLICATION_ID + ".permission.INTERNAL";
    }
}
