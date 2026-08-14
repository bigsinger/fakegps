package com.bigsing.fakemap.ui;

import android.Manifest;
import android.app.Activity;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bigsing.fakemap.BuildConfig;
import com.bigsing.fakemap.R;
import com.bigsing.fakemap.core.AppConstants;
import com.bigsing.fakemap.core.GeoPoint;
import com.bigsing.fakemap.core.LocationConfig;
import com.bigsing.fakemap.core.MapProviderSelector;
import com.bigsing.fakemap.core.TargetPackages;
import com.bigsing.fakemap.core.XposedStatus;
import com.bigsing.fakemap.data.AppPreferences;
import com.bigsing.fakemap.data.XposedPreferenceBridge;
import com.bigsing.fakemap.mock.MockLocationAppOps;
import com.bigsing.fakemap.mock.MockLocationService;

import java.util.Locale;
import java.util.Set;

@SuppressLint("ObsoleteSdkInt")
public final class MainActivity extends Activity {
    private static final int REQUEST_MAP = 101;
    private static final int REQUEST_LOCATION_PERMISSIONS = 102;

    private AppPreferences preferences;
    private Switch xposedEnabled;
    private EditText targetPackages;
    private EditText latitude;
    private EditText longitude;
    private EditText accuracy;
    private EditText altitude;
    private EditText speed;
    private EditText bearing;
    private Spinner mapProvider;
    private EditText amapKey;
    private EditText amapSecurity;
    private EditText tileUrl;
    private EditText tileAttribution;
    private EditText tileAttributionUrl;
    private TextView mockStatus;
    private boolean receiverRegistered;
    private boolean startAfterPermission;

    private final BroadcastReceiver mockStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean running = intent.getBooleanExtra(AppConstants.EXTRA_MOCK_RUNNING, false);
            String error = intent.getStringExtra(AppConstants.EXTRA_MOCK_ERROR);
            updateMockStatus(running);
            if (error != null && !error.isEmpty()) {
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(
                        MainActivity.this,
                        running ? R.string.mock_started : R.string.mock_stopped,
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SystemBarInsets.applyToContentRoot(this);
        preferences = new AppPreferences(this);
        bindViews();
        loadPreferences();
        configureStatus();
        configureActions();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerMockReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMockStatus(preferences.isMockRunning());
    }

    @Override
    protected void onStop() {
        if (receiverRegistered) {
            unregisterReceiver(mockStateReceiver);
            receiverRegistered = false;
        }
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MAP && resultCode == RESULT_OK && data != null) {
            double selectedLatitude = data.getDoubleExtra(MapActivity.EXTRA_LATITUDE, Double.NaN);
            double selectedLongitude = data.getDoubleExtra(MapActivity.EXTRA_LONGITUDE, Double.NaN);
            GeoPoint selected = new GeoPoint(selectedLatitude, selectedLongitude);
            if (selected.isValid()) {
                latitude.setText(formatDouble(selected.latitude));
                longitude.setText(formatDouble(selected.longitude));
                saveConfiguration(false);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_LOCATION_PERMISSIONS || !startAfterPermission) {
            return;
        }
        startAfterPermission = false;
        if (hasLocationPermission()) {
            startMockService();
        } else {
            Toast.makeText(this, R.string.location_permission_required, Toast.LENGTH_LONG).show();
        }
    }

    private void bindViews() {
        xposedEnabled = findViewById(R.id.switch_xposed);
        targetPackages = findViewById(R.id.edit_target_packages);
        latitude = findViewById(R.id.edit_latitude);
        longitude = findViewById(R.id.edit_longitude);
        accuracy = findViewById(R.id.edit_accuracy);
        altitude = findViewById(R.id.edit_altitude);
        speed = findViewById(R.id.edit_speed);
        bearing = findViewById(R.id.edit_bearing);
        mapProvider = findViewById(R.id.spinner_map_provider);
        amapKey = findViewById(R.id.edit_amap_key);
        amapSecurity = findViewById(R.id.edit_amap_security);
        tileUrl = findViewById(R.id.edit_osm_tile_url);
        tileAttribution = findViewById(R.id.edit_tile_attribution);
        tileAttributionUrl = findViewById(R.id.edit_tile_attribution_url);
        mockStatus = findViewById(R.id.text_mock_status);
    }

    private void loadPreferences() {
        LocationConfig config = preferences.locationConfig(true);
        xposedEnabled.setChecked(config.enabled);
        targetPackages.setText(preferences.targetPackagesText());
        latitude.setText(formatDouble(config.latitude));
        longitude.setText(formatDouble(config.longitude));
        accuracy.setText(formatFloat(config.accuracy));
        altitude.setText(formatDouble(config.altitude));
        speed.setText(formatFloat(config.speed));
        bearing.setText(formatFloat(config.bearing));
        mapProvider.setSelection(providerIndex(preferences.mapProvider()));
        amapKey.setText(preferences.amapKey());
        amapSecurity.setText(preferences.amapSecurityCode());
        tileUrl.setText(preferences.tileUrl());
        tileAttribution.setText(preferences.tileAttribution());
        tileAttributionUrl.setText(preferences.tileAttributionUrl());
    }

    private void configureStatus() {
        TextView version = findViewById(R.id.text_version);
        version.setText(getString(
                R.string.version_format,
                appVersion(),
                BuildConfig.XPOSED_API_KIND));

        TextView xposedStatus = findViewById(R.id.text_xposed_status);
        if (XposedStatus.isActive()) {
            xposedStatus.setText(R.string.xposed_status_active);
            xposedStatus.setTextColor(getColorCompat(R.color.color_success));
        } else if ("modern".equals(BuildConfig.XPOSED_API_KIND)
                && XposedPreferenceBridge.isConnected()) {
            xposedStatus.setText(R.string.xposed_status_modern_connected);
            xposedStatus.setTextColor(getColorCompat(R.color.color_primary_dark));
        } else {
            xposedStatus.setText("modern".equals(BuildConfig.XPOSED_API_KIND)
                    ? R.string.xposed_status_modern
                    : R.string.xposed_status_legacy_unknown);
            xposedStatus.setTextColor(getColorCompat(R.color.color_warning));
        }
        updateMockStatus(preferences.isMockRunning());
    }

    private void configureActions() {
        findViewById(R.id.button_save).setOnClickListener(view -> saveConfiguration(true));
        findViewById(R.id.button_pick_map).setOnClickListener(view -> openMap());
        findViewById(R.id.button_developer_options).setOnClickListener(
                view -> openDeveloperOptions());
        findViewById(R.id.button_start_mock).setOnClickListener(view -> prepareMockStart());
        findViewById(R.id.button_stop_mock).setOnClickListener(view -> stopMockService());
        findViewById(R.id.button_help).setOnClickListener(
                view -> startActivity(new Intent(this, HelpActivity.class)));
    }

    private boolean saveConfiguration(boolean showConfirmation) {
        TargetPackages.ParseResult targetResult = TargetPackages.parse(
                targetPackages.getText().toString());
        if (!targetResult.valid) {
            targetPackages.setError(getString(R.string.invalid_target_packages));
            return false;
        }

        LocationConfig config;
        try {
            config = new LocationConfig(
                    true,
                    xposedEnabled.isChecked(),
                    parseDouble(latitude),
                    parseDouble(longitude),
                    parseFloat(accuracy),
                    parseDouble(altitude),
                    parseFloat(speed),
                    parseFloat(bearing),
                    System.currentTimeMillis());
        } catch (NumberFormatException error) {
            Toast.makeText(this, R.string.invalid_number, Toast.LENGTH_LONG).show();
            return false;
        }
        if (!config.point().isValid()) {
            Toast.makeText(this, R.string.invalid_coordinate, Toast.LENGTH_LONG).show();
            return false;
        }
        if (!config.isValid()) {
            Toast.makeText(this, R.string.invalid_number, Toast.LENGTH_LONG).show();
            return false;
        }
        if (!saveMapSettings()) {
            return false;
        }

        boolean remoteSynchronized = preferences.saveLocationConfig(
                config, targetResult.packages);
        targetPackages.setText(TargetPackages.format(targetResult.packages));
        bearing.setText(formatFloat(config.bearing));
        if (showConfirmation) {
            int message = "modern".equals(BuildConfig.XPOSED_API_KIND)
                    && !remoteSynchronized
                    ? R.string.configuration_saved_pending
                    : R.string.configuration_saved;
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
        return true;
    }

    private boolean saveMapSettings() {
        String selectedProvider = providerValue(mapProvider.getSelectedItemPosition());
        String selectedTileUrl = tileUrl.getText().toString().trim();
        String selectedAttribution = tileAttribution.getText().toString().trim();
        String selectedAttributionUrl = tileAttributionUrl.getText().toString().trim();
        if (!validTileUrl(selectedTileUrl)) {
            tileUrl.setError(getString(R.string.invalid_tile_url));
            return false;
        }
        if (selectedAttribution.isEmpty() || !validHttpsUrl(selectedAttributionUrl)) {
            tileAttribution.setError(getString(R.string.invalid_tile_attribution));
            return false;
        }
        preferences.saveMapSettings(
                selectedProvider,
                amapKey.getText().toString(),
                amapSecurity.getText().toString(),
                selectedTileUrl,
                selectedAttribution,
                selectedAttributionUrl);
        return true;
    }

    private void openMap() {
        if (!saveConfiguration(false)) {
            return;
        }
        startActivityForResult(new Intent(this, MapActivity.class), REQUEST_MAP);
    }

    private void prepareMockStart() {
        if (!saveConfiguration(false)) {
            return;
        }
        if (!MockLocationAppOps.isSelected(this)) {
            Toast.makeText(this, R.string.mock_status_not_selected, Toast.LENGTH_LONG).show();
            openDeveloperOptions();
            return;
        }
        if (!hasLocationPermission()) {
            startAfterPermission = true;
            requestRuntimePermissions();
            return;
        }
        startMockService();
    }

    private void startMockService() {
        Intent intent = new Intent(this, MockLocationService.class)
                .setAction(AppConstants.ACTION_START_MOCK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void stopMockService() {
        Intent intent = new Intent(this, MockLocationService.class)
                .setAction(AppConstants.ACTION_STOP_MOCK);
        startService(intent);
    }

    private void openDeveloperOptions() {
        try {
            startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
        } catch (RuntimeException error) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    }

    private boolean hasLocationPermission() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                    new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.POST_NOTIFICATIONS
                    },
                    REQUEST_LOCATION_PERMISSIONS);
        } else {
            requestPermissions(
                    new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQUEST_LOCATION_PERMISSIONS);
        }
    }

