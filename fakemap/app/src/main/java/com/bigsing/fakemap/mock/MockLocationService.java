package com.bigsing.fakemap.mock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.provider.ProviderProperties;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.bigsing.fakemap.R;
import com.bigsing.fakemap.core.AppConstants;
import com.bigsing.fakemap.core.LocationConfig;
import com.bigsing.fakemap.core.LocationFactory;
import com.bigsing.fakemap.data.AppPreferences;
import com.bigsing.fakemap.ui.MainActivity;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@SuppressLint("ObsoleteSdkInt")
public final class MockLocationService extends Service {
    private static final String CHANNEL_ID = "fakemap_location_simulation";
    private static final int NOTIFICATION_ID = 2001;
    private static final long UPDATE_INTERVAL_MS = 1_000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Set<String> installedProviders = new LinkedHashSet<>();
    private LocationManager locationManager;
    private AppPreferences preferences;
    private boolean running;
    private int consecutiveDeliveryFailures;

    private final Runnable locationTicker = new Runnable() {
        @Override
        public void run() {
            if (!running) {
                return;
            }
            LocationConfig config = preferences.locationConfig(true);
            if (!config.isValid()) {
                stopSimulation(getString(R.string.invalid_coordinate));
                return;
            }
            int delivered = 0;
            for (String provider : installedProviders) {
                try {
                    Location location = LocationFactory.create(provider, config);
                    locationManager.setTestProviderLocation(provider, location);
                    delivered += 1;
                } catch (RuntimeException ignored) {
                    // One OEM provider must not stop updates to the others.
                }
            }
            if (delivered == 0) {
                consecutiveDeliveryFailures += 1;
                if (consecutiveDeliveryFailures >= 3) {
                    stopSimulation(getString(R.string.mock_delivery_failed));
                    return;
                }
            } else {
                consecutiveDeliveryFailures = 0;
            }
            updateNotification(config);
            handler.postDelayed(this, UPDATE_INTERVAL_MS);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        preferences = new AppPreferences(this);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? AppConstants.ACTION_START_MOCK : intent.getAction();
        if (AppConstants.ACTION_STOP_MOCK.equals(action)) {
            stopSimulation(null);
            return START_NOT_STICKY;
        }

        LocationConfig config = preferences.locationConfig(true);
        startForeground(NOTIFICATION_ID, buildNotification(config));
        if (!MockLocationAppOps.isSelected(this)) {
            stopSimulation(getString(R.string.mock_status_not_selected));
            return START_NOT_STICKY;
        }
        if (locationManager == null || !config.isValid()) {
            stopSimulation(getString(R.string.mock_start_failed));
            return START_NOT_STICKY;
        }

        try {
            installProvider(LocationManager.GPS_PROVIDER);
            installProvider(LocationManager.NETWORK_PROVIDER);
        } catch (SecurityException error) {
            stopSimulation(getString(R.string.mock_start_failed));
            return START_NOT_STICKY;
        }
        if (installedProviders.isEmpty()) {
            stopSimulation(getString(R.string.mock_start_failed));
            return START_NOT_STICKY;
        }

        running = true;
        consecutiveDeliveryFailures = 0;
        preferences.setMockRunning(true);
        broadcastState(true, null);
        handler.removeCallbacks(locationTicker);
        handler.post(locationTicker);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        cleanupProviders();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("InlinedApi")
    private void installProvider(String provider) {
        boolean added = false;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ProviderProperties properties = new ProviderProperties.Builder()
                        .setHasNetworkRequirement(LocationManager.NETWORK_PROVIDER.equals(provider))
                        .setHasSatelliteRequirement(LocationManager.GPS_PROVIDER.equals(provider))
                        .setHasCellRequirement(false)
                        .setHasMonetaryCost(false)
                        .setHasAltitudeSupport(true)
                        .setHasSpeedSupport(true)
                        .setHasBearingSupport(true)
                        .setPowerUsage(ProviderProperties.POWER_USAGE_LOW)
                        .setAccuracy(ProviderProperties.ACCURACY_FINE)
                        .build();
                locationManager.addTestProvider(provider, properties);
            } else {
                locationManager.addTestProvider(
                        provider,
                        LocationManager.NETWORK_PROVIDER.equals(provider),
                        LocationManager.GPS_PROVIDER.equals(provider),
                        false,
                        false,
                        true,
                        true,
                        true,
                        ProviderProperties.POWER_USAGE_LOW,
                        ProviderProperties.ACCURACY_FINE);
            }
            added = true;
            installedProviders.add(provider);
            locationManager.setTestProviderEnabled(provider, true);
        } catch (IllegalArgumentException | SecurityException error) {
            if (added) {
                rollbackProvider(provider);
            }
            if (error instanceof SecurityException) {
                throw (SecurityException) error;
            }
        }
    }

    private void rollbackProvider(String provider) {
        try {
            locationManager.setTestProviderEnabled(provider, false);
        } catch (RuntimeException ignored) {
        }
        try {
            locationManager.removeTestProvider(provider);
        } catch (RuntimeException ignored) {
        }
        installedProviders.remove(provider);
    }

    private void stopSimulation(String error) {
        cleanupProviders();
        broadcastState(false, error);
        stopForeground(true);
        stopSelf();
    }

    private void cleanupProviders() {
        running = false;
        consecutiveDeliveryFailures = 0;
        handler.removeCallbacks(locationTicker);
        if (locationManager != null) {
            for (String provider : installedProviders) {
                try {
                    locationManager.setTestProviderEnabled(provider, false);
                } catch (RuntimeException ignored) {
                }
                try {
                    locationManager.removeTestProvider(provider);
                } catch (RuntimeException ignored) {
                }
            }
        }
        installedProviders.clear();
        if (preferences != null) {
            preferences.setMockRunning(false);
        }
    }

    private void broadcastState(boolean isRunning, String error) {
        Intent state = new Intent(AppConstants.ACTION_MOCK_STATE_CHANGED)
                .setPackage(getPackageName())
                .putExtra(AppConstants.EXTRA_MOCK_RUNNING, isRunning);
        if (error != null) {
            state.putExtra(AppConstants.EXTRA_MOCK_ERROR, error);
        }
        sendBroadcast(state, AppConstants.internalPermission());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(getString(R.string.notification_channel_description));
        NotificationManager manager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification(LocationConfig config) {
        Intent mainIntent = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        String text = String.format(
                Locale.getDefault(),
                getString(R.string.notification_text_format),
                config.latitude,
                config.longitude);
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);
        return builder
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
    }

    private void updateNotification(LocationConfig config) {
        NotificationManager manager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, buildNotification(config));
        }
    }
}
