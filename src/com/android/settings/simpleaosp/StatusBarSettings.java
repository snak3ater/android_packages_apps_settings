
package com.android.settings.simpleaosp;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.net.TrafficStats;
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
    private static final String NETWORK_TRAFFIC_STATE = "network_traffic_state";
    private static final String NETWORK_TRAFFIC_UNIT = "network_traffic_unit";
    private static final String NETWORK_TRAFFIC_PERIOD = "network_traffic_period";
    private static final String KEY_HOVER_NOTIFICATONS = "hover_notifications";
    private static final String STATUS_BAR_NETWORK_ACTIVITY = "status_bar_network_activity";
    private static final String NETWORK_TRAFFIC_AUTOHIDE = "network_traffic_autohide";
    private static final String NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD = "network_traffic_autohide_threshold";

    private CheckBoxPreference mStatusBarBrightnessControl;
    // Double-tap to sleep
    private CheckBoxPreference mStatusBarDoubleTapSleepGesture;
    private CheckBoxPreference mStatusBarNotifCount;
    private ListPreference mNetTrafficState;
    private ListPreference mNetTrafficUnit;
    private ListPreference mNetTrafficPeriod;
    private PreferenceScreen mhoverNotifications;
    private CheckBoxPreference mStatusBarNetworkActivity;
    private CheckBoxPreference mNetTrafficAutohide;
    private SeekBarPreference mNetTrafficAutohideThreshold;

    private int mNetTrafficVal;
    private int MASK_UP;
    private int MASK_DOWN;
    private int MASK_UNIT;
    private int MASK_PERIOD;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	loadResources();

        addPreferencesFromResource(R.xml.status_bar_settings);
	PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mhoverNotifications = (PreferenceScreen) prefSet.findPreference(KEY_HOVER_NOTIFICATONS);

 	    // Notification Count
 	    mStatusBarNotifCount = (CheckBoxPreference) findPreference(STATUSBAR_NOTIF_COUNT);
            mStatusBarNotifCount.setOnPreferenceChangeListener(this);

 	    // Status bar double-tap to sleep
            mStatusBarDoubleTapSleepGesture = (CheckBoxPreference) getPreferenceScreen().findPreference(DOUBLE_TAP_SLEEP_GESTURE);
            mStatusBarDoubleTapSleepGesture.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.DOUBLE_TAP_SLEEP_GESTURE, 0) == 1));

	mNetTrafficState = (ListPreference) prefSet.findPreference(NETWORK_TRAFFIC_STATE);
        mNetTrafficUnit = (ListPreference) prefSet.findPreference(NETWORK_TRAFFIC_UNIT);
        mNetTrafficPeriod = (ListPreference) prefSet.findPreference(NETWORK_TRAFFIC_PERIOD);

	mNetTrafficAutohide =
	(CheckBoxPreference) prefSet.findPreference(NETWORK_TRAFFIC_AUTOHIDE);
	mNetTrafficAutohide.setChecked((Settings.System.getInt(getContentResolver(),
	Settings.System.NETWORK_TRAFFIC_AUTOHIDE, 0) == 1));
	mNetTrafficAutohide.setOnPreferenceChangeListener(this);
	mNetTrafficAutohideThreshold = (SeekBarPreference) prefSet.findPreference(NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD);
	int netTrafficAutohideThreshold = Settings.System.getInt(resolver,
	Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, 10);
	mNetTrafficAutohideThreshold.setValue(netTrafficAutohideThreshold / 1);
	mNetTrafficAutohideThreshold.setOnPreferenceChangeListener(this);

        // TrafficStats will return UNSUPPORTED if the device does not support it.
        if (TrafficStats.getTotalTxBytes() != TrafficStats.UNSUPPORTED &&
                TrafficStats.getTotalRxBytes() != TrafficStats.UNSUPPORTED) {
            mNetTrafficVal = Settings.System.getInt(getActivity().getContentResolver(), Settings.System.NETWORK_TRAFFIC_STATE, 0);
            int intIndex = mNetTrafficVal & (MASK_UP + MASK_DOWN);
            intIndex = mNetTrafficState.findIndexOfValue(String.valueOf(intIndex));
            if (intIndex <= 0) {
                mNetTrafficUnit.setEnabled(false);
                mNetTrafficPeriod.setEnabled(false);
                mNetTrafficAutohide.setEnabled(false);
                mNetTrafficAutohideThreshold.setEnabled(false);
            }
            mNetTrafficState.setValueIndex(intIndex >= 0 ? intIndex : 0);
            mNetTrafficState.setSummary(mNetTrafficState.getEntry());
            mNetTrafficState.setOnPreferenceChangeListener(this);

            mNetTrafficUnit.setValueIndex(getBit(mNetTrafficVal, MASK_UNIT) ? 1 : 0);
            mNetTrafficUnit.setSummary(mNetTrafficUnit.getEntry());
            mNetTrafficUnit.setOnPreferenceChangeListener(this);

            intIndex = (mNetTrafficVal & MASK_PERIOD) >>> 16;
            intIndex = mNetTrafficPeriod.findIndexOfValue(String.valueOf(intIndex));
            mNetTrafficPeriod.setValueIndex(intIndex >= 0 ? intIndex : 1);
            mNetTrafficPeriod.setSummary(mNetTrafficPeriod.getEntry());
            mNetTrafficPeriod.setOnPreferenceChangeListener(this);
        } else {
            prefSet.removePreference(findPreference(NETWORK_TRAFFIC_STATE));
            prefSet.removePreference(findPreference(NETWORK_TRAFFIC_UNIT));
            prefSet.removePreference(findPreference(NETWORK_TRAFFIC_PERIOD));
	    prefSet.removePreference(findPreference(NETWORK_TRAFFIC_AUTOHIDE));
	    prefSet.removePreference(findPreference(NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD));
        }

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

        mStatusBarNetworkActivity = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_NETWORK_ACTIVITY);
        mStatusBarNetworkActivity.setChecked(Settings.System.getInt(resolver,
            Settings.System.STATUS_BAR_NETWORK_ACTIVITY, 0) == 1);
         mStatusBarNetworkActivity.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
	if (preference == mStatusBarNotifCount) {
            Settings.System.putInt(getActivity().getContentResolver(), Settings.System.STATUSBAR_NOTIF_COUNT,
                    ((CheckBoxPreference)preference).isChecked() ? 0 : 1);
            return true;
       } else if (preference == mNetTrafficState) {
            int intState = Integer.valueOf((String)objValue);
            mNetTrafficVal = setBit(mNetTrafficVal, MASK_UP, getBit(intState, MASK_UP));
            mNetTrafficVal = setBit(mNetTrafficVal, MASK_DOWN, getBit(intState, MASK_DOWN));
            Settings.System.putInt(getActivity().getContentResolver(), Settings.System.NETWORK_TRAFFIC_STATE, mNetTrafficVal);
            int index = mNetTrafficState.findIndexOfValue((String) objValue);
            mNetTrafficState.setSummary(mNetTrafficState.getEntries()[index]);
            if (intState == 0) {
                mNetTrafficUnit.setEnabled(false);
                mNetTrafficPeriod.setEnabled(false);
		mNetTrafficAutohide.setEnabled(false);
		mNetTrafficAutohideThreshold.setEnabled(false);
            } else {
                mNetTrafficUnit.setEnabled(true);
                mNetTrafficPeriod.setEnabled(true);
		mNetTrafficAutohide.setEnabled(true);
		mNetTrafficAutohideThreshold.setEnabled(true);
            }
        } else if (preference == mNetTrafficUnit) {
            // 1 = Display as Byte/s; default is bit/s
            mNetTrafficVal = setBit(mNetTrafficVal, MASK_UNIT, ((String)objValue).equals("1"));
            Settings.System.putInt(getActivity().getContentResolver(), Settings.System.NETWORK_TRAFFIC_STATE, mNetTrafficVal);
            int index = mNetTrafficUnit.findIndexOfValue((String) objValue);
            mNetTrafficUnit.setSummary(mNetTrafficUnit.getEntries()[index]);
        } else if (preference == mNetTrafficPeriod) {
            int intState = Integer.valueOf((String)objValue);
            mNetTrafficVal = setBit(mNetTrafficVal, MASK_PERIOD, false) + (intState << 16);
            Settings.System.putInt(getActivity().getContentResolver(), Settings.System.NETWORK_TRAFFIC_STATE, mNetTrafficVal);
            int index = mNetTrafficPeriod.findIndexOfValue((String) objValue);
            mNetTrafficPeriod.setSummary(mNetTrafficPeriod.getEntries()[index]);
	} else if (preference == mNetTrafficAutohide) {
	boolean value = (Boolean) objValue;
	Settings.System.putInt(getActivity().getContentResolver(), Settings.System.NETWORK_TRAFFIC_AUTOHIDE,
	value ? 1 : 0);
	} else if (preference == mNetTrafficAutohideThreshold) {
	int netTrafficAutohideThreshold = (Integer) objValue;
	Settings.System.putInt(getContentResolver(),
	Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, netTrafficAutohideThreshold * 1);
        } else if (preference == mStatusBarNetworkActivity) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(), Settings.System.STATUS_BAR_NETWORK_ACTIVITY, value ? 1 : 0);
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

	boolean hoverEnabled = Settings.System.getInt(
                getContentResolver(), Settings.System.HOVER_STATE, 0) == 1;
        mhoverNotifications.setSummary(hoverEnabled
                ? R.string.summary_hover_notifications_enabled : R.string.summary_hover_notifications_disabled);
    }

	 private void loadResources() {
        Resources resources = getActivity().getResources();
        MASK_UP = resources.getInteger(R.integer.maskUp);
        MASK_DOWN = resources.getInteger(R.integer.maskDown);
        MASK_UNIT = resources.getInteger(R.integer.maskUnit);
        MASK_PERIOD = resources.getInteger(R.integer.maskPeriod);
    }

    // intMask should only have the desired bit(s) set
    private int setBit(int intNumber, int intMask, boolean blnState) {
        if (blnState) {
            return (intNumber | intMask);
        }
        return (intNumber & ~intMask);
    }

    private boolean getBit(int intNumber, int intMask) {
        return (intNumber & intMask) == intMask;
    }
}
