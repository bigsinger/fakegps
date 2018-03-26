package com.reverse.mocklocation;

import android.location.GpsStatus;
import android.location.LocationManager;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;


/**
 * Created by sing on 2016/10/13.
 */

public class addGpsStatusListenerHooker extends BaseMethodHooker {
    public addGpsStatusListenerHooker(XSharedPreferences preferences, ClassLoader classLoader, String paramString) {
        super(preferences, classLoader, paramString);
    }

    @Override
    public void hook() {
        XposedHelpers.findAndHookMethod(LocationManager.class, "addGpsStatusListener", new Object[]{GpsStatus.Listener.class, this});
    }

    @Override
    protected void beforeCall(MethodHookParam paramMethodHookParam) {

    }

    @Override
    protected void afterCall(MethodHookParam param) {
        if (((Boolean) param.getResult()).booleanValue()) {
            GpsStatus.Listener listener = (GpsStatus.Listener) param.args[0];
            if (listener != null) {
                XposedHelpers.callMethod(listener, "onGpsStatusChanged", GpsStatus.GPS_EVENT_STARTED);
                XposedHelpers.callMethod(listener, "onGpsStatusChanged", GpsStatus.GPS_EVENT_FIRST_FIX);
                XposedBridge.log("LM:agsl addGpsStatusListener: " + this.mParamString);
            }
        }
    }
}
