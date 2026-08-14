package com.bigsing.fakemap.xposed.legacy;

import android.app.Application;
import android.content.Context;

import com.bigsing.fakemap.BuildConfig;
import com.bigsing.fakemap.core.AppConstants;
import com.bigsing.fakemap.core.TargetPackages;
import com.bigsing.fakemap.core.XposedStatus;
import com.bigsing.fakemap.data.RemoteConfigClient;

import java.util.concurrent.atomic.AtomicBoolean;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class LegacyHookEntry implements IXposedHookLoadPackage {
    private static final AtomicBoolean ATTACH_HOOKED = new AtomicBoolean();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (BuildConfig.APPLICATION_ID.equals(loadPackageParam.packageName)) {
            hookActivationProbe(loadPackageParam.classLoader);
            return;
        }
        if (!loadPackageParam.isFirstApplication
                || TargetPackages.isProtected(loadPackageParam.packageName)
                || !ATTACH_HOOKED.compareAndSet(false, true)) {
            return;
        }

        try {
            XposedHelpers.findAndHookMethod(
                    Application.class,
                    "attach",
                    Context.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            XSharedPreferences remotePreferences = new XSharedPreferences(
                                    BuildConfig.APPLICATION_ID,
                                    AppConstants.PREFERENCES_NAME);
                            RemoteConfigClient.bindAndRefresh(
                                    remotePreferences,
                                    loadPackageParam.packageName,
                                    remotePreferences::reload);
                            // Framework scope controls process injection; the local package
                            // allowlist is rechecked on every delivered location.
                            LegacyLocationHooks.install(loadPackageParam.classLoader);
                        }
                    });
        } catch (Throwable error) {
            XposedBridge.log("FakeMap: unable to register Application.attach for "
                    + loadPackageParam.packageName + ": " + error.getClass().getSimpleName());
        }
    }

    private void hookActivationProbe(ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                    XposedStatus.class.getName(),
                    classLoader,
                    "isActive",
                    XC_MethodReplacement.returnConstant(true));
        } catch (Throwable error) {
            XposedBridge.log("FakeMap: activation probe failed: "
                    + error.getClass().getSimpleName());
        }
    }
}
