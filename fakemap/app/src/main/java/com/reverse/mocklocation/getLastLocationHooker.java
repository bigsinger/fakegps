package com.reverse.mocklocation;

import android.location.Location;
import android.location.LocationManager;

import com.baidu.mapapi.model.LatLng;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by sing on 2016/10/13.
 */

public class getLastLocationHooker extends BaseMethodHooker {
    public getLastLocationHooker(XSharedPreferences preferences, ClassLoader classLoader, String paramString) {
        super(preferences, classLoader, paramString);
    }

    @Override
    public void hook() {
        XposedHelpers.findAndHookMethod(LocationManager.class, "getLastLocation", new Object[]{this});
    }

    @Override
    protected void beforeCall(MethodHookParam param) {

    }

    @Override
    protected void afterCall(MethodHookParam param) {
        //// TODO: 2016/10/13 从配置中读取出配置坐标并作为结果返回
        // 这里设置Location
        LatLng latLng = LocationMocker.getLatLng();
        Location location = LocationMocker.makeLocation(latLng.latitude, latLng.longitude);
        XposedBridge.log("getLastLocation: latitude: " + latLng.latitude + " longitude: " + latLng.longitude);
        param.setResult(location);
    }
}
