
package com.android.settings.simpleaosp;

import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class StatusBarSettings extends SettingsPreferenceFragment implements
OnPreferenceChangeListener {

    private static final String QUICK_PULLDOWN = "quick_pulldown";
    private static final String STATUS_BAR_BRIGHTNESS_CONTROL = "status_bar_brightness_control";
    // Double-tap to sleep
    private static final String DOUBLE_TAP_SLEEP_GESTURE = "double_tap_sleep_gesture";
    // Notification Count
    private static final String STATUSBAR_NOTIF_COUNT = "status_bar_notif_count";
    private static final String STATUS_BAR_TRAFFIC = "status_bar_traffic";

    private ListPreference mQuickPulldown;
    private CheckBoxPreference mStatusBarBrightnessControl;
    // Double-tap to sleep
    private CheckBoxPreference mStatusBarDoubleTapSleepGesture;
    private CheckBoxPreference mStatusBarNotifCount;
    private CheckBoxPreference mStatusBarTraffic;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.status_bar_settings);

 	    // Notification Count
 	    mStatusBarNotifCount = (CheckBoxPreference) findPreference(STATUSBAR_NOTIF_COUNT);
            mStatusBarNotifCount.setOnPreferenceChangeListener(this);

 	    // Status bar double-tap to sleep
            mStatusBarDoubleTapSleepGesture = (CheckBoxPreference) getPreferenceScreen().findPreference(DOUBLE_TAP_SLEEP_GESTURE);
            mStatusBarDoubleTapSleepGesture.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.DOUBLE_TAP_SLEEP_GESTURE, 0) == 1));

	mStatusBarTraffic = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_TRAFFIC);
        int intState = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_TRAFFIC, 0);
        intState = setStatusBarTrafficSummary(intState);
        mStatusBarTraffic.setChecked(intState > 0);
        mStatusBarTraffic.setOnPreferenceChangeListener(this);

        // Quick Settings pull down
        mQuickPulldown = (ListPreference) getPreferenceScreen().findPreference(QUICK_PULLDOWN);
        mQuickPulldown.setOnPreferenceChangeListener(this);
        int quickPulldownValue = Settings.System.getInt(getActivity().getApplicationContext()
                .getContentResolver(),
                Settings.System.QS_QUICK_PULLDOWN, 0);
        mQuickPulldown.setValue(String.valueOf(quickPulldownValue));
        mQuickPulldown.setSummary(mQuickPulldown.getEntry());

        // Status bar brightness control
        mStatusBarBrightnessControl = (CheckBoxPreference) getPreferenceScreen().findPreference(STATUS_BAR_BRIGHTNESS_CONTROL);
        mStatusBarBrightnessControl.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, 0) == 1));
        try {
            if (Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                mStatusBarBrightnessControl.setEnabled(false);
                mStatusBarBrightnessControl.setSummary(R.string.status_bar_toggle_info);
            }
        } catch (SettingNotFoundException e) {
        }

        // don't show status bar brightnees and quick settings pull down control on tablet
        if (Utils.isTablet(getActivity())) {
            getPreferenceScreen().removePreference(mStatusBarBrightnessControl);
            getPreferenceScreen().removePreference(mQuickPulldown);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
	if (preference == mStatusBarNotifCount) {
            Settings.System.putInt(getActivity().getContentResolver(), Settings.System.STATUSBAR_NOTIF_COUNT,
                    ((CheckBoxPreference)preference).isChecked() ? 0 : 1);
            return true;
        } else if (preference == mQuickPulldown) {
            int quickPulldownValue = Integer.valueOf((String) objValue);
            int quickPulldownIndex = mQuickPulldown.findIndexOfValue((String) objValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.QS_QUICK_PULLDOWN, quickPulldownValue);
            mQuickPulldown.setSummary(mQuickPulldown.getEntries()[quickPulldownIndex]);
            return true;
       } else if (preference == mStatusBarTraffic) {

            // Increment the state and then update the label
            int intState = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_TRAFFIC, 0);
            intState++;
            intState = setStatusBarTrafficSummary(intState);
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_TRAFFIC, intState);
            if (intState > 1) {return false;}        
        } else {
            return false;
        }
        return true;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mStatusBarBrightnessControl) {
            value = mStatusBarBrightnessControl.isChecked();
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, value ? 1 : 0);
            return true;
        } else if (preference == mStatusBarDoubleTapSleepGesture) {
            value = mStatusBarDoubleTapSleepGesture.isChecked();
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.DOUBLE_TAP_SLEEP_GESTURE, value ? 1: 0);
            return true;
	}
   	 return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

	private int setStatusBarTrafficSummary(int intState) {
        // These states must match com.android.systemui.statusbar.policy.Traffic
        if (intState == 1) {
            mStatusBarTraffic.setSummary(R.string.show_network_speed_bits);
        } else if (intState == 2) {
            mStatusBarTraffic.setSummary(R.string.show_network_speed_bytes);
        } else {
            mStatusBarTraffic.setSummary(R.string.show_network_speed_summary);
            return 0;
        }
        return intState;
    }
}
