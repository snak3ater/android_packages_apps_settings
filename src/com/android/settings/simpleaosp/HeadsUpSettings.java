package com.android.settings.simpleaosp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.app.Activity;
import android.app.ActionBar;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.provider.Settings;
import android.os.Handler;
import android.os.UserHandle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
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

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class HeadsUpSettings extends SettingsPreferenceFragment
            implements OnPreferenceChangeListener  {

    public static final String TAG = "HeadsUpSettings";

    // Default timeout for heads up snooze. 5 minutes.
    protected static final int DEFAULT_TIME_HEADS_UP_SNOOZE = 300000;

    private static final String PREF_HEADS_UP_EXPANDED = "heads_up_expanded";
    private static final String PREF_HEADS_UP_SNOOZE_TIME = "heads_up_snooze_time";
    private static final String PREF_HEADS_UP_TIME_OUT = "heads_up_time_out";
    private static final String PREF_HEADS_UP_SHOW_UPDATE = "heads_up_show_update";
    private static final String PREF_HEADS_UP_GRAVITY = "heads_up_gravity";
    private static final String PREF_HEADS_UP_EXCLUDE_FROM_LOCK_SCREEN = "heads_up_exclude_from_lock_screen";
    private static final String HEADS_UP_BG_COLOR = "heads_up_bg_color";
    private static final String HEADS_UP_TEXT_COLOR = "heads_up_text_color";
    
    ListPreference mHeadsUpSnoozeTime;
    ListPreference mHeadsUpTimeOut;
    CheckBoxPreference mHeadsUpExpanded;
    CheckBoxPreference mHeadsUpShowUpdates;
    CheckBoxPreference mHeadsUpGravity;
    CheckBoxPreference mHeadsExcludeFromLockscreen;
    private ColorPickerPreference mHeadsUpBgColor;
    private ColorPickerPreference mHeadsUpTextColor;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DEFAULT_BACKGROUND_COLOR = 0x00ffffff;
    private static final int DEFAULT_TEXT_COLOR = 0xffffffff;

    private Switch mActionBarSwitch;
    private HeadsupNotificationsEnabler mHeadsupEnabler;

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

        mHeadsupEnabler = new HeadsupNotificationsEnabler(activity, mActionBarSwitch);
        // After confirming PreferenceScreen is available, we call super.
          super.onActivityCreated(icicle);
          setHasOptionsMenu(true);
      }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.heads_up_settings);

        PreferenceScreen prefs = getPreferenceScreen();

        PackageManager pm = getPackageManager();

	// Heads Up background color
        mHeadsUpBgColor = (ColorPickerPreference) findPreference(HEADS_UP_BG_COLOR);
        mHeadsUpBgColor.setOnPreferenceChangeListener(this);
        final int intColor = Settings.System.getInt(getContentResolver(),
                Settings.System.HEADS_UP_BG_COLOR, 0x00ffffff);
        String hexColor = String.format("#%08x", (0x00ffffff & intColor));
        if (hexColor.equals("#00ffffff")) {
            mHeadsUpBgColor.setSummary(R.string.heads_up_default_color);
        } else {
            mHeadsUpBgColor.setSummary(hexColor);
        }
        mHeadsUpBgColor.setNewPreviewColor(intColor);

        // Heads Up text color
        mHeadsUpTextColor = (ColorPickerPreference) findPreference(HEADS_UP_TEXT_COLOR);
        mHeadsUpTextColor.setOnPreferenceChangeListener(this);
        final int intTextColor = Settings.System.getInt(getContentResolver(),
                Settings.System.HEADS_UP_TEXT_COLOR, 0x00000000);
        String hexTextColor = String.format("#%08x", (0x00000000 & intTextColor));
        if (hexTextColor.equals("#00000000")) {
            mHeadsUpTextColor.setSummary(R.string.heads_up_default_color);
        } else {
            mHeadsUpTextColor.setSummary(hexTextColor);
        }
        mHeadsUpTextColor.setNewPreviewColor(intTextColor);
        setHasOptionsMenu(true);

        mHeadsUpExpanded = (CheckBoxPreference) findPreference(PREF_HEADS_UP_EXPANDED);
        mHeadsUpExpanded.setChecked(Settings.System.getIntForUser(getContentResolver(),
                Settings.System.HEADS_UP_EXPANDED, 0, UserHandle.USER_CURRENT) == 1);
        mHeadsUpExpanded.setOnPreferenceChangeListener(this);

        mHeadsUpShowUpdates = (CheckBoxPreference) findPreference(PREF_HEADS_UP_SHOW_UPDATE);
        mHeadsUpShowUpdates.setChecked(Settings.System.getIntForUser(getContentResolver(),
                Settings.System.HEADS_UP_SHOW_UPDATE, 0, UserHandle.USER_CURRENT) == 1);
        mHeadsUpShowUpdates.setOnPreferenceChangeListener(this);

        mHeadsUpGravity = (CheckBoxPreference) findPreference(PREF_HEADS_UP_GRAVITY);
        mHeadsUpGravity.setChecked(Settings.System.getIntForUser(getContentResolver(),
                Settings.System.HEADS_UP_GRAVITY_BOTTOM, 0, UserHandle.USER_CURRENT) == 1);
        mHeadsUpGravity.setOnPreferenceChangeListener(this);

        mHeadsExcludeFromLockscreen = (CheckBoxPreference) findPreference(PREF_HEADS_UP_EXCLUDE_FROM_LOCK_SCREEN);
        mHeadsExcludeFromLockscreen.setChecked(Settings.System.getIntForUser(getContentResolver(),
                Settings.System.HEADS_UP_EXCLUDE_FROM_LOCK_SCREEN, 0, UserHandle.USER_CURRENT) == 1);
        mHeadsExcludeFromLockscreen.setOnPreferenceChangeListener(this);

        mHeadsUpSnoozeTime = (ListPreference) findPreference(PREF_HEADS_UP_SNOOZE_TIME);
        mHeadsUpSnoozeTime.setOnPreferenceChangeListener(this);
        int headsUpSnoozeTime = Settings.System.getInt(getContentResolver(),
                Settings.System.HEADS_UP_SNOOZE_TIME, DEFAULT_TIME_HEADS_UP_SNOOZE);
        mHeadsUpSnoozeTime.setValue(String.valueOf(headsUpSnoozeTime));
        updateHeadsUpSnoozeTimeSummary(headsUpSnoozeTime);

        Resources systemUiResources;
        try {
            systemUiResources = pm.getResourcesForApplication("com.android.systemui");
        } catch (Exception e) {
            return;
        }

        int defaultTimeOut = systemUiResources.getInteger(systemUiResources.getIdentifier(
                    "com.android.systemui:integer/heads_up_notification_decay", null, null));
        mHeadsUpTimeOut = (ListPreference) findPreference(PREF_HEADS_UP_TIME_OUT);
        mHeadsUpTimeOut.setOnPreferenceChangeListener(this);
        int headsUpTimeOut = Settings.System.getInt(getContentResolver(),
                Settings.System.HEADS_UP_NOTIFCATION_DECAY, defaultTimeOut);
        mHeadsUpTimeOut.setValue(String.valueOf(headsUpTimeOut));
        updateHeadsUpTimeOutSummary(headsUpTimeOut);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHeadsUpSnoozeTime) {
            int headsUpSnoozeTime = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.HEADS_UP_SNOOZE_TIME,
                    headsUpSnoozeTime);
            updateHeadsUpSnoozeTimeSummary(headsUpSnoozeTime);
            return true;
        } else if (preference == mHeadsUpTimeOut) {
            int headsUpTimeOut = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.HEADS_UP_NOTIFCATION_DECAY,
                    headsUpTimeOut);
            updateHeadsUpTimeOutSummary(headsUpTimeOut);
            return true;
        } else if (preference == mHeadsUpExpanded) {
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.HEADS_UP_EXPANDED,
                    (Boolean) newValue ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mHeadsUpShowUpdates) {
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.HEADS_UP_SHOW_UPDATE,
                    (Boolean) newValue ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mHeadsUpGravity) {
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.HEADS_UP_GRAVITY_BOTTOM,
                    (Boolean) newValue ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mHeadsExcludeFromLockscreen) {
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.HEADS_UP_EXCLUDE_FROM_LOCK_SCREEN,
                    (Boolean) newValue ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
	    } else if (preference == mHeadsUpBgColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#00ffffff")) {
                preference.setSummary(R.string.heads_up_default_color);
            } else {
                preference.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.HEADS_UP_BG_COLOR,
                    intHex);
            return true;
        } else if (preference == mHeadsUpTextColor) {
            String hexText = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hexText.equals("#00000000")) {
                preference.setSummary(R.string.heads_up_default_color);
            } else {
                preference.setSummary(hexText);
            }
            int intHexText = ColorPickerPreference.convertToColorInt(hexText);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.HEADS_UP_TEXT_COLOR,
                    intHexText);
            return true;
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.headsup_fragment, container, false);
        mPrefsContainer = (ViewGroup) v.findViewById(R.id.prefs_container);
        mDisabledText = v.findViewById(R.id.disabled_text);

        View prefs = super.onCreateView(inflater, mPrefsContainer, savedInstanceState);
        mPrefsContainer.addView(prefs);

        return v;
    }

    @Override
      public void onResume() {
        super.onResume();
        if (mHeadsupEnabler != null) {
            mHeadsupEnabler.resume();
        }
        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.HEADS_UP_MASTER_SWITCH),
                true, mSettingsObserver);
        updateEnabledState();
    }

    public void onPause() {
        super.onPause();
        if (mHeadsupEnabler != null) {
            mHeadsupEnabler.pause();
        }
        getContentResolver().unregisterContentObserver(mSettingsObserver);
    }

    private void updateHeadsUpSnoozeTimeSummary(int value) {
        String summary = value != 0
                ? getResources().getString(R.string.heads_up_snooze_summary, value / 60 / 1000)
                : getResources().getString(R.string.heads_up_snooze_disabled_summary);
        mHeadsUpSnoozeTime.setSummary(summary);
    }

    private void updateHeadsUpTimeOutSummary(int value) {
        String summary = getResources().getString(R.string.heads_up_time_out_summary,
                value / 1000);
        if (value == 0) {
            mHeadsUpTimeOut.setSummary(
                    getResources().getString(R.string.heads_up_time_out_never_summary));
        } else {
            mHeadsUpTimeOut.setSummary(summary);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset_default_message)
                .setIcon(R.drawable.ic_settings_backup)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetToDefault();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void resetToDefault() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.shortcut_action_reset);
        alertDialog.setMessage(R.string.heads_up_reset_message);
        alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resetValues();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    private void resetValues() {
        Settings.System.putInt(getContentResolver(),
                Settings.System.HEADS_UP_BG_COLOR, DEFAULT_BACKGROUND_COLOR);
        mHeadsUpBgColor.setNewPreviewColor(DEFAULT_BACKGROUND_COLOR);
        mHeadsUpBgColor.setSummary(R.string.heads_up_default_color);
        Settings.System.putInt(getContentResolver(),
                Settings.System.HEADS_UP_TEXT_COLOR, 0);
        mHeadsUpTextColor.setNewPreviewColor(DEFAULT_TEXT_COLOR);
        mHeadsUpTextColor.setSummary(R.string.heads_up_default_color);
	}

	private void updateEnabledState() {
        boolean enabled = Settings.System.getInt(getContentResolver(),
                Settings.System.HEADS_UP_MASTER_SWITCH, 0) != 0;
        mPrefsContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);
        mDisabledText.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }
}
