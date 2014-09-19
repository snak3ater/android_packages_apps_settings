package com.android.settings.simpleaosp;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

public class PieSettings extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String KEY_PIE_SIZE = "pie_size";
    private static final String KEY_PIE_GAP = "pie_gap";
    private static final String KEY_PIE_ANGLE = "pie_angle";

    private ListPreference mPieSize;
    private ListPreference mPieGap;
    private ListPreference mPieAngle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pie_settings);
        PreferenceScreen prefs = getPreferenceScreen();
        ContentResolver cr = getActivity().getContentResolver();

        mPieSize = (ListPreference) prefs.findPreference(KEY_PIE_SIZE);
        float pieSize = Settings.System.getFloat(getContentResolver(),
                Settings.System.PIE_SIZE, 1.0f);
        mPieSize.setValue(String.valueOf(pieSize));
        mPieSize.setOnPreferenceChangeListener(this);

        mPieGap = (ListPreference) prefs.findPreference(KEY_PIE_GAP);
        int pieGap = Settings.System.getInt(getContentResolver(),
                Settings.System.PIE_GAP, 1);
        mPieGap.setValue(String.valueOf(pieGap));
        mPieGap.setOnPreferenceChangeListener(this);

        mPieAngle = (ListPreference) prefs.findPreference(KEY_PIE_ANGLE);
        int pieAngle = Settings.System.getInt(getContentResolver(),
                Settings.System.PIE_ANGLE, 12);
        mPieAngle.setValue(String.valueOf(pieAngle));
        mPieAngle.setOnPreferenceChangeListener(this);

        // Enable ActionBar menu
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.pie_settings_item, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reset:
                showResetDialog();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void showResetDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.pie_settings_reset);
        alertDialog.setMessage(R.string.pie_settings_reset_message);
        alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resetPieSettings();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object value) {
        if (pref == mPieSize) {
            float pieSize = Float.valueOf((String) value);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.PIE_SIZE, pieSize);
            return true;
        } else if (pref == mPieGap) {
            int pieGap = Integer.valueOf((String) value);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_GAP, pieGap);
            return true;
        } else if (pref == mPieAngle) {
            int pieAngle = Integer.valueOf((String) value);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_ANGLE, pieAngle);
            return true;
        }
        return false;
    }

    private void resetPieSettings() {
        Settings.System.putFloat(getContentResolver(),
                Settings.System.PIE_SIZE, 1.0f);
        Settings.System.putInt(getContentResolver(),
                Settings.System.PIE_GAP, 1);
        Settings.System.putInt(getContentResolver(),
                Settings.System.PIE_ANGLE, 12);

        mPieSize.setValueIndex(2);
        mPieGap.setValueIndex(0);
        mPieAngle.setValueIndex(2);
    }
}
