package com.bigsing.fakemap.ui;

import android.app.Activity;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bigsing.fakemap.R;
import com.bigsing.fakemap.core.CoordinateTransform;
import com.bigsing.fakemap.core.GeoPoint;
import com.bigsing.fakemap.core.LocationConfig;
import com.bigsing.fakemap.core.MapProviderSelector;
import com.bigsing.fakemap.data.AppPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

@SuppressLint("ObsoleteSdkInt")
public final class MapActivity extends Activity {
    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";

    private static final String ASSET_HOST = "appassets.androidplatform.net";
    private static final String MAP_URL = "https://" + ASSET_HOST + "/assets/map.html";
    private static final String BRIDGE_SCHEME = "fakemap";
    private static final String BRIDGE_HOST = "bridge";

    private WebView webView;
    private ProgressBar progress;
    private TextView status;
    private TextView coordinate;
    private GeoPoint selectedWgs84;
    private String allowedAttributionHost = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        SystemBarInsets.applyToContentRoot(this);
        webView = findViewById(R.id.web_map);
        progress = findViewById(R.id.progress_map);
        status = findViewById(R.id.text_map_status);
        coordinate = findViewById(R.id.text_map_coordinate);

        AppPreferences preferences = new AppPreferences(this);
        LocationConfig config = preferences.locationConfig(true);
        selectedWgs84 = config.point();
        updateCoordinateText();
        configureButtons();
        configureWebView(preferences);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    protected void onPause() {
        if (webView != null) {
            webView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.loadUrl("about:blank");
            webView.clearHistory();
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    private void configureButtons() {
        findViewById(R.id.button_map_cancel).setOnClickListener(view -> finish());
        findViewById(R.id.button_map_use).setOnClickListener(view -> {
            Intent result = new Intent()
                    .putExtra(EXTRA_LATITUDE, selectedWgs84.latitude)
                    .putExtra(EXTRA_LONGITUDE, selectedWgs84.longitude);
            setResult(RESULT_OK, result);
            finish();
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView(AppPreferences preferences) {
        String attributionHost = Uri.parse(preferences.tileAttributionUrl()).getHost();
        allowedAttributionHost = attributionHost == null
                ? ""
                : attributionHost.toLowerCase(Locale.ROOT);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setDomStorageEnabled(false);
        settings.setDatabaseEnabled(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setGeolocationEnabled(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setUserAgentString(settings.getUserAgentString() + " FakeMap/2.0");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setSafeBrowsingEnabled(true);
        }

        webView.setSaveEnabled(false);
        webView.removeJavascriptInterface("searchBoxJavaBridge_");
        webView.removeJavascriptInterface("accessibility");
        webView.removeJavascriptInterface("accessibilityTraversal");
        webView.setWebViewClient(new AssetWebViewClient(preferences));
        webView.loadUrl(MAP_URL);
    }

    private JSONObject buildMapOptions(AppPreferences preferences) throws JSONException {
        boolean hasAmapCredentials = !preferences.amapKey().isEmpty()
                && !preferences.amapSecurityCode().isEmpty();
        String resolvedProvider = MapProviderSelector.resolve(
                preferences.mapProvider(),
                selectedWgs84,
                hasAmapCredentials);
        GeoPoint displayPoint = MapProviderSelector.AMAP.equals(resolvedProvider)
                ? CoordinateTransform.wgs84ToGcj02(selectedWgs84)
                : selectedWgs84;

        JSONObject options = new JSONObject();
        options.put("provider", resolvedProvider);
        options.put("latitude", displayPoint.latitude);
        options.put("longitude", displayPoint.longitude);
        options.put("wgsLatitude", selectedWgs84.latitude);
        options.put("wgsLongitude", selectedWgs84.longitude);
        options.put("zoom", 15);
        options.put("tileUrl", preferences.tileUrl());
        options.put("tileAttribution", preferences.tileAttribution());
        options.put("tileAttributionUrl", preferences.tileAttributionUrl());
        options.put("amapKey", preferences.amapKey());
        options.put("amapSecurity", preferences.amapSecurityCode());
        options.put("languageTag", Locale.getDefault().toLanguageTag());
        options.put("amapLanguageCode", amapLanguageCode(Locale.getDefault()));
        options.put("amapLogoLanguage", isChinese() ? "zh" : "en");
        options.put("fallbackMessage", getString(R.string.map_amap_fallback));
        options.put("tileErrorMessage", getString(R.string.map_tile_error));
        return options;
    }

    private void initializeMap(AppPreferences preferences) {
        try {
            JSONObject options = buildMapOptions(preferences);
            if (MapProviderSelector.AMAP.equals(preferences.mapProvider())
                    && (preferences.amapKey().isEmpty()
                    || preferences.amapSecurityCode().isEmpty())) {
                status.setText(R.string.map_amap_credentials_missing);
            }
            webView.evaluateJavascript("window.FakeMap.init(" + options + ");", null);
        } catch (JSONException error) {
            showMapError(R.string.map_internal_error);
        }
    }

    private void updateCoordinateText() {
        coordinate.setText(getString(
                R.string.map_coordinate_format,
                selectedWgs84.latitude,
                selectedWgs84.longitude));
    }

    private void showMapError(int messageResource) {
        progress.setVisibility(View.GONE);
        status.setText(messageResource);
        status.setTextColor(getResources().getColor(R.color.color_error, getTheme()));
    }

    private void handleBridge(Uri uri) {
        if (!BRIDGE_SCHEME.equals(uri.getScheme())
                || !BRIDGE_HOST.equals(uri.getHost())
                || uri.getPort() != -1
                || uri.getUserInfo() != null) {
            return;
        }
        String path = uri.getPath();
        if ("/picked".equals(path)) {
            handlePicked(uri);
        } else if ("/ready".equals(path)) {
            progress.setVisibility(View.GONE);
            String fallback = uri.getQueryParameter("fallback");
            String provider = uri.getQueryParameter("provider");
            if (fallback != null && !fallback.isEmpty()) {
                status.setText(R.string.map_ready_international_fallback);
            } else if (MapProviderSelector.AMAP.equals(provider)) {
                status.setText(R.string.map_ready_amap);
            } else {
                status.setText(R.string.map_ready_international);
            }
            status.setTextColor(getResources().getColor(
                    R.color.color_text_primary, getTheme()));
        } else if ("/error".equals(path)) {
            showMapError(R.string.map_network_error);
        }
    }

    private void handlePicked(Uri uri) {
        String coordinateSystem = uri.getQueryParameter("coordinateSystem");
        if (!"wgs84".equals(coordinateSystem) && !"gcj02".equals(coordinateSystem)) {
            showMapError(R.string.map_coordinate_system_error);
            return;
        }
        try {
            GeoPoint point = new GeoPoint(
                    Double.parseDouble(uri.getQueryParameter("latitude")),
                    Double.parseDouble(uri.getQueryParameter("longitude")));
            if ("gcj02".equals(coordinateSystem)) {
                point = CoordinateTransform.gcj02ToWgs84(point);
            }
            if (point.isValid()) {
                selectedWgs84 = point;
                updateCoordinateText();
            }
        } catch (NullPointerException | NumberFormatException ignored) {
            showMapError(R.string.map_coordinate_system_error);
        }
    }

    private boolean handleNavigation(Uri uri) {
        if (isLocalMapUri(uri)) {
            return false;
        }
        if (BRIDGE_SCHEME.equals(uri.getScheme())) {
            handleBridge(uri);
            return true;
        }
        if (isAllowedExternalLink(uri)) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri)
                        .addCategory(Intent.CATEGORY_BROWSABLE);
                startActivity(intent);
            } catch (ActivityNotFoundException ignored) {
            }
        }
        return true;
    }

    private static boolean isLocalMapUri(Uri uri) {
        return "https".equalsIgnoreCase(uri.getScheme())
                && ASSET_HOST.equals(uri.getHost())
                && uri.getPort() == -1
                && "/assets/map.html".equals(uri.getPath())
                && uri.getUserInfo() == null;
    }

    private boolean isAllowedExternalLink(Uri uri) {
        if (!"https".equalsIgnoreCase(uri.getScheme())
                || uri.getHost() == null
                || uri.getUserInfo() != null) {
            return false;
        }
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        return host.equals("openstreetmap.org")
                || host.endsWith(".openstreetmap.org")
                || host.equals("amap.com")
                || host.endsWith(".amap.com")
                || host.equals("autonavi.com")
                || host.endsWith(".autonavi.com")
                || host.equals(allowedAttributionHost);
    }

    private static boolean isChinese() {
        return Locale.getDefault().getLanguage().toLowerCase(Locale.ROOT).startsWith("zh");
    }

    private static String amapLanguageCode(Locale locale) {
        String language = locale.getLanguage().toLowerCase(Locale.ROOT);
        if ("zh".equals(language)) {
            String script = locale.getScript();
            String country = locale.getCountry();
            return "Hant".equalsIgnoreCase(script)
                    || "TW".equalsIgnoreCase(country)
                    || "HK".equalsIgnoreCase(country)
                    || "MO".equalsIgnoreCase(country)
                    ? "zh-Hant"
                    : "zh";
        }
        switch (language) {
            case "en":
            case "es":
            case "pt":
            case "fr":
            case "de":
            case "th":
            case "ja":
            case "ko":
            case "ar":
            case "tr":
            case "it":
            case "ru":
            case "ms":
            case "id":
            case "vi":
                return language;
            default:
                return "en";
        }
    }

    private final class AssetWebViewClient extends WebViewClient {
        private final AppPreferences preferences;

        private AssetWebViewClient(AppPreferences preferences) {
            this.preferences = preferences;
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(
                WebView view,
                WebResourceRequest request) {
            Uri uri = request.getUrl();
            String path = uri.getPath();
            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || !ASSET_HOST.equals(uri.getHost())
                    || uri.getPort() != -1
                    || path == null
                    || !path.startsWith("/assets/")) {
                return null;
            }
            String assetPath = path.substring("/assets/".length());
            if (assetPath.contains("..") || assetPath.indexOf('\\') >= 0) {
                return null;
            }
            try {
                InputStream input = getAssets().open(assetPath);
                return new WebResourceResponse(mimeType(assetPath), "UTF-8", input);
            } catch (IOException ignored) {
                return null;
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return !request.isForMainFrame() || handleNavigation(request.getUrl());
        }

        @Override
        @SuppressWarnings("deprecation")
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return handleNavigation(Uri.parse(url));
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (MAP_URL.equals(url)) {
                progress.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (MAP_URL.equals(url)) {
                initializeMap(preferences);
            }
        }

        @Override
        public void onReceivedError(
                WebView view,
                WebResourceRequest request,
                WebResourceError error) {
            if (request.isForMainFrame()) {
                showMapError(R.string.map_network_error);
            }
        }

        @Override
        public void onReceivedSslError(
                WebView view,
                SslErrorHandler handler,
                SslError error) {
            handler.cancel();
            showMapError(R.string.map_tls_error);
        }

        private String mimeType(String assetPath) {
            if (assetPath.endsWith(".html")) {
                return "text/html";
            }
            if (assetPath.endsWith(".js")) {
                return "application/javascript";
            }
            if (assetPath.endsWith(".css")) {
                return "text/css";
            }
            if (assetPath.endsWith(".png")) {
                return "image/png";
            }
            if (assetPath.endsWith(".svg")) {
                return "image/svg+xml";
            }
            return "application/octet-stream";
        }
    }
}
