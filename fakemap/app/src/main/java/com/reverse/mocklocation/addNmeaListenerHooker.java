package com.reverse.mocklocation;

import android.location.GpsStatus;
import android.location.LocationManager;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by sing on 2016/10/13.
 */

public class addNmeaListenerHooker extends BaseMethodHooker {
    public addNmeaListenerHooker(XSharedPreferences preferences, ClassLoader classLoader, String paramString) {
        super(preferences, classLoader, paramString);
    }

    @Override
    public void hook() {
        XposedHelpers.findAndHookMethod(LocationManager.class, "addNmeaListener", new Object[]{GpsStatus.NmeaListener.class, this});
    }

    @Override
    protected void beforeCall(MethodHookParam paramMethodHookParam) {
        XposedBridge.log("LM:anl status=on: " + this.mParamString);
        paramMethodHookParam.setResult(false);
    }

    @Override
    protected void afterCall(MethodHookParam paramMethodHookParam) {

    }
}
