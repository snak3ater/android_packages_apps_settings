package com.android.settings.simpleaosp;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
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

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class HoverSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "HoverSettings";

    private static final String PREF_HOVER_LONG_FADE_OUT_DELAY = "hover_long_fade_out_delay";
    private static final String PREF_HOVER_EXCLUDE_NON_CLEARABLE = "hover_exclude_non_clearable";
    private static final String PREF_HOVER_EXCLUDE_LOW_PRIORITY = "hover_exclude_low_priority";
    private static final String PREF_HOVER_REQUIRE_FULLSCREEN_MODE = "hover_require_fullscreen_mode";
    private static final String PREF_HOVER_EXCLUDE_TOPMOST = "hover_exclude_topmost";

    ListPreference mHoverLongFadeOutDelay;
    CheckBoxPreference mHoverExcludeNonClearable;
    CheckBoxPreference mHoverExcludeNonLowPriority;
    CheckBoxPreference mHoverRequireFullScreenMode;
    CheckBoxPreference mHoverExcludeTopmost;

    private Switch mActionBarSwitch;
    private HoverNotificationsEnabler mHoverEnabler;

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

        mHoverEnabler = new HoverNotificationsEnabler(activity, mActionBarSwitch);
        // After confirming PreferenceScreen is available, we call super.
          super.onActivityCreated(icicle);
          setHasOptionsMenu(true);
      }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.hover_settings);
        PreferenceScreen prefSet = getPreferenceScreen();

        mHoverLongFadeOutDelay = (ListPreference) prefSet.findPreference(PREF_HOVER_LONG_FADE_OUT_DELAY);
        int hoverLongFadeOutDelay = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.HOVER_LONG_FADE_OUT_DELAY, 5000, UserHandle.USER_CURRENT);
        mHoverLongFadeOutDelay.setValue(String.valueOf(hoverLongFadeOutDelay));
        mHoverLongFadeOutDelay.setSummary(mHoverLongFadeOutDelay.getEntry());
        mHoverLongFadeOutDelay.setOnPreferenceChangeListener(this);

        mHoverExcludeNonClearable = (CheckBoxPreference) findPreference(PREF_HOVER_EXCLUDE_NON_CLEARABLE);
        mHoverExcludeNonClearable.setChecked(Settings.System.getIntForUser(getContentResolver(),
                Settings.System.HOVER_EXCLUDE_NON_CLEARABLE, 0, UserHandle.USER_CURRENT) == 1);
        mHoverExcludeNonClearable.setOnPreferenceChangeListener(this);

        mHoverExcludeNonLowPriority = (CheckBoxPreference) findPreference(PREF_HOVER_EXCLUDE_LOW_PRIORITY);
        mHoverExcludeNonLowPriority.setChecked(Settings.System.getIntForUser(getContentResolver(),
                Settings.System.HOVER_EXCLUDE_LOW_PRIORITY, 0, UserHandle.USER_CURRENT) == 1);
        mHoverExcludeNonLowPriority.setOnPreferenceChangeListener(this);

        mHoverRequireFullScreenMode = (CheckBoxPreference) findPreference(PREF_HOVER_REQUIRE_FULLSCREEN_MODE);
        mHoverRequireFullScreenMode.setChecked(Settings.System.getIntForUser(getContentResolver(),
                Settings.System.HOVER_REQUIRE_FULLSCREEN_MODE, 0, UserHandle.USER_CURRENT) == 1);
        mHoverRequireFullScreenMode.setOnPreferenceChangeListener(this);

        mHoverExcludeTopmost = (CheckBoxPreference) findPreference(PREF_HOVER_EXCLUDE_TOPMOST);
        mHoverExcludeTopmost.setChecked(Settings.System.getIntForUser(getContentResolver(),
                Settings.System.HOVER_EXCLUDE_TOPMOST, 0, UserHandle.USER_CURRENT) == 1);
        mHoverExcludeTopmost.setOnPreferenceChangeListener(this);

        UpdateSettings();
    }

    public void UpdateSettings() {}

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mHoverLongFadeOutDelay) {
            int index = mHoverLongFadeOutDelay.findIndexOfValue((String) objValue);
            int hoverLongFadeOutDelay = Integer.valueOf((String) objValue);
            Settings.System.putIntForUser(getContentResolver(),
                Settings.System.HOVER_LONG_FADE_OUT_DELAY,
                    hoverLongFadeOutDelay, UserHandle.USER_CURRENT);
            mHoverLongFadeOutDelay.setSummary(mHoverLongFadeOutDelay.getEntries()[index]);
            return true;
        } else if (preference == mHoverExcludeNonClearable) {
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.HOVER_EXCLUDE_NON_CLEARABLE,
                    (Boolean) objValue ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mHoverExcludeNonLowPriority) {
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.HOVER_EXCLUDE_LOW_PRIORITY,
                    (Boolean) objValue ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mHoverRequireFullScreenMode) {
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.HOVER_REQUIRE_FULLSCREEN_MODE,
                    (Boolean) objValue ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mHoverExcludeTopmost) {
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.HOVER_EXCLUDE_TOPMOST,
                    (Boolean) objValue ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.hover_fragment, container, false);
        mPrefsContainer = (ViewGroup) v.findViewById(R.id.prefs_container);
        mDisabledText = v.findViewById(R.id.disabled_text);

        View prefs = super.onCreateView(inflater, mPrefsContainer, savedInstanceState);
        mPrefsContainer.addView(prefs);

        return v;
    }

    @Override
      public void onResume() {
        super.onResume();
        if (mHoverEnabler != null) {
            mHoverEnabler.resume();
        }
        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.HOVER_STATE),
                true, mSettingsObserver);
        updateEnabledState();
        UpdateSettings();
    }

    public void onPause() {
        super.onPause();
        if (mHoverEnabler != null) {
            mHoverEnabler.pause();
        }
        getContentResolver().unregisterContentObserver(mSettingsObserver);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

	private void updateEnabledState() {
        boolean enabled = Settings.System.getInt(getContentResolver(),
                Settings.System.HOVER_STATE, 0) != 0;
        mPrefsContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);
        mDisabledText.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }
}
