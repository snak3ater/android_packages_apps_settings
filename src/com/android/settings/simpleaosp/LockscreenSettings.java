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

    private static final String KEY_PEEK = "notification_peek";
    private static final String PEEK_APPLICATION = "com.jedga.peek";
    private static final String KEY_SEE_THROUGH = "see_through";
    private static final String KEY_BLUR_RADIUS = "blur_radius";
    private static final String KEY_PEEK_PICKUP_TIMEOUT = "peek_pickup_timeout";
    private static final String KEY_PEEK_WAKE_TIMEOUT = "peek_wake_timeout";
    private static final String KEY_LOCKSCREEN_GENERAL_CATEGORY = "lockscreen_general_category";

    private SystemSettingCheckBoxPreference mNotificationPeek;
    private SystemSettingCheckBoxPreference mSeeThrough;
    private SeekBarPreference mBlurRadius;
    private PackageStatusReceiver mPackageStatusReceiver;
    private IntentFilter mIntentFilter;
    private ListPreference mPeekPickupTimeout;
    private ListPreference mPeekWakeTimeout;

    private boolean isPeekAppInstalled() {
        return isPackageInstalled(PEEK_APPLICATION);
    }

    private boolean isPackageInstalled(String packagename) {
        PackageManager pm = getActivity().getPackageManager();
        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_settings);
        PreferenceScreen prefSet = getPreferenceScreen();

 	// lockscreen see through
        mSeeThrough = (SystemSettingCheckBoxPreference) findPreference(KEY_SEE_THROUGH);
        if (mSeeThrough != null) {
            mSeeThrough.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_SEE_THROUGH, 0) == 1);
        }

	// lock screen blur radius
        mBlurRadius = (SeekBarPreference) findPreference(KEY_BLUR_RADIUS);
        mBlurRadius.setProgress(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_BLUR_RADIUS, 12));
        mBlurRadius.setOnPreferenceChangeListener(this);
        mBlurRadius.setEnabled(mSeeThrough.isChecked() && mSeeThrough.isEnabled());

	if (mPackageStatusReceiver == null) {
            mPackageStatusReceiver = new PackageStatusReceiver();
        }
        if (mIntentFilter == null) {
            mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
            mIntentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        }
        getActivity().registerReceiver(mPackageStatusReceiver, mIntentFilter);

	mNotificationPeek = (SystemSettingCheckBoxPreference) findPreference(KEY_PEEK);

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

    public boolean onPreferenceChange(Preference preference, Object objValue) {
    if (preference == mPeekPickupTimeout) {
	    int index = mPeekPickupTimeout.findIndexOfValue((String) objValue);
	    int peekPickupTimeout = Integer.valueOf((String) objValue);
            Settings.System.putIntForUser(getContentResolver(),
                Settings.System.PEEK_PICKUP_TIMEOUT,
 		peekPickupTimeout, UserHandle.USER_CURRENT);
	    mPeekPickupTimeout.setSummary(mPeekPickupTimeout.getEntries()[index]);
            return true;
	} else if (preference == mPeekWakeTimeout) {
            int index = mPeekWakeTimeout.findIndexOfValue((String) objValue);
            int peekWakeTimeout = Integer.valueOf((String) objValue);
            Settings.System.putIntForUser(getContentResolver(),
                Settings.System.PEEK_WAKE_TIMEOUT,
                    peekWakeTimeout, UserHandle.USER_CURRENT);
            mPeekWakeTimeout.setSummary(mPeekWakeTimeout.getEntries()[index]);
            return true;
	} else if (preference == mBlurRadius) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_BLUR_RADIUS,
                    (Integer) objValue);
            return true;
	}
        return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
	boolean value;
	if (preference == mSeeThrough) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_SEE_THROUGH,
                    mSeeThrough.isChecked() ? 1 : 0);
		mBlurRadius.setEnabled(mSeeThrough.isChecked() && mSeeThrough.isEnabled());
            return true;
	} else if (preference == mNotificationPeek) {
            Settings.System.putInt(getContentResolver(), Settings.System.PEEK_STATE,
                    mNotificationPeek.isChecked() ? 1 : 0);
	return true; 
	}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onResume() {
	getActivity().registerReceiver(mPackageStatusReceiver, mIntentFilter);
        super.onResume();
    }

 private void updateState() {
        updatePeekCheckbox();
    }

    private void updatePeekCheckbox() {
        boolean enabled = Settings.System.getInt(getContentResolver(),
                Settings.System.PEEK_STATE, 0) == 1;
        mNotificationPeek.setChecked(enabled && !isPeekAppInstalled());
        mNotificationPeek.setEnabled(!isPeekAppInstalled());
      }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mPackageStatusReceiver);
    }

     public class PackageStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
                updatePeekCheckbox();
            } else if(action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
                updatePeekCheckbox();
            }
        }
    }
}
