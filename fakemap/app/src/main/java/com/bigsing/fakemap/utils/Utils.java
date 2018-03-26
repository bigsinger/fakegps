package com.bigsing.fakemap.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.bigsing.fakemap.Constant;
import com.bigsing.fakemap.MyApp;

/**
 * Created by sing on 2017/4/19.
 */

public class Utils {
    private static boolean debug = true;

    public static void log(String text) {
        if (debug) {
            System.out.println(text);
        }
    }

    public static void toast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void toast(String msg) {
        Toast.makeText(MyApp.mContext, msg, Toast.LENGTH_SHORT).show();
    }

    public static void logd(String msg) {
        Log.d(Constant.TAG, msg);
    }

    public static void loge(String msg) {
        Log.e(Constant.TAG, msg);
    }

    public static String getVersionInfo(Context context) {
        String versionName = null;

        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            //versioncode = pi.versionCode;

        } catch (Exception e) {
        }

        if (versionName == null || versionName.length() <= 0) {
            return "";
        } else {
            return " v" + versionName;
        }
    }

    public static void openUrl(Context context, String url) {
        try {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        } catch (Exception e) {
        }
    }
}
