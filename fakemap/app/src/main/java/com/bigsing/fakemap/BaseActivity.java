package com.bigsing.fakemap;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;

import com.bigsing.fakemap.utils.ActivityCollector;
import com.bigsing.fakemap.utils.Utils;

import java.util.Locale;

/**
 * Created by sing on 2017/4/19.
 */

public  class BaseActivity extends AppCompatActivity {
    protected String actName;//用于友盟页面统计


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateConfig(getApplicationContext());
        ActivityCollector.getInstance().addActivity(this);
       // actName = setActName();

    }

    protected void updateConfig(Context context) {
        String lang = MyApp.getSharedPreferences().getString("setting_lang_list", getString(R.string.lang_auto));
        if (lang.equals(getString(R.string.lang_auto))) {
            Utils.changeLocalLanguage(context, Locale.getDefault());
        } else if (lang.equals(getString(R.string.lang_en))) {
            Utils.changeLocalLanguage(context, Locale.ENGLISH);
        } else if (lang.equals(getString(R.string.lang_ch))) {
            Utils.changeLocalLanguage(context, Locale.CHINA);
        }
    }

   // public abstract String setActName();

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }

    protected void onResume() {
        super.onResume();
        // MobclickAgent.onPageStart(actName);
        // MobclickAgent.onResume(this);
    }

    protected void onPause() {
        super.onPause();
        //  MobclickAgent.onPageEnd(actName);
        // MobclickAgent.onPause(this);
    }


    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {


        ActivityCollector.getInstance().removeActivity(this);
        super.onDestroy();

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}
