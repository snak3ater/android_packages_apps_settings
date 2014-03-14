
package com.android.settings.simpleaosp;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SeekBarPreference;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class NavigationBarSettings extends SettingsPreferenceFragment implements
OnPreferenceChangeListener {

    private static final String KEY_NAVIGATION_BAR_HEIGHT = "navigation_bar_height";
    private static final String KEY_NAVIGATION_BAR_GENERAL_CATEGORY = "navigation_bar_general_category";
    private static final String NAVIGATION_BUTTON_GLOW_TIME = "navigation_button_glow_time";


    private SeekBarPreference mNavigationBarHeight;
    private SeekBarPreference mNavigationButtonGlowTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.navigation_bar_settings);

	mNavigationButtonGlowTime = (SeekBarPreference) findPreference(NAVIGATION_BUTTON_GLOW_TIME);
        mNavigationButtonGlowTime.setProgress(Settings.System.getInt(getContentResolver(),
                  Settings.System.NAVIGATION_BUTTON_GLOW_TIME, 500));
        mNavigationButtonGlowTime.setOnPreferenceChangeListener(this);

        mNavigationBarHeight = (SeekBarPreference) findPreference(KEY_NAVIGATION_BAR_HEIGHT);
        mNavigationBarHeight.setProgress((int)(Settings.System.getFloat(getContentResolver(),
                    Settings.System.NAVIGATION_BAR_HEIGHT, 1f) * 100));
        mNavigationBarHeight.setTitle(getResources().getText(R.string.navigation_bar_height) + " " + mNavigationBarHeight.getProgress() + "%");
        mNavigationBarHeight.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
	if (preference == mNavigationButtonGlowTime) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NAVIGATION_BUTTON_GLOW_TIME, (Integer)objValue);
            return true;
       } else if (preference == mNavigationBarHeight) {
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_HEIGHT, (Integer)objValue / 100f);
            mNavigationBarHeight.setTitle(getResources().getText(R.string.navigation_bar_height) + " " + (Integer)objValue + "%");
        return true;
        }
        return false;
    }
}
