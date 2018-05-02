package com.bigsing.fakemap;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.tencent.bugly.Bugly;

/**
 * Created by sing on 2017/4/19.
 */

public class MyApp extends Application {
    public static Context mContext;
    //程序通用的配置文件
    private static SharedPreferences mSP;

    public static SharedPreferences getSharedPreferences() {
        return mSP;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mSP = getSharedPreferences(Constant.TAG, MODE_PRIVATE);
        Bugly.init(mContext, "c733286b0d", false);
    }
}
