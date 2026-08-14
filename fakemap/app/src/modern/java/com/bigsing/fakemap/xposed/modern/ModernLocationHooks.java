package com.bigsing.fakemap.xposed.modern;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.bigsing.fakemap.core.LocationBoundary;
import com.bigsing.fakemap.core.LocationConfig;
import com.bigsing.fakemap.core.WeakIdentitySet;
import com.bigsing.fakemap.data.RemoteConfigClient;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.libxposed.api.XposedInterface;

final class ModernLocationHooks {
    private final ModernHookEntry module;
    private final AtomicBoolean installed = new AtomicBoolean();
    private final Set<Executable> callbackMethods = Collections.newSetFromMap(
            new ConcurrentHashMap<Executable, Boolean>());
    private final Set<Executable> fusedClientMethods = Collections.newSetFromMap(
            new ConcurrentHashMap<Executable, Boolean>());
    private final WeakIdentitySet<Object> fusedTasks = new WeakIdentitySet<>();
    private final WeakIdentitySet<Object> fusedCallbacks = new WeakIdentitySet<>();

    ModernLocationHooks(ModernHookEntry module) {
        this.module = module;
    }

    void install(ClassLoader targetClassLoader) {
        if (!installed.compareAndSet(false, true)) {
            return;
        }
        hookLocationManagerResults();
        hookLocationRegistrations();
        hookLocationExtras();
        hookGoogleLocationResult(targetClassLoader);
        hookGoogleFusedOneShot(targetClassLoader);
    }

    private void hookLocationManagerResults() {
        hookAll(LocationManager.class, "getLastKnownLocation", chain -> {
            Object result = chain.proceed();
            LocationConfig config = RemoteConfigClient.current();
            if (!LocationBoundary.active(config)) {
                return result;
            }
            return result instanceof Location
                    ? LocationBoundary.replaceValue(result, config)
                    : LocationBoundary.synthetic(config);
        });
    }

    private void hookLocationRegistrations() {
        XposedInterface.Hooker registration = chain -> {
            for (Object argument : chain.getArgs()) {
                prepareCallback(argument);
            }
            return chain.proceed();
        };
        hookAll(LocationManager.class, "requestLocationUpdates", registration);
        hookAll(LocationManager.class, "requestSingleUpdate", registration);
        hookAll(LocationManager.class, "getCurrentLocation", registration);
    }

    private void hookLocationExtras() {
        XposedInterface.Hooker extras = chain -> {
            Object result = chain.proceed();
            if (chain.getArgs().isEmpty()
                    || !LocationBoundary.isLocationExtraKey(chain.getArg(0))) {
                return result;
            }
            LocationConfig config = RemoteConfigClient.current();
            return LocationBoundary.active(config)
                    ? LocationBoundary.replaceValue(result, config)
                    : result;
        };
        hookAll(Intent.class, "getParcelableExtra", extras);
        hookAll(Intent.class, "getParcelableArrayExtra", extras);
        hookAll(Intent.class, "getParcelableArrayListExtra", extras);
        hookAll(Bundle.class, "getParcelable", extras);
        hookAll(Bundle.class, "getParcelableArray", extras);
        hookAll(Bundle.class, "getParcelableArrayList", extras);
    }

    private void hookGoogleLocationResult(ClassLoader targetClassLoader) {
        try {
            Class<?> locationResult = Class.forName(
                    "com.google.android.gms.location.LocationResult",
                    false,
                    targetClassLoader);
            XposedInterface.Hooker resultHook = chain -> {
                Object result = chain.proceed();
                LocationConfig config = RemoteConfigClient.current();
                return LocationBoundary.active(config)
                        ? LocationBoundary.replaceValue(result, config)
                        : result;
            };
            hookAll(locationResult, "getLastLocation", resultHook);
            hookAll(locationResult, "getLocations", resultHook);
        } catch (ClassNotFoundException ignored) {
            // GMS is optional and commonly absent on devices sold in mainland China.
        }
    }

    private void hookGoogleFusedOneShot(ClassLoader targetClassLoader) {
        try {
            Class<?> locationServices = Class.forName(
                    "com.google.android.gms.location.LocationServices",
                    false,
                    targetClassLoader);
            hookAll(locationServices, "getFusedLocationProviderClient", chain -> {
                Object result = chain.proceed();
                prepareFusedClient(result);
                return result;
            });
            try {
                Class<?> olderConcreteClient = Class.forName(
                        "com.google.android.gms.location.FusedLocationProviderClient",
                        false,
                        targetClassLoader);
                if (!olderConcreteClient.isInterface()
                        && !Modifier.isAbstract(olderConcreteClient.getModifiers())) {
                    prepareFusedClientClass(olderConcreteClient);
                }
            } catch (Throwable ignored) {
                // Old GMS allowed constructing the concrete client directly; it is optional.
            }
        } catch (ClassNotFoundException ignored) {
        }
    }

