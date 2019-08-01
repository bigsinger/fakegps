package com.bigsing.fakemap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.bigsing.fakemap.utils.Utils;

import java.util.Locale;


public class SettingsFragment extends PreferenceFragment {

    private void notifyChangeMapView(boolean isBaiduMap) {
        Intent intent = new Intent("switchMapView");
        if (isBaiduMap) {
            intent.putExtra("map_type", "baidu");
        } else {
            intent.putExtra("map_type", "google");
        }
        getContext().sendBroadcast(intent);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(getResources().getColor(R.color.bg_light_gray));
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Constant.TAG);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_PRIVATE);
        addPreferencesFromResource(R.xml.pref_setting);
        iniView();
    }

    private void iniView() {
        //地图设置相关
        final ListPreference mapListPreference = (ListPreference) findPreference("setting_map_list");
        mapListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue.toString().equals(getResources().getString(R.string.baidu_map))) {
                    notifyChangeMapView(true);
                    Utils.toast(getActivity().getApplicationContext(), "选择了百度地图");
                } else if (newValue.toString().equals(getResources().getString(R.string.google_map))) {
                    notifyChangeMapView(false);
                    Utils.toast(getActivity().getApplicationContext(), "选择了谷歌地图");
                }
                return true;
            }
        });
        //语言设置相关
        final Context context = getActivity().getApplicationContext();
        final ListPreference LangListPreference = (ListPreference) findPreference("setting_lang_list");
        LangListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue.toString().equals(getResources().getString(R.string.lang_auto))) {
                    MyApp.setLocale(Locale.getDefault());
                } else if (newValue.toString().equals(getResources().getString(R.string.lang_en))) {
                    MyApp.setLocale(Locale.ENGLISH);
                } else if (newValue.toString().equals(getResources().getString(R.string.lang_ch))) {
                    MyApp.setLocale(Locale.CHINA);
                }

                Activity activity = getActivity();
                activity.setResult(MyMapActivity.RESULT_CODE_RELOAD);
                activity.finish();

               // Utils.toast(getActivity().getApplicationContext(), getString(R.string.lang_setting_is_applyed));
                return true;
            }
        });
    }
}