/*
* Copyright (C) 2013 SlimRoms Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.android.settings.simpleaosp;

import android.app.Activity;
import android.app.ActionBar;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SeekBarPreference;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class BlurSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

	private static final String KEY_SEE_THROUGH = "see_through";
	private static final String KEY_BLUR_RADIUS = "blur_radius";

	private SwitchPreference mSeeThrough;
	private SeekBarPreference mBlurRadius;

	private Switch mActionBarSwitch;
	private BlurEnabler mBlurEnabler;
	private ViewGroup mPrefsContainer;
	private View mDisabledText;
	private ContentObserver mSettingsObserver = new ContentObserver(new Handler()) {
	@Override
	public void onChange(boolean selfChange, Uri uri) {
	updateEnabledState();
	}
	};
	@Override
	public void onActivityCreated(Bundle icicle) {
	// We don't call super.onActivityCreated() here, since it assumes we already set up
	// Preference (probably in onCreate()), while ProfilesSettings exceptionally set it up in
	// this method.
	// On/off switch
	Activity activity = getActivity();
	//Switch
	mActionBarSwitch = new Switch(activity);
	if (activity instanceof PreferenceActivity) {
	PreferenceActivity preferenceActivity = (PreferenceActivity) activity;
	if (preferenceActivity.onIsHidingHeaders() || !preferenceActivity.onIsMultiPane()) {
	final int padding = activity.getResources().getDimensionPixelSize(
	R.dimen.action_bar_switch_padding);
	mActionBarSwitch.setPaddingRelative(0, 0, padding, 0);
	activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
	ActionBar.DISPLAY_SHOW_CUSTOM);
	activity.getActionBar().setCustomView(mActionBarSwitch, new ActionBar.LayoutParams(
	ActionBar.LayoutParams.WRAP_CONTENT,
	ActionBar.LayoutParams.WRAP_CONTENT,
	Gravity.CENTER_VERTICAL | Gravity.END));
	}
	}
	mBlurEnabler = new BlurEnabler(activity, mActionBarSwitch);
	// After confirming PreferenceScreen is available, we call super.
	super.onActivityCreated(icicle);
	setHasOptionsMenu(true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	addPreferencesFromResource(R.xml.blur_settings);
        PreferenceScreen prefSet = getPreferenceScreen();

	// lock screen blur radius
        mBlurRadius = (SeekBarPreference) findPreference(KEY_BLUR_RADIUS);
        mBlurRadius.setProgress(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_BLUR_RADIUS, 12));
        mBlurRadius.setOnPreferenceChangeListener(this);
	}

	public boolean onPreferenceChange(Preference preference, Object objValue) {
	ContentResolver cr = getActivity().getContentResolver();
	if (preference == mBlurRadius) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_BLUR_RADIUS,
                    (Integer) objValue);
            return true;
	}
	return false;
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
	return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	Bundle savedInstanceState) {
	View v = inflater.inflate(R.layout.blur_fragment, container, false);
	mPrefsContainer = (ViewGroup) v.findViewById(R.id.prefs_container);
	mDisabledText = v.findViewById(R.id.disabled_text);
	View prefs = super.onCreateView(inflater, mPrefsContainer, savedInstanceState);
	mPrefsContainer.addView(prefs);
	return v;
	}

	@Override
	public void onResume() {
	super.onResume();
	if (mBlurEnabler != null) {
	mBlurEnabler.resume();
	}
	getContentResolver().registerContentObserver(
	Settings.System.getUriFor(Settings.System.LOCKSCREEN_SEE_THROUGH),
	true, mSettingsObserver);
	updateEnabledState();
	}

	public void onPause() {
	super.onPause();
	if (mBlurEnabler != null) {
	mBlurEnabler.pause();
	}
	getContentResolver().unregisterContentObserver(mSettingsObserver);
	}

	private void updateEnabledState() {
	boolean enabled = Settings.System.getInt(getContentResolver(),
	Settings.System.LOCKSCREEN_SEE_THROUGH, 0) != 0;
	mPrefsContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);
	mDisabledText.setVisibility(enabled ? View.GONE : View.VISIBLE);
		}
}
