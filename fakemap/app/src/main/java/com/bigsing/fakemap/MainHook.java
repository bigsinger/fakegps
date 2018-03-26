package com.bigsing.fakemap;

import com.reverse.mocklocation.LocationMocker;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by sing on 2017/4/18.
 */

public class MainHook implements IXposedHookLoadPackage {
    private XSharedPreferences mPreferences;

    private static boolean IsPackageIgnored(String packageName) {
        return (packageName.equals("android")) || (packageName.equals("com.android.providers.settings")) || (packageName.equals("com.android.server.telecom")) || (packageName.equals("com.android.location.fused")) || (packageName.equals("com.qualcomm.location"));
    }

    private static boolean IsVendorPackage(String packageName) {
        return (packageName.startsWith("com.sonymobile")) || (packageName.startsWith("com.sonyericsson"));
    }

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam param) {
        XposedBridge.log("[handleLoadPackage] " + param.packageName);

        if (IsPackageIgnored(param.packageName) == true || IsVendorPackage(param.packageName) == true) {
            return;
        }

        if (param.packageName.equals(Constant.PACKAGE_THIS)) {
            //用来判断是否激活的
            XC_MethodHook.Unhook unhook = findAndHookMethod(XposedActive.class.getName(), param.classLoader, "isActive", XC_MethodReplacement.returnConstant(true));
            if (unhook != null) {
                XposedBridge.log("isActive HOOK OK!!!");
            } else {
                XposedBridge.log("isActive HOOK FAILED error here: class [XposedActive.isActive] maybe optimized!!! shoul keep it in proguard-rules.pro");
            }
            return;
        }

        mPreferences = new XSharedPreferences(Constant.PACKAGE_THIS, Constant.TAG);

        LocationMocker locationMocker = LocationMocker.getInstance();
        locationMocker.init(mPreferences, param.classLoader);
        locationMocker.hook(param.classLoader);
    }

    public void initZygote(IXposedHookZygoteInit.StartupParam param) throws Throwable {
        //PhoneInfoUtils.getPhoneInfo();
    }
}