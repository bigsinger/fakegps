package com.reverse.mocklocation;

import android.location.LocationManager;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by sing on 2016/10/13.
 */

public class isProviderEnabledHooker extends BaseMethodHooker {
    public isProviderEnabledHooker(XSharedPreferences preferences, ClassLoader classLoader, String paramString) {
        super(preferences, classLoader, paramString);
    }

    @Override
    public void hook() {
        XposedHelpers.findAndHookMethod(LocationManager.class, "isProviderEnabled", new Object[]{String.class, this});
    }

    @Override
    protected void beforeCall(MethodHookParam paramMethodHookParam) {

    }

    @Override
    protected void afterCall(MethodHookParam paramMethodHookParam) {
        if ("gps".equals((String) paramMethodHookParam.args[0])) {
            XposedBridge.log("LM:ipe status=on, provider=GPS " + this.mParamString);
            paramMethodHookParam.setResult(true);
        }
    }
}
