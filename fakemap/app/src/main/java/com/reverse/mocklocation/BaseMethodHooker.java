package com.reverse.mocklocation;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;

/**
 * Created by sing on 2016/10/13.
 */

public abstract class BaseMethodHooker extends XC_MethodHook {
    protected XSharedPreferences mPreferences;
    protected ClassLoader mClassLoader;
    protected String mParamString;

    public BaseMethodHooker(XSharedPreferences preferences, ClassLoader classLoader, String paramString) {
        this.mPreferences = preferences;
        this.mClassLoader = classLoader;
        this.mParamString = paramString;
    }

    //调用hook开启hook
    public abstract void hook();

    protected abstract void beforeCall(XC_MethodHook.MethodHookParam param);

    protected abstract void afterCall(XC_MethodHook.MethodHookParam param);

    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) {
        afterCall(param);
    }

    protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
        beforeCall(param);
    }
}
