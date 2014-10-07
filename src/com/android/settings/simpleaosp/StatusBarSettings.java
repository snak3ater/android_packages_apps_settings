
package com.android.settings.simpleaosp;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import com.android.settings.chameleonos.SeekBarPreference;
import net.margaritov.preference.colorpicker.ColorPickerPreference;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class StatusBarSettings extends SettingsPreferenceFragment implements
OnPreferenceChangeListener {

    private static final String STATUS_BAR_BRIGHTNESS_CONTROL = "status_bar_brightness_control";
    // Double-tap to sleep
    private static final String DOUBLE_TAP_SLEEP_GESTURE = "double_tap_sleep_gesture";
    // Notification Count
    private static final String STATUSBAR_NOTIF_COUNT = "status_bar_notif_count";
    private static final String KEY_HOVER_NOTIFICATONS = "hover_notifications";    

    private CheckBoxPreference mStatusBarBrightnessControl;
    // Double-tap to sleep
    private CheckBoxPreference mStatusBarDoubleTapSleepGesture;
    private CheckBoxPreference mStatusBarNotifCount;
    private PreferenceScreen mhoverNotifications;

    private Preference mHeadsUp;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.status_bar_settings);
	PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mhoverNotifications = (PreferenceScreen) prefSet.findPreference(KEY_HOVER_NOTIFICATONS);

        mHeadsUp = findPreference(Settings.System.HEADS_UP_NOTIFICATION);

 	    // Notification Count
 	    mStatusBarNotifCount = (CheckBoxPreference) findPreference(STATUSBAR_NOTIF_COUNT);
            mStatusBarNotifCount.setOnPreferenceChangeListener(this);

 	    // Status bar double-tap to sleep
            mStatusBarDoubleTapSleepGesture = (CheckBoxPreference) getPreferenceScreen().findPreference(DOUBLE_TAP_SLEEP_GESTURE);
            mStatusBarDoubleTapSleepGesture.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.DOUBLE_TAP_SLEEP_GESTURE, 0) == 1));

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

        // don't show status bar brightnees control on tablet
        if (Utils.isTablet(getActivity())) {
            getPreferenceScreen().removePreference(mStatusBarBrightnessControl);
        }
    }
	
    public boolean onPreferenceChange(Preference preference, Object objValue) {
	if (preference == mStatusBarNotifCount) {
            Settings.System.putInt(getActivity().getContentResolver(), Settings.System.STATUSBAR_NOTIF_COUNT,
                    ((CheckBoxPreference)preference).isChecked() ? 0 : 1);
            return true;
        }
        return false;
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

	boolean hoverEnabled = Settings.System.getInt(
                getContentResolver(), Settings.System.HOVER_STATE, 0) == 1;

 	boolean headsUpEnabled = Settings.System.getInt(
 	getContentResolver(), Settings.System.HEADS_UP_NOTIFICATION, 0) == 1;
    }
}
