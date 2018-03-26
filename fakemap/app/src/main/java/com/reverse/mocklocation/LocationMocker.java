package com.reverse.mocklocation;

import android.content.ContentResolver;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import com.baidu.mapapi.model.LatLng;
import com.bigsing.fakemap.Constant;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by sing on 2016/10/14.
 */

public class LocationMocker {
    private static LocationMocker mInstance;
    private ArrayList<BaseMethodHooker> mHookers;

    public synchronized static LocationMocker getInstance() {
        if (mInstance == null) {
            mInstance = new LocationMocker();
        }
        return mInstance;
    }

    public static Location makeLocation(double latitude, double longitude) {
        Location location = new Location("gps");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAccuracy(100.0F);
        location.setTime(System.currentTimeMillis());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        return location;
    }

    public static LatLng getLatLng() {
        //读取配置坐标位置
        XSharedPreferences preferences = new XSharedPreferences(Constant.PACKAGE_THIS, Constant.TAG);
        String s1 = preferences.getString("latitude", "");
        String s2 = preferences.getString("longitude", "");
        LatLng latLng = new LatLng(Double.parseDouble(s1), Double.parseDouble(s2));
        XposedBridge.log("read XSharedPreferences latitude:" + latLng.latitude + " longitude: " + latLng.longitude);
        return latLng;
    }

    public void init(XSharedPreferences preferences, ClassLoader classLoader) {
        mHookers = new ArrayList<>();
        mHookers.add(new requestLocationUpdatesHooker(preferences, classLoader, "requestLocationUpdatesHooker"));
        mHookers.add(new getGpsStatusHooker(preferences, classLoader, "getGpsStatusHooker"));
        if (Build.VERSION.SDK_INT < 24) {
            mHookers.add(new addGpsStatusListenerHooker(preferences, classLoader, "addGpsStatusListenerHooker"));
        }
        mHookers.add(new getLastLocationHooker(preferences, classLoader, "getLastLocationHooker"));
        mHookers.add(new getLastKnownLocationHooker(preferences, classLoader, "getLastKnownLocationHooker"));
        mHookers.add(new addNmeaListenerHooker(preferences, classLoader, "addNmeaListenerHooker"));
        mHookers.add(new callLocationChangedLockedHooker(preferences, classLoader, "callLocationChangedLockedHooker"));
        mHookers.add(new getBestProviderHooker(preferences, classLoader, "getBestProviderHooker"));
        mHookers.add(new getProvidersHooker(preferences, classLoader, "getProvidersHooker"));
        mHookers.add(new isProviderEnabledHooker(preferences, classLoader, "isProviderEnabledHooker"));
    }

