package com.android.settings.simpleaosp;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.Settings;
import android.view.Gravity;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.preference.ListPreference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class PeekSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {


    private static final String PEEK_APPLICATION = "com.jedga.peek";
    private static final String KEY_PEEK_PICKUP_TIMEOUT = "peek_pickup_timeout";
    private static final String KEY_PEEK_WAKE_TIMEOUT = "peek_wake_timeout";

    private SwitchPreference mNotificationPeek;
    private ListPreference mPeekPickupTimeout;
    private ListPreference mPeekWakeTimeout;
    private PackageStatusReceiver mPackageStatusReceiver;
    private IntentFilter mIntentFilter;

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

	private Switch mActionBarSwitch;
	private PeekEnabler mPeekEnabler;
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
	mPeekEnabler = new PeekEnabler(activity, mActionBarSwitch);
	// After confirming PreferenceScreen is available, we call super.
	super.onActivityCreated(icicle);
	setHasOptionsMenu(true);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.peek_settings);
        PreferenceScreen prefSet = getPreferenceScreen();


	if (mPackageStatusReceiver == null) {
            mPackageStatusReceiver = new PackageStatusReceiver();
        }
        if (mIntentFilter == null) {
            mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
            mIntentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        }
        getActivity().registerReceiver(mPackageStatusReceiver, mIntentFilter);

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	Bundle savedInstanceState) {
	View v = inflater.inflate(R.layout.peek_fragment, container, false);
	mPrefsContainer = (ViewGroup) v.findViewById(R.id.prefs_container);
	mDisabledText = v.findViewById(R.id.disabled_text);
	View prefs = super.onCreateView(inflater, mPrefsContainer, savedInstanceState);
	mPrefsContainer.addView(prefs);
	return v;
	}

    @Override
    public void onResume() {
	getActivity().registerReceiver(mPackageStatusReceiver, mIntentFilter);
        super.onResume();
	if (mPeekEnabler != null) {
	mPeekEnabler.resume();
	}
	getContentResolver().registerContentObserver(
	Settings.System.getUriFor(Settings.System.PEEK_STATE),
	true, mSettingsObserver);
	updateEnabledState();
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
		ContentResolver cr = getActivity().getContentResolver();
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
	}
        return false;
    }

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
	return super.onPreferenceTreeClick(preferenceScreen, preference);
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
	if (mPeekEnabler != null) {
	mPeekEnabler.pause();
	}
	getContentResolver().unregisterContentObserver(mSettingsObserver);
    }

	private void updateEnabledState() {
	boolean enabled = Settings.System.getInt(getContentResolver(),
	Settings.System.PEEK_STATE, 0) != 0;
	mPrefsContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);
	mDisabledText.setVisibility(enabled ? View.GONE : View.VISIBLE);
	}

	public void UpdateSettings() {}

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

