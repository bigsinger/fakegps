package com.bigsing.fakemap;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.bigsing.fakemap.utils.Utils;

import java.util.Locale;

public class SettingActivity extends BaseActivity {
    public static final String TAG = "SettingActivity";
    private SettingsFragment mSettingsFragment;

    public String setActName() {
        return TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        if (savedInstanceState == null) {
            mSettingsFragment = new SettingsFragment();
            replaceFragment(R.id.settings_container, mSettingsFragment);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void replaceFragment(int viewId, android.app.Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(viewId, fragment).commit();
    }

    private  void notifyChangeMapView(boolean isBaiduMap) {
        Intent intent = new Intent("switchMapView");
        if (isBaiduMap) {
            intent.putExtra("map_type", "baidu");
        } else {
            intent.putExtra("map_type", "google");
        }
        sendBroadcast(intent);
    }

    /**
     * A placeholder fragment containing a settings view.
     */
    @SuppressLint("ValidFragment")
    public  class SettingsFragment extends PreferenceFragment {
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
            getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
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
                        Utils.changeLocalLanguage(context, Locale.getDefault());
                    } else if (newValue.toString().equals(getResources().getString(R.string.lang_en))) {
                        Utils.changeLocalLanguage(context, Locale.ENGLISH);
                    } else if (newValue.toString().equals(getResources().getString(R.string.lang_ch))) {
                        Utils.changeLocalLanguage(getActivity().getApplicationContext(), Locale.CHINA);
                    }
                    Utils.toast(getActivity().getApplicationContext(), getString(R.string.lang_setting_is_applyed));
                    return true;
                }
            });
        }
    }
}
