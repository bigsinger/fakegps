package com.reverse.mocklocation;

import android.location.Location;

import com.baidu.mapapi.model.LatLng;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by sing on 2016/10/13.
 */

public class callLocationChangedLockedHooker extends BaseMethodHooker {
    public callLocationChangedLockedHooker(XSharedPreferences preferences, ClassLoader classLoader, String paramString) {
        super(preferences, classLoader, paramString);
    }

    @Override
    public void hook() {
        XposedHelpers.findAndHookMethod("com.android.server.LocationManagerService$Receiver", this.mClassLoader, "callLocationChangedLocked", new Object[]{Location.class, this});
    }

    @Override
    protected void beforeCall(MethodHookParam param) {
        String str = (String) XposedHelpers.getObjectField(param.thisObject, "mPackageName");
        if (str == null)
            return;

        // 这里设置Location

        LatLng latLng = LocationMocker.getLatLng();
        Location location = LocationMocker.makeLocation(latLng.latitude, latLng.longitude);
        XposedBridge.log("LocationManagerService$Receiver: status=on latitude: " + latLng.latitude + " longitude: " + latLng.longitude);
        param.args[0] = location;
    }

    @Override
    protected void afterCall(MethodHookParam param) {

    }
}
