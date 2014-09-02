/*
 * Copyright (C) 2013 SlimRoms Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.simpleaosp;

import android.content.ContentResolver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.SeekBarPreference;
import android.preference.SwitchPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class LockscreenSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private static final String TAG = "LockscreenSettings";

    
    private static final String KEY_SEE_THROUGH = "see_through";
    private static final String KEY_LOCKSCREEN_GENERAL_CATEGORY = "lockscreen_general_category";
    private static final String KEY_PEEK_NOTIFICATONS = "peek_notifications";
    // Omni Additions
    private static final String BATTERY_AROUND_LOCKSCREEN_RING = "battery_around_lockscreen_ring";

    private PreferenceScreen mPeekNotifications;
    private PreferenceScreen mseethrough;
    
    // Omni Additions
    private SystemSettingCheckBoxPreference mLockRingBattery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_settings);
        PreferenceScreen prefSet = getPreferenceScreen();

	mPeekNotifications = (PreferenceScreen) prefSet.findPreference(KEY_PEEK_NOTIFICATONS);
	mseethrough = (PreferenceScreen) prefSet.findPreference(KEY_SEE_THROUGH);

	// Add the additional Omni settings
        mLockRingBattery = (SystemSettingCheckBoxPreference) getPreferenceScreen()
                .findPreference(BATTERY_AROUND_LOCKSCREEN_RING);
        if (mLockRingBattery != null) {
            mLockRingBattery.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.BATTERY_AROUND_LOCKSCREEN_RING, 0) == 1);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
	boolean value;
	if (preference == mLockRingBattery) {
	value = mLockRingBattery.isChecked();
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.BATTERY_AROUND_LOCKSCREEN_RING, value ? 1 : 0);
	return true;
	}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onResume() {
        super.onResume();

	boolean peekEnabled = Settings.System.getInt(
	getContentResolver(), Settings.System.PEEK_STATE, 0) == 1;
	mPeekNotifications.setSummary(peekEnabled
	? R.string.summary_peek_notifications_enabled : R.string.summary_peek_notifications_disabled);

	boolean blurEnabled = Settings.System.getInt(
 	getContentResolver(), Settings.System.LOCKSCREEN_SEE_THROUGH, 0) == 1;
	mseethrough.setSummary(blurEnabled
	? R.string.summary_blur_notifications_enabled : R.string.summary_blur_notifications_disabled);
    }
}
