package com.bigsing.fakemap;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.tencent.bugly.Bugly;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;


/**
 * Created by sing on 2017/4/19.
 */

public class MyApp extends Application {
    public static Context mContext;
    //程序通用的配置文件
    private static SharedPreferences mSP;
    private static Locale mLocale = Locale.CHINA;

    public static SharedPreferences getSharedPreferences() {
        return mSP;
    }

    public static Locale getLocale() {
        return mLocale;
    }

    public static void setLocale(Locale locale) {
        try {
            File langFile = new File(mContext.getCacheDir(), ".lang");
            FileOutputStream writer = new FileOutputStream(langFile);
            writer.write(locale.toLanguageTag().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mLocale = locale;
    }

    void setLocale() {
        File langFile = new File(mContext.getCacheDir(), ".lang");
        int length = (int) langFile.length();
        byte[] bytes = new byte[length];
        try {
            FileInputStream in = new FileInputStream(langFile);
            in.read(bytes);
            String contents = new String(bytes);
            switch (contents) {
                case "zh-CN":
                    mLocale = Locale.CHINA;
                    break;
                case "en":
                    mLocale = Locale.ENGLISH;
                    break;
                default:
                    mLocale = Locale.getDefault();
                    break;
            }
        } catch (FileNotFoundException e) {
            mLocale = Locale.getDefault();
            e.printStackTrace();
        } catch (IOException e) {
            mLocale = Locale.getDefault();
            e.printStackTrace();
        } finally {

        }


    }


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        setLocale();
        //7.0以及上，必须设置为MODE_PRIVATE，否则会出现奔溃，但此时XSharedPreferences读取不到内容
        if (Build.VERSION.SDK_INT >= 24) {
            mSP = mContext.getSharedPreferences(Constant.TAG, MODE_PRIVATE);
        } else {
            mSP = mContext.getSharedPreferences(Constant.TAG, MODE_WORLD_READABLE);
        }

        //  Bugly.init(mContext, "c733286b0d", false);
    }
}