    private void prepareFusedClient(Object client) {
        if (client == null) {
            return;
        }
        prepareFusedClientClass(client.getClass());
    }

    private void prepareFusedClientClass(Class<?> clientClass) {
        for (Method method : clientClass.getMethods()) {
            if (!isSupportedFusedClientMethod(method)
                    || !fusedClientMethods.add(method)) {
                continue;
            }
            try {
                module.hook(method)
                        .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                        .intercept(chain -> {
                            Object result = chain.proceed();
                            prepareGoogleTask(result);
                            return result;
                        });
            } catch (Throwable error) {
                fusedClientMethods.remove(method);
            }
        }
    }

    private void prepareGoogleTask(Object task) {
        if (task == null || !isTypeOrSupertypeNamed(
                task.getClass(), "com.google.android.gms.tasks.Task")) {
            return;
        }
        fusedTasks.add(task);
        for (Method method : task.getClass().getMethods()) {
            boolean resultMethod = isTaskResultMethod(method);
            boolean successRegistration = isTaskSuccessRegistration(method);
            if (Modifier.isAbstract(method.getModifiers())
                    || (!resultMethod && !successRegistration)
                    || !callbackMethods.add(method)) {
                continue;
            }
            try {
                if (resultMethod) {
                    module.hook(method)
                            .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                            .intercept(chain -> {
                                Object result = chain.proceed();
                                LocationConfig config = RemoteConfigClient.current();
                                if (!LocationBoundary.active(config)
                                        || !fusedTasks.contains(chain.getThisObject())) {
                                    return result;
                                }
                                return result == null
                                        ? LocationBoundary.synthetic(config)
                                        : LocationBoundary.replaceValue(result, config);
                            });
                } else {
                    module.hook(method)
                            .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                            .intercept(chain -> {
                                if (!fusedTasks.contains(chain.getThisObject())) {
                                    return chain.proceed();
                                }
                                for (Object argument : chain.getArgs()) {
                                    prepareFusedCallback(argument);
                                }
                                return chain.proceed();
                            });
                }
            } catch (Throwable error) {
                callbackMethods.remove(method);
            }
        }
    }

    private void prepareFusedCallback(Object callback) {
        if (callback != null && implementsNamedInterface(
                callback.getClass(), "com.google.android.gms.tasks.OnSuccessListener")) {
            fusedCallbacks.add(callback);
        }
        prepareCallback(callback);
    }

    private void prepareCallback(Object callback) {
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

    private void hookCallbackMethods(Class<?> callbackClass, String methodName) {
        for (Method method : callbackClass.getMethods()) {
            if (!isSupportedCallbackMethod(method, methodName)
                    || !callbackMethods.add(method)) {
                continue;
            }
            try {
                module.hook(method)
                        .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                        .intercept(chain -> {
                            LocationConfig config = RemoteConfigClient.current();
                            if (!LocationBoundary.active(config)
                                    || chain.getArgs().size() != 1
                                    || ("onSuccess".equals(methodName)
                                    && !fusedCallbacks.contains(chain.getThisObject()))) {
                                return chain.proceed();
                            }
                            Object[] args = chain.getArgs().toArray(new Object[0]);
                            args[0] = args[0] == null
                                    && fusedCallbacks.contains(chain.getThisObject())
                                    ? LocationBoundary.synthetic(config)
                                    : LocationBoundary.replaceValue(args[0], config);
                            return chain.proceed(args);
                        });
            } catch (Throwable error) {
                callbackMethods.remove(method);
            }
        }
    }

    private void hookAll(Class<?> type, String methodName, XposedInterface.Hooker hooker) {
        boolean found = false;
        for (Method method : type.getDeclaredMethods()) {
            if (!methodName.equals(method.getName()) || Modifier.isAbstract(method.getModifiers())) {
                continue;
            }
            found = true;
            try {
                module.hook(method)
                        .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                        .intercept(hooker);
            } catch (Throwable error) {
                module.log(Log.WARN, "FakeMap", "Skipped " + type.getName() + "."
                        + methodName, error);
            }
        }
        if (!found) {
            module.log(Log.DEBUG, "FakeMap", "Method unavailable: "
                    + type.getName() + "." + methodName);
        }
    }
}
