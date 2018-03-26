package com.reverse.mocklocation;

import android.location.GpsStatus;
import android.location.LocationManager;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by sing on 2016/10/13.
 */

public class getGpsStatusHooker extends BaseMethodHooker {
    public getGpsStatusHooker(XSharedPreferences preferences, ClassLoader classLoader, String paramString) {
        super(preferences, classLoader, paramString);
    }

    @Override
    public void hook() {
        XposedHelpers.findAndHookMethod(LocationManager.class, "getGpsStatus", new Object[]{GpsStatus.class, this});
    }

    @Override
    protected void beforeCall(MethodHookParam paramMethodHookParam) {

    }

    @Override
    protected void afterCall(MethodHookParam paramMethodHookParam) {
        XposedBridge.log("LM:ggs status=on : " + this.mParamString);
        GpsStatus result = (GpsStatus) paramMethodHookParam.getResult();
        //// TODO: 2016/10/13
//        XposedHelpers.callMethod((Object)result, "setStatus", new Object[] { 5, { 1, 2, 3, 4, 5 }, { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f }, { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f }, { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f }, 31, 31, 31 });
        paramMethodHookParam.args[0] = result;
        paramMethodHookParam.setResult((Object) result);
    }
}
