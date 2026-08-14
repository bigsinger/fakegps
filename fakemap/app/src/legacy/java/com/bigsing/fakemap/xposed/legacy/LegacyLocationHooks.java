package com.bigsing.fakemap.xposed.legacy;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.bigsing.fakemap.core.LocationBoundary;
import com.bigsing.fakemap.core.LocationConfig;
import com.bigsing.fakemap.core.WeakIdentitySet;
import com.bigsing.fakemap.data.RemoteConfigClient;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

final class LegacyLocationHooks {
    private static final AtomicBoolean INSTALLED = new AtomicBoolean();
    private static final Set<Member> CALLBACK_METHODS = Collections.newSetFromMap(
            new ConcurrentHashMap<Member, Boolean>());
    private static final Set<Member> FUSED_CLIENT_METHODS = Collections.newSetFromMap(
            new ConcurrentHashMap<Member, Boolean>());
    private static final WeakIdentitySet<Object> FUSED_TASKS = new WeakIdentitySet<>();
    private static final WeakIdentitySet<Object> FUSED_CALLBACKS = new WeakIdentitySet<>();

    private LegacyLocationHooks() {
    }

    static void install(ClassLoader targetClassLoader) {
        if (!INSTALLED.compareAndSet(false, true)) {
            return;
        }
        hookLocationManagerResults();
        hookLocationRegistrations();
        hookLocationExtras();
        hookGoogleLocationResult(targetClassLoader);
        hookGoogleFusedOneShot(targetClassLoader);
    }