    private void updateMockStatus(boolean running) {
        if (running) {
            mockStatus.setText(R.string.mock_status_running);
            mockStatus.setTextColor(getColorCompat(R.color.color_success));
        } else if (!MockLocationAppOps.isSelected(this)) {
            mockStatus.setText(R.string.mock_status_not_selected);
            mockStatus.setTextColor(getColorCompat(R.color.color_warning));
        } else {
            mockStatus.setText(R.string.mock_status_stopped);
            mockStatus.setTextColor(getColorCompat(R.color.color_text_secondary));
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerMockReceiver() {
        IntentFilter filter = new IntentFilter(AppConstants.ACTION_MOCK_STATE_CHANGED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                    mockStateReceiver,
                    filter,
                    AppConstants.internalPermission(),
                    null,
                    Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(
                    mockStateReceiver,
                    filter,
                    AppConstants.internalPermission(),
                    null);
        }
        receiverRegistered = true;
    }

    private String appVersion() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            return info.versionName == null ? "" : info.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
            return "";
        }
    }

    private static int providerIndex(String provider) {
        if (MapProviderSelector.AMAP.equals(provider)) {
            return 1;
        }
        if (MapProviderSelector.OSM.equals(provider)) {
            return 2;
        }
        return 0;
    }

    private static String providerValue(int index) {
        if (index == 1) {
            return MapProviderSelector.AMAP;
        }
        if (index == 2) {
            return MapProviderSelector.OSM;
        }
        return MapProviderSelector.AUTO;
    }

    private static boolean validTileUrl(String value) {
        if (value == null) {
            return false;
        }
        Uri parsed = Uri.parse(value.trim());
        return "https".equalsIgnoreCase(parsed.getScheme())
                && value.contains("{z}")
                && value.contains("{x}")
                && value.contains("{y}");
    }

    private static boolean validHttpsUrl(String value) {
        Uri parsed = Uri.parse(value);
        return "https".equalsIgnoreCase(parsed.getScheme())
                && parsed.getHost() != null
                && parsed.getUserInfo() == null;
    }

    private static double parseDouble(EditText view) {
        return Double.parseDouble(view.getText().toString().trim().replace(',', '.'));
    }

    private static float parseFloat(EditText view) {
        return Float.parseFloat(view.getText().toString().trim().replace(',', '.'));
    }

    private static String formatDouble(double value) {
        return String.format(Locale.US, "%.6f", value);
    }

    private static String formatFloat(float value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private int getColorCompat(int resourceId) {
        return getResources().getColor(resourceId, getTheme());
    }
}
