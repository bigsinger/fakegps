package com.reverse.mocklocation;

import android.location.LocationManager;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by sing on 2016/10/13.
 */

public class getLastKnownLocationHooker extends BaseMethodHooker {
    public getLastKnownLocationHooker(XSharedPreferences preferences, ClassLoader classLoader, String paramString) {
        super(preferences, classLoader, paramString);
    }

    @Override
    public void hook() {
        XposedHelpers.findAndHookMethod(LocationManager.class, "getLastKnownLocation", new Object[]{String.class, this});
    }

    @Override
    protected void beforeCall(MethodHookParam paramMethodHookParam) {

    }

    @Override
    protected void afterCall(MethodHookParam paramMethodHookParam) {
        XposedBridge.log("LM:gbp return GPS_PROVIDER directly: " + this.mParamString);
        paramMethodHookParam.setResult("gps");
    }
}
