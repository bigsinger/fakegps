package com.bigsing.fakemap.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.LocaleList;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.bigsing.fakemap.BaiduMapFragment;
import com.bigsing.fakemap.Constant;
import com.bigsing.fakemap.GoogleMapFragment;
import com.bigsing.fakemap.MapBaiduActivity;
import com.bigsing.fakemap.MapGaodeActivity;
import com.bigsing.fakemap.MyApp;
import com.bigsing.fakemap.R;

import java.util.Locale;

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


    public static void changeLocalLanguage(Context context, Locale locale) {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            config.setLocales(localeList);
            context.createConfigurationContext(config);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            config.setLocale(locale);
            resources.updateConfiguration(config, dm);
        } else {
            config.locale = locale;
            resources.updateConfiguration(config, dm);
        }
    }

    public static void saveFakeLocation(Fragment fragment, double latitude, double longitude) {
        SharedPreferences preferences = fragment.getActivity().getSharedPreferences(Constant.TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if (fragment instanceof BaiduMapFragment) {
            //百度选点，是否需要转换？
            editor.putString("baidulatitude", latitude + "");
            editor.putString("baidulongitude", longitude + "");
        } else if (fragment instanceof GoogleMapFragment) {
            //高德选点，是否需要转换？
            editor.putString("googlelatitude", latitude + "");
            editor.putString("googlelongitude", longitude + "");
        }
        editor.putString("latitude", latitude + "");
        editor.putString("longitude", longitude + "");
        editor.commit();
        Utils.toast("地图位置已刷新~");
    }

}
