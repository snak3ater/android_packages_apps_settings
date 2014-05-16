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

package com.android.settings.rascarlo;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.Handler;
import android.app.admin.DevicePolicyManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import com.android.settings.rascarlo.lsn.LockscreenNotificationsPreference;
import android.preference.SwitchPreference;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class LockscreenSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private static final String TAG = "LockscreenSettings";
    private static final String KEY_SEE_THROUGH = "see_through";
    private static final String KEY_NOTIFICATON_PEEK = "notification_peek";
    private static final String KEY_PEEK_PICKUP_TIMEOUT = "peek_pickup_timeout";
    private static final String KEY_PEEK_WAKE_TIMEOUT = "peek_wake_timeout";
    private static final String KEY_LOCKSCREEN_NOTIFICATONS = "lockscreen_notifications";

    private CheckBoxPreference mSeeThrough;
    private ListPreference mPeekPickupTimeout;
    private ListPreference mPeekWakeTimeout;
    private LockscreenNotificationsPreference mLockscreenNotifications;
    private SwitchPreference mNotificationPeek;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_settings);
        PreferenceScreen prefSet = getPreferenceScreen();

        // lockscreen see through
        mSeeThrough = (CheckBoxPreference) prefSet.findPreference(KEY_SEE_THROUGH);
        if (mSeeThrough != null) {
            mSeeThrough.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_SEE_THROUGH, 0) == 1);
        }
        mLockscreenNotifications = (LockscreenNotificationsPreference) prefSet.findPreference(KEY_LOCKSCREEN_NOTIFICATONS);
        mNotificationPeek = (SwitchPreference) prefSet.findPreference(KEY_NOTIFICATON_PEEK);
        mNotificationPeek.setChecked(Settings.System.getInt(getContentResolver(), Settings.System.PEEK_STATE, 0) == 1);
        mNotificationPeek.setOnPreferenceChangeListener(this);
        updateVisiblePreferences();

        mPeekPickupTimeout = (ListPreference) prefSet.findPreference(KEY_PEEK_PICKUP_TIMEOUT);
	int peekPickupTimeout = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.PEEK_PICKUP_TIMEOUT, 10000, UserHandle.USER_CURRENT);
	mPeekPickupTimeout.setValue(String.valueOf(peekPickupTimeout));
        mPeekPickupTimeout.setSummary(mPeekPickupTimeout.getEntry());
        mPeekPickupTimeout.setOnPreferenceChangeListener(this);

	mPeekWakeTimeout = (ListPreference) prefSet.findPreference(KEY_PEEK_WAKE_TIMEOUT);
        int peekWakeTimeout = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.PEEK_WAKE_TIMEOUT, 5000, UserHandle.USER_CURRENT);
        mPeekWakeTimeout.setValue(String.valueOf(peekWakeTimeout));
        mPeekWakeTimeout.setSummary(mPeekWakeTimeout.getEntry());
        mPeekWakeTimeout.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object objValue) {
	ContentResolver cr = getActivity().getContentResolver();
	if (pref == mNotificationPeek) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(cr, Settings.System.PEEK_STATE,
                    value ? 1 : 0);
        updateVisiblePreferences();
            return true;
	} else if (pref == mPeekPickupTimeout) {
 	    int index = mPeekPickupTimeout.findIndexOfValue((String) objValue);
            int peekPickupTimeout = Integer.valueOf((String) objValue);
            Settings.System.putIntForUser(getContentResolver(),
                Settings.System.PEEK_PICKUP_TIMEOUT,
                   peekPickupTimeout, UserHandle.USER_CURRENT);
		    mPeekPickupTimeout.setSummary(mPeekPickupTimeout.getEntries()[index]);
            return true;
	} else if (pref == mPeekWakeTimeout) {
            int index = mPeekWakeTimeout.findIndexOfValue((String) objValue);
            int peekWakeTimeout = Integer.valueOf((String) objValue);
            Settings.System.putIntForUser(getContentResolver(),
                Settings.System.PEEK_WAKE_TIMEOUT,
                    peekWakeTimeout, UserHandle.USER_CURRENT);
            mPeekWakeTimeout.setSummary(mPeekWakeTimeout.getEntries()[index]);
            return true;
        }
    	    return false;
}

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
	boolean value;
	if (preference == mSeeThrough) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_SEE_THROUGH,
                    mSeeThrough.isChecked() ? 1 : 0);
	return true;
	}else if (preference == mLockscreenNotifications) {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }        
		return super.onPreferenceTreeClick(preferenceScreen, preference);

    }

	private void updateVisiblePreferences() {
        int peek = Settings.System.getInt(getContentResolver(),
                Settings.System.PEEK_STATE, 0);
        int lsn = Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_NOTIFICATIONS, 0);

        if (peek == 1) {
            Settings.System.putInt(getContentResolver(),
                Settings.System.LOCKSCREEN_NOTIFICATIONS, 0);
            mLockscreenNotifications.setEnabled(false);
        } else if (lsn == 1) {
            Settings.System.putInt(getContentResolver(),
                Settings.System.PEEK_STATE, 0);
        } else {
            mLockscreenNotifications.setEnabled(true);
            mNotificationPeek.setEnabled(true);
        }
    }



    @Override
    public void onResume() {
        super.onResume();
    }

}
