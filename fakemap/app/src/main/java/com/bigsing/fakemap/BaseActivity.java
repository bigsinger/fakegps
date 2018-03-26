package com.bigsing.fakemap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.bigsing.fakemap.utils.ActivityCollector;

/**
 * Created by sing on 2017/4/19.
 */

public abstract class BaseActivity extends AppCompatActivity {
    protected String actName;//用于友盟页面统计

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCollector.getInstance().addActivity(this);
        actName = setActName();


    }

    public abstract String setActName();

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
