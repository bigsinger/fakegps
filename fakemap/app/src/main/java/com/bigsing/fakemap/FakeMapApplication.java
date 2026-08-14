package com.bigsing.fakemap;

import android.app.Application;

import com.bigsing.fakemap.data.AppPreferences;
import com.bigsing.fakemap.data.XposedPreferenceBridge;

/** Application entry point. Configuration stays in private app storage. */
public final class FakeMapApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        XposedPreferenceBridge.initialize(this);
        // The service is START_NOT_STICKY, so a new process means no old simulation is running.
        new AppPreferences(this).setMockRunning(false);
    }
}
