package com.bigsing.fakemap.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.bigsing.fakemap.core.AppConstants;
import com.bigsing.fakemap.core.LocationConfig;
import com.bigsing.fakemap.core.TargetPackages;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.libxposed.service.XposedService;
import io.github.libxposed.service.XposedServiceHelper;

/** App-side writer for libxposed's framework-owned remote preference group. */
public final class XposedPreferenceBridge {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean();
    private static final CopyOnWriteArraySet<XposedService> SERVICES =
            new CopyOnWriteArraySet<>();
    private static volatile Context applicationContext;

    private XposedPreferenceBridge() {
    }

    public static void initialize(Context context) {
        applicationContext = context.getApplicationContext() == null
                ? context
                : context.getApplicationContext();
        if (!INITIALIZED.compareAndSet(false, true)) {
            return;
        }
        XposedServiceHelper.registerListener(new XposedServiceHelper.OnServiceListener() {
            @Override
            public void onServiceBind(XposedService service) {
                if (!supportsRemotePreferences(service)) {
                    return;
                }
                SERVICES.add(service);
                AppPreferences local = new AppPreferences(applicationContext);
                sync(applicationContext, local.locationConfig(true), local.targetPackages());
            }

            @Override
            public void onServiceDied(XposedService service) {
                SERVICES.remove(service);
            }
        });
    }

    public static boolean sync(
            Context context,
            LocationConfig config,
            Set<String> targetPackages) {
        boolean synchronizedAtLeastOne = false;
        for (XposedService service : SERVICES) {
            try {
                SharedPreferences remote = service.getRemotePreferences(
                        AppConstants.PREFERENCES_NAME);
                boolean committed = remote.edit()
                        .putBoolean(AppPreferences.KEY_ENABLED, config.enabled)
                        .putString(AppPreferences.KEY_TARGET_PACKAGES,
                                TargetPackages.format(targetPackages))
                        .putString(AppPreferences.KEY_LATITUDE,
                                Double.toString(config.latitude))
                        .putString(AppPreferences.KEY_LONGITUDE,
                                Double.toString(config.longitude))
                        .putFloat(AppPreferences.KEY_ACCURACY, config.accuracy)
                        .putString(AppPreferences.KEY_ALTITUDE,
                                Double.toString(config.altitude))
                        .putFloat(AppPreferences.KEY_SPEED, config.speed)
                        .putFloat(AppPreferences.KEY_BEARING, config.bearing)
                        .putLong(AppPreferences.KEY_UPDATED_AT, System.currentTimeMillis())
                        .commit();
                synchronizedAtLeastOne |= committed;
            } catch (RuntimeException error) {
                SERVICES.remove(service);
            }
        }
        return synchronizedAtLeastOne;
    }

    public static boolean isConnected() {
        return !SERVICES.isEmpty();
    }

    private static boolean supportsRemotePreferences(XposedService service) {
        return (service.getFrameworkProperties() & XposedService.PROP_CAP_REMOTE) != 0L;
    }
}
