package com.bigsing.fakemap;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
    ////////////////////

    /**
     * A placeholder fragment containing a settings view.
     */
    public static class SettingsFragment extends PreferenceFragment {
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

            final Preference mapselector = findPreference("gaodemap");    //地图选择器指示选择哪个地图来选点
            mapselector.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    return false;
                }
            });

            iniView();
        }

        private void iniView() {
            final Context context = getActivity().getApplicationContext();
            final ListPreference listPreference = (ListPreference) findPreference("setting_lang_list");
            listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.toString().equals(getResources().getString(R.string.lang_auto))) {
                        Utils.changeLocalLanguage(context,Locale.getDefault());
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