    public void hook(ClassLoader classLoader) {
        for (BaseMethodHooker hooker : mHookers) {
            hooker.hook();
        }


//        findAndHookMethod("android.net.NetworkInfo", classLoader, "getType", new Object[] { new n(this) });
//        findAndHookMethod("android.telephony.gsm.GsmCellLocation", classLoader, "getLac", new Object[] { new o(this) });
//        findAndHookMethod("android.telephony.gsm.GsmCellLocation", classLoader, "getCid", new Object[] { new p(this) });
//        findAndHookMethod("android.telephony.cdma.CdmaCellLocation", classLoader, "getNetworkId", new Object[] { new q(this) });
//        findAndHookMethod("android.telephony.cdma.CdmaCellLocation", classLoader, "getBaseStationId", new Object[] { new r(this) });
//        findAndHookMethod("android.telephony.cdma.CdmaCellLocation", classLoader, "getSystemId", new Object[] { new s(this) });
//        findAndHookMethod("android.telephony.cdma.CdmaCellLocation", classLoader, "getBaseStationLatitude", new Object[] { new t(this) });
//        findAndHookMethod("android.telephony.cdma.CdmaCellLocation", classLoader, "getBaseStationLongitude", new Object[] { new u(this) });

        findAndHookMethod("android.location.Location", classLoader, "hasAccuracy", XC_MethodReplacement.returnConstant(true));
        findAndHookMethod("android.location.Location", classLoader, "hasAltitude", XC_MethodReplacement.returnConstant(true));
        findAndHookMethod("android.location.Location", classLoader, "hasBearing", XC_MethodReplacement.returnConstant(true));
        findAndHookMethod("android.location.Location", classLoader, "hasSpeed", XC_MethodReplacement.returnConstant(true));
        findAndHookMethod("android.location.Location", classLoader, "getExtras", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Bundle bundle = new Bundle();
                bundle.putInt("satellites", 12);
                param.setResult(bundle);
            }
        });

        findAndHookMethod("android.telephony.TelephonyManager", classLoader, "getNetworkOperatorName", XC_MethodReplacement.returnConstant("on"));
        findAndHookMethod("android.telephony.TelephonyManager", classLoader, "getSimOperatorName", XC_MethodReplacement.returnConstant("os"));
        findAndHookMethod("android.telephony.TelephonyManager", classLoader, "getSimOperator", XC_MethodReplacement.returnConstant("os"));
        findAndHookMethod("android.telephony.TelephonyManager", classLoader, "getNetworkOperator", XC_MethodReplacement.returnConstant("on"));
        findAndHookMethod("android.telephony.TelephonyManager", classLoader, "getSimCountryIso", XC_MethodReplacement.returnConstant("oc"));
        findAndHookMethod("android.telephony.TelephonyManager", classLoader, "getNetworkCountryIso", XC_MethodReplacement.returnConstant("oc"));
        if (Build.VERSION.SDK_INT < 23) {
            findAndHookMethod("android.telephony.TelephonyManager", classLoader, "getNeighboringCellInfo", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("getNeighboringCellInfo  status=on ");
                    param.setResult(null);
                    //            if (param.getResult() != null) {
                    //                param.setResult(new ArrayList());
                    //            }
                }
            });
        }
        if (Build.VERSION.SDK_INT > 16) {
            findAndHookMethod("android.telephony.TelephonyManager", classLoader, "getAllCellInfo", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("getAllCellInfo status=on ");
                    param.setResult(null);
                }
            });
            findAndHookMethod("android.telephony.PhoneStateListener", classLoader, "onCellInfoChanged", new Object[]{List.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("onCellInfoChanged status=on ");
                    param.setResult(null);
                }
            }});
        }
        findAndHookMethod("android.telephony.TelephonyManager", classLoader, "getCellLocation", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("getCellLocation status=on ");
                param.setResult(null);
            }
        });
        findAndHookMethod("android.location.Location", classLoader, "getLatitude", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                return LocationMocker.getLatLng().latitude;
            }
        });
        findAndHookMethod("android.location.Location", classLoader, "getLongitude", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                return LocationMocker.getLatLng().longitude;
            }
        });
        findAndHookMethod("android.location.Location", classLoader, "getSpeed", XC_MethodReplacement.returnConstant(5.0f));
        findAndHookMethod("android.location.Location", classLoader, "getAccuracy", XC_MethodReplacement.returnConstant(50.0f));
        findAndHookMethod("android.location.Location", classLoader, "getBearing", XC_MethodReplacement.returnConstant(50.0f));
        findAndHookMethod("android.location.Location", classLoader, "getAltitude", XC_MethodReplacement.returnConstant(50.0d));
        findAndHookMethod("android.net.wifi.WifiManager", classLoader, "getScanResults", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("getScanResults status=on ");
                param.setResult(new ArrayList());
            }
        });
        findAndHookMethod("android.net.wifi.WifiInfo", classLoader, "getMacAddress", XC_MethodReplacement.returnConstant("00-00-00-00-00-00-00-E0"));
        findAndHookMethod("android.net.wifi.WifiInfo", classLoader, "getSSID", XC_MethodReplacement.returnConstant("null"));
        findAndHookMethod("android.net.wifi.WifiInfo", classLoader, "getBSSID", XC_MethodReplacement.returnConstant("00:00:00:00:00:00"));
        findAndHookMethod("android.telephony.TelephonyManager", classLoader, "getNetworkType", XC_MethodReplacement.returnConstant(TelephonyManager.NETWORK_TYPE_GPRS));
        findAndHookMethod("android.telephony.TelephonyManager", classLoader, "getPhoneType", XC_MethodReplacement.returnConstant(TelephonyManager.PHONE_TYPE_NONE));
        findAndHookMethod("android.telephony.TelephonyManager", classLoader, "getCurrentPhoneType", XC_MethodReplacement.returnConstant(TelephonyManager.PHONE_TYPE_NONE));

        //findAndHookMethod("android.location.LocationManager", classLoader, "removeUpdates", new Object[] { LocationListener.class, new ca(this) });
        if (Build.VERSION.SDK_INT < 24) {
//            findAndHookMethod("android.location.LocationManager", classLoader, "removeGpsStatusListener", new Object[] { GpsStatus.Listener.class, new cc(this) });
        }
        findAndHookMethod("android.location.GpsStatus", classLoader, "getTimeToFirstFix", XC_MethodReplacement.returnConstant(1080));

        findAndHookMethod("android.provider.Settings.Secure", classLoader, "getString", ContentResolver.class, String.class, new XC_MethodHook() {
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param)
                    throws Throwable {
                if (((String) param.args[1]).equals("mock_location"))
                    param.setResult("0");
            }
        });
        if (Build.VERSION.SDK_INT >= 18) {
            findAndHookMethod("android.location.Location", classLoader, "isFromMockProvider", new XC_MethodHook() {
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param)
                        throws Throwable {
                    param.setResult(Boolean.valueOf(false));
                }
            });
            findAndHookMethod("android.telephony.TelephonyManager", classLoader, "getDataState", new XC_MethodHook() {
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    param.setResult(2);
                }
            });
            findAndHookMethod("android.telephony.TelephonyManager", classLoader, "getSimState", new XC_MethodHook() {
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    param.setResult(0);
                }
            });
            findAndHookMethod("android.telephony.PhoneStateListener", classLoader, "onCellLocationChanged", CellLocation.class, new XC_MethodHook() {
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    GsmCellLocation localGsmCellLocation = new GsmCellLocation();
                    localGsmCellLocation.setLacAndCid(0, 0);
                    param.setResult(localGsmCellLocation);
                }
            });
            if (Build.VERSION.SDK_INT > 22) {
                findAndHookMethod("android.telephony.TelephonyManager", classLoader, "getPhoneCount", new XC_MethodHook() {
                    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                        param.setResult(1);
                    }
                });
            }


            findAndHookMethod("android.net.wifi.WifiManager", classLoader, "getWifiState", new XC_MethodHook() {
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    param.setResult(1);
                }
            });
            findAndHookMethod("android.net.wifi.WifiManager", classLoader, "isWifiEnabled", new XC_MethodHook() {
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    param.setResult(true);
                }
            });
            findAndHookMethod("android.net.NetworkInfo", classLoader, "getTypeName", XC_MethodReplacement.returnConstant("WIFI"));//"MOBILE"
            findAndHookMethod("android.net.NetworkInfo", classLoader, "isConnectedOrConnecting", XC_MethodReplacement.returnConstant(true));
            findAndHookMethod("android.net.NetworkInfo", classLoader, "isConnected", XC_MethodReplacement.returnConstant(true));

            findAndHookMethod("android.net.NetworkInfo", classLoader, "isAvailable", XC_MethodReplacement.returnConstant(true));
            findAndHookMethod("android.telephony.CellInfo", classLoader, "isRegistered", XC_MethodReplacement.returnConstant(true));
        }

    }
}
