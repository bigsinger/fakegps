package com.reverse.mocklocation;

import android.app.PendingIntent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;

import com.baidu.mapapi.model.LatLng;

import java.lang.reflect.Method;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedHelpers.findMethodBestMatch;

/**
 * Created by sing on 2016/10/13.
 */

public class requestLocationUpdatesHooker extends BaseMethodHooker {
    public requestLocationUpdatesHooker(XSharedPreferences preferences, ClassLoader classLoader, String paramString) {
        super(preferences, classLoader, paramString);
    }

    @Override
    public void hook() {
        XposedHelpers.findAndHookMethod(LocationManager.class, "requestLocationUpdates", new Object[]{XposedHelpers.findClass("android.location.LocationRequest", this.mClassLoader), LocationListener.class, Looper.class, PendingIntent.class, this});
    }

    @Override
    protected void beforeCall(MethodHookParam param) {

    }

    @Override
    protected void afterCall(MethodHookParam param) {
        LocationListener listener = (LocationListener) param.args[1];
        if (listener != null) {
            //// TODO: 2016/10/13  这里需要设置Location
            // 这里设置Location
            LatLng latLng = LocationMocker.getLatLng();
            Location location = LocationMocker.makeLocation(latLng.latitude, latLng.longitude);
            XposedBridge.log(listener.toString() + " requestLocationUpdates: latitude: " + latLng.latitude + " longitude: " + latLng.longitude);
            Method method = findMethodBestMatch(listener.getClass(), "onLocationChanged", location);
            if (method != null) {
                XposedBridge.log("find " + listener.getClass() + " method onLocationChanged, invoke...");
                try {
                    method.invoke(listener, location);
                } catch (Exception e) {
                    XposedBridge.log("onLocationChanged invoke Exception: " + e.toString());
                }
            } else {
                XposedBridge.log("not found " + listener.getClass() + " method onLocationChanged");
            }
            //listener.onLocationChanged(location);
            //XposedHelpers.callMethod(listener, "onLocationChanged", location);
        }
    }
}
