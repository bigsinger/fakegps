package com.reverse.mocklocation;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

import com.baidu.mapapi.model.LatLng;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by sing on 2016/10/13.
 */

public class getBestProviderHooker extends BaseMethodHooker {
    public getBestProviderHooker(XSharedPreferences preferences, ClassLoader classLoader, String paramString) {
        super(preferences, classLoader, paramString);
    }

    @Override
    public void hook() {
        XposedHelpers.findAndHookMethod(LocationManager.class, "getBestProvider", new Object[]{Criteria.class, Boolean.TYPE, this});
    }

    @Override
    protected void beforeCall(MethodHookParam paramMethodHookParam) {

    }

    @Override
    protected void afterCall(MethodHookParam paramMethodHookParam) {
        XposedBridge.log("LM:gbp return GPS_PROVIDER directly: " + this.mParamString);
        LatLng latLng = LocationMocker.getLatLng();
        Location location = LocationMocker.makeLocation(latLng.latitude, latLng.longitude);
        paramMethodHookParam.setResult(location);
    }
}