    private static void hookLocationManagerResults() {
        safelyHookAll(LocationManager.class, "getLastKnownLocation", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (param.hasThrowable()) {
                    return;
                }
                LocationConfig config = RemoteConfigClient.current();
                if (!LocationBoundary.active(config)) {
                    return;
                }
                Object result = param.getResult();
                param.setResult(result instanceof Location
                        ? LocationBoundary.replaceValue(result, config)
                        : LocationBoundary.synthetic(config));
            }
        });
    }

    private static void hookLocationRegistrations() {
        XC_MethodHook registrationHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                for (Object argument : param.args) {
                    prepareCallback(argument);
                }
            }
        };
        safelyHookAll(LocationManager.class, "requestLocationUpdates", registrationHook);
        safelyHookAll(LocationManager.class, "requestSingleUpdate", registrationHook);
        safelyHookAll(LocationManager.class, "getCurrentLocation", registrationHook);
    }

    private static void hookLocationExtras() {
        XC_MethodHook extraHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (param.hasThrowable()) {
                    return;
                }
                if (param.args.length == 0 || !LocationBoundary.isLocationExtraKey(param.args[0])) {
                    return;
                }
                LocationConfig config = RemoteConfigClient.current();
                if (LocationBoundary.active(config)) {
                    param.setResult(LocationBoundary.replaceValue(param.getResult(), config));
                }
            }
        };
        safelyHookAll(Intent.class, "getParcelableExtra", extraHook);
        safelyHookAll(Intent.class, "getParcelableArrayExtra", extraHook);
        safelyHookAll(Intent.class, "getParcelableArrayListExtra", extraHook);
        safelyHookAll(Bundle.class, "getParcelable", extraHook);
        safelyHookAll(Bundle.class, "getParcelableArray", extraHook);
        safelyHookAll(Bundle.class, "getParcelableArrayList", extraHook);
    }

    private static void hookGoogleLocationResult(ClassLoader targetClassLoader) {
        Class<?> locationResult = XposedHelpers.findClassIfExists(
                "com.google.android.gms.location.LocationResult",
                targetClassLoader);
        if (locationResult == null) {
            return;
        }
        XC_MethodHook resultHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (param.hasThrowable()) {
                    return;
                }
                LocationConfig config = RemoteConfigClient.current();
                if (LocationBoundary.active(config)) {
                    param.setResult(LocationBoundary.replaceValue(param.getResult(), config));
                }
            }
        };
        safelyHookAll(locationResult, "getLastLocation", resultHook);
        safelyHookAll(locationResult, "getLocations", resultHook);
    }

    private static void hookGoogleFusedOneShot(ClassLoader targetClassLoader) {
        Class<?> locationServices = XposedHelpers.findClassIfExists(
                "com.google.android.gms.location.LocationServices",
                targetClassLoader);
        if (locationServices == null) {
            return;
        }
        safelyHookAll(locationServices, "getFusedLocationProviderClient", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (param.hasThrowable()) {
                    return;
                }
                prepareFusedClient(param.getResult());
            }
        });
        try {
            Class<?> olderConcreteClient = XposedHelpers.findClassIfExists(
                    "com.google.android.gms.location.FusedLocationProviderClient",
                    targetClassLoader);
            if (olderConcreteClient != null
                    && !olderConcreteClient.isInterface()
                    && !Modifier.isAbstract(olderConcreteClient.getModifiers())) {
                prepareFusedClientClass(olderConcreteClient);
            }
        } catch (Throwable ignored) {
            // Old GMS allowed constructing the concrete client directly; it is optional.
        }
    }

    private static void prepareFusedClient(Object client) {
        if (client == null) {
            return;
        }
        prepareFusedClientClass(client.getClass());
    }

    private static void prepareFusedClientClass(Class<?> clientClass) {
        for (Method method : clientClass.getMethods()) {
            if (!isSupportedFusedClientMethod(method)
                    || !FUSED_CLIENT_METHODS.add(method)) {
                continue;
            }
            try {
                XposedBridge.hookMethod(method, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (param.hasThrowable()) {
                            return;
                        }
                        Object result = param.getResult();
                        prepareGoogleTask(result);
                    }
                });
            } catch (Throwable error) {
                FUSED_CLIENT_METHODS.remove(method);
            }
        }
    }

    private static void prepareGoogleTask(Object task) {
        if (task == null || !isTypeOrSupertypeNamed(
                task.getClass(), "com.google.android.gms.tasks.Task")) {
            return;
        }
        FUSED_TASKS.add(task);
        for (Method method : task.getClass().getMethods()) {
            boolean resultMethod = isTaskResultMethod(method);
            boolean successRegistration = isTaskSuccessRegistration(method);
            if (Modifier.isAbstract(method.getModifiers())
                    || (!resultMethod && !successRegistration)
                    || !CALLBACK_METHODS.add(method)) {
                continue;
            }
            try {
                if (resultMethod) {
                    XposedBridge.hookMethod(method, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            if (param.hasThrowable()) {
                                return;
                            }
                            LocationConfig config = RemoteConfigClient.current();
                            if (LocationBoundary.active(config)
                                    && FUSED_TASKS.contains(param.thisObject)) {
                                Object result = param.getResult();
                                param.setResult(result == null
                                        ? LocationBoundary.synthetic(config)
                                        : LocationBoundary.replaceValue(result, config));
                            }
                        }
                    });
                } else {
                    XposedBridge.hookMethod(method, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            if (!FUSED_TASKS.contains(param.thisObject)) {
                                return;
                            }
                            for (Object argument : param.args) {
                                prepareFusedCallback(argument);
                            }
                        }
                    });
                }
            } catch (Throwable error) {
                CALLBACK_METHODS.remove(method);
            }
        }
    }

    private static void prepareFusedCallback(Object callback) {
        if (callback != null && implementsNamedInterface(
                callback.getClass(), "com.google.android.gms.tasks.OnSuccessListener")) {
            FUSED_CALLBACKS.add(callback);
        }
        prepareCallback(callback);
    }

    private static void prepareCallback(Object callback) {
        if (callback == null) {
            return;
        }
        if (callback instanceof LocationListener) {
            hookCallbackMethods(callback.getClass(), "onLocationChanged");
            return;
        }
        if (implementsNamedInterface(callback.getClass(), "java.util.function.Consumer")) {
            hookCallbackMethods(callback.getClass(), "accept");
            return;
        }
        if (implementsNamedInterface(
                callback.getClass(), "com.google.android.gms.tasks.OnSuccessListener")) {
            hookCallbackMethods(callback.getClass(), "onSuccess");
        }
    }

    private static boolean implementsNamedInterface(Class<?> type, String interfaceName) {
        if (type == null) {
            return false;
        }
        for (Class<?> candidate : type.getInterfaces()) {
            if (interfaceName.equals(candidate.getName())
                    || implementsNamedInterface(candidate, interfaceName)) {
                return true;
            }
        }
        return implementsNamedInterface(type.getSuperclass(), interfaceName);
    }

    private static boolean isTypeOrSupertypeNamed(Class<?> type, String className) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            if (className.equals(current.getName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSupportedFusedClientMethod(Method method) {
        if (Modifier.isAbstract(method.getModifiers()) || !isTypeOrSupertypeNamed(
                method.getReturnType(), "com.google.android.gms.tasks.Task")) {
            return false;
        }
        Class<?>[] parameters = method.getParameterTypes();
        if ("getLastLocation".equals(method.getName())) {
            return parameters.length == 0
                    || parameters.length == 1
                    && "com.google.android.gms.location.LastLocationRequest".equals(
                    parameters[0].getName());
        }
        if (!"getCurrentLocation".equals(method.getName()) || parameters.length != 2
                || !"com.google.android.gms.tasks.CancellationToken".equals(
                parameters[1].getName())) {
            return false;
        }
        return parameters[0] == Integer.TYPE
                || "com.google.android.gms.location.CurrentLocationRequest".equals(
                parameters[0].getName());
    }

    private static boolean isTaskResultMethod(Method method) {
        Class<?>[] parameters = method.getParameterTypes();
        return "getResult".equals(method.getName())
                && (parameters.length == 0
                || parameters.length == 1 && parameters[0] == Class.class);
    }

    private static boolean isTaskSuccessRegistration(Method method) {
        Class<?>[] parameters = method.getParameterTypes();
        return "addOnSuccessListener".equals(method.getName())
                && (parameters.length == 1 || parameters.length == 2)
                && (parameters.length == 1
                || parameters[0].getName().equals("android.app.Activity")
                || parameters[0].getName().equals("java.util.concurrent.Executor"))
                && "com.google.android.gms.tasks.OnSuccessListener".equals(
                parameters[parameters.length - 1].getName())
                && isTypeOrSupertypeNamed(
                method.getReturnType(), "com.google.android.gms.tasks.Task");
    }

    private static boolean isSupportedCallbackMethod(Method method, String methodName) {
        if (!methodName.equals(method.getName())
                || Modifier.isAbstract(method.getModifiers())
                || method.getReturnType() != Void.TYPE
                || method.getParameterTypes().length != 1) {
            return false;
        }
        Class<?> parameter = method.getParameterTypes()[0];
        if ("onLocationChanged".equals(methodName)) {
            return parameter == Location.class || parameter == List.class;
        }
        if ("accept".equals(methodName)) {
            return parameter == Object.class || parameter == Location.class;
        }
        return "onSuccess".equals(methodName) && parameter == Object.class;
    }

    private static void hookCallbackMethods(Class<?> callbackClass, String methodName) {
        for (Method method : callbackClass.getMethods()) {
            if (!isSupportedCallbackMethod(method, methodName)
                    || !CALLBACK_METHODS.add(method)) {
                continue;
            }
            try {
                XposedBridge.hookMethod(method, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        LocationConfig config = RemoteConfigClient.current();
                        if (LocationBoundary.active(config)
                                && param.args.length == 1
                                && (!"onSuccess".equals(methodName)
                                || FUSED_CALLBACKS.contains(param.thisObject))) {
                            param.args[0] = param.args[0] == null
                                    && FUSED_CALLBACKS.contains(param.thisObject)
                                    ? LocationBoundary.synthetic(config)
                                    : LocationBoundary.replaceValue(param.args[0], config);
                        }
                    }
                });
            } catch (Throwable error) {
                CALLBACK_METHODS.remove(method);
            }
        }
    }

    private static void safelyHookAll(Class<?> type, String methodName, XC_MethodHook hook) {
        try {
            XposedBridge.hookAllMethods(type, methodName, hook);
        } catch (Throwable error) {
            XposedBridge.log("FakeMap: skipped " + type.getName() + "." + methodName
                    + " (" + error.getClass().getSimpleName() + ")");
        }
    }
}
