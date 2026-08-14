package com.bigsing.fakemap.core;

public final class XposedStatus {
    private XposedStatus() {
    }

    /** Legacy entry replaces this return value only inside the module app. */
    public static boolean isActive() {
        return false;
    }
}
