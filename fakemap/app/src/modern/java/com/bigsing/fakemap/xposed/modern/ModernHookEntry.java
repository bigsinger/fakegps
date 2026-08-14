package com.bigsing.fakemap.xposed.modern;

import android.app.Application;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.bigsing.fakemap.BuildConfig;
import com.bigsing.fakemap.core.AppConstants;
import com.bigsing.fakemap.core.TargetPackages;
import com.bigsing.fakemap.core.XposedStatus;
import com.bigsing.fakemap.data.RemoteConfigClient;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedInterface;

public final class ModernHookEntry extends XposedModule {
    private final AtomicBoolean attachHooked = new AtomicBoolean();
    private final ModernLocationHooks locationHooks = new ModernLocationHooks(this);

    @Override
    @SuppressLint("DiscouragedPrivateApi")
    public void onPackageReady(PackageReadyParam param) {
        String packageName = param.getPackageName();
        if (BuildConfig.APPLICATION_ID.equals(packageName)) {
            hookActivationProbe();
            return;
        }
        if (!param.isFirstPackage()
                || TargetPackages.isProtected(packageName)
                || !attachHooked.compareAndSet(false, true)) {
            return;
        }

        try {
            Method attach = Application.class.getDeclaredMethod("attach", Context.class);
            hook(attach)
                    .setExceptionMode(ExceptionMode.PROTECTIVE)
                    .intercept(chain -> {
                        Object result = chain.proceed();
                        if ((getFrameworkProperties() & XposedInterface.PROP_CAP_REMOTE) == 0L) {
                            log(Log.WARN, "FakeMap",
                                    "Framework does not support remote preferences");
                            return result;
                        }
                        SharedPreferences remotePreferences = getRemotePreferences(
                                AppConstants.PREFERENCES_NAME);
                        RemoteConfigClient.bindAndRefresh(
                                remotePreferences,
                                packageName,
                                null);
                        locationHooks.install(param.getClassLoader());
                        return result;
                    });
        } catch (Throwable error) {
            log(Log.WARN, "FakeMap", "Unable to register Application.attach for "
                    + packageName, error);
        }
    }

    private void hookActivationProbe() {
        try {
            Method method = XposedStatus.class.getDeclaredMethod("isActive");
            hook(method)
                    .setExceptionMode(ExceptionMode.PROTECTIVE)
                    .intercept(chain -> true);
        } catch (Throwable error) {
            log(Log.WARN, "FakeMap", "Activation probe failed", error);
        }
    }
}
