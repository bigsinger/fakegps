package com.reverse.mocklocation;

import android.location.LocationManager;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

/**
 * Created by sing on 2016/10/13.
 */

public class getProvidersHooker extends BaseMethodHooker {
    public getProvidersHooker(XSharedPreferences preferences, ClassLoader classLoader, String paramString) {
        super(preferences, classLoader, paramString);
    }

    @Override
    public void hook() {
        XposedBridge.hookAllMethods(LocationManager.class, "getProviders", this);
    }

    @Override
    protected void beforeCall(MethodHookParam paramMethodHookParam) {

    }

    @Override
    protected void afterCall(MethodHookParam param) {
//        ArrayList localArrayList = new ArrayList();
//        localArrayList.add("gps");
//        param.setResult(localArrayList);

        Object result = param.getResult();
        boolean isEmpty = false;
        if (result == null) {
            isEmpty = true;
        } else if (((List) result).contains("gps")) {
            return;
        }
        if (isEmpty == true) {
            result = new ArrayList<>();
        }
        XposedBridge.log("LM:gp add GPS_PROVIDER to the list" + this.mParamString);
        ((List<String>) result).add("gps");
        param.setResult(result);
    }
}
