package io.github.libxposed.service;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;

/** Receives the modern framework binder only from root or system UID. */
public final class SecureXposedProvider extends ContentProvider {
    private static final String METHOD_SEND_BINDER = "SendBinder";
    private static final String EXTRA_BINDER = "binder";
    private static final String SERVICE_DESCRIPTOR =
            "io.github.libxposed.service.IXposedService";

    @Override
    public boolean onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                && getContext() != null
                && getContext().getApplicationInfo().targetSdkVersion
                >= Build.VERSION_CODES.R) {
            RemotePreferences.shouldNotifyCleared = true;
        }
        return true;
    }

    @Override
    public Bundle call(String method, String argument, Bundle extras) {
        if (!METHOD_SEND_BINDER.equals(method)
                || extras == null
                || !isTrustedCaller(Binder.getCallingUid())) {
            return null;
        }
        IBinder binder = extras.getBinder(EXTRA_BINDER);
        if (binder == null || !hasExpectedDescriptor(binder)) {
            return null;
        }
        XposedServiceHelper.onBinderReceived(binder);
        return new Bundle();
    }

    private static boolean isTrustedCaller(int uid) {
        return uid == 0 || uid == Process.SYSTEM_UID;
    }

    private static boolean hasExpectedDescriptor(IBinder binder) {
        try {
            return SERVICE_DESCRIPTOR.equals(binder.getInterfaceDescriptor());
        } catch (RemoteException error) {
            return false;
        }
    }

    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(
            Uri uri,
            ContentValues values,
            String selection,
            String[] selectionArgs) {
        return 0;
    }
}
