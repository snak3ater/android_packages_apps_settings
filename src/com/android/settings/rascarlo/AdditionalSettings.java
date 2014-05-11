package com.android.settings.rascarlo;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.settings.rascarlo.StatusBarSettings;
import com.android.settings.rascarlo.NavigationBarSettings;
import com.android.settings.rascarlo.SystemSettings;
import com.android.settings.rascarlo.LockscreenSettings;
import com.android.settings.rascarlo.DepthPageTransformer;
import com.android.internal.util.rascarlo.DeviceUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;


import java.util.ArrayList;
import java.util.List;

public class AdditionalSettings extends SettingsPreferenceFragment implements ActionBar.TabListener {

    ViewPager mViewPager;
    String titleString[];
    ViewGroup mContainer;

    static Bundle mSavedState;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContainer = container;
	final ActionBar actionBar = getActivity().getActionBar();
        actionBar.setIcon(R.drawable.ic_settings_system);

        View view = inflater.inflate(R.layout.system_settings, container, false);
        mViewPager = (ViewPager) view.findViewById(R.id.viewPager);

        StatusBarAdapter StatusBarAdapter = new StatusBarAdapter(getFragmentManager());
        mViewPager.setAdapter(StatusBarAdapter);

mViewPager.setPageTransformer(true, new DepthPageTransformer());
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        ActionBar.Tab systemTab = actionBar.newTab();
        systemTab.setText("System");
        systemTab.setTabListener(this);

        ActionBar.Tab statusBarTab = actionBar.newTab();
        statusBarTab.setText("Status Bar");
        statusBarTab.setTabListener(this);

        ActionBar.Tab navBarTab = actionBar.newTab();
        navBarTab.setText("Navigation Bar");
        navBarTab.setTabListener(this);

        ActionBar.Tab lockscreenTab = actionBar.newTab();
        lockscreenTab.setText("Lockscreen");
        lockscreenTab.setTabListener(this);

        actionBar.addTab(systemTab);
        actionBar.addTab(statusBarTab);
        actionBar.addTab(navBarTab);
        actionBar.addTab(lockscreenTab);

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        setHasOptionsMenu(true);
        return view;
    }

   @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        // User selected the already selected tab. Usually do nothing.
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // After confirming PreferenceScreen is available, we call super.
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle saveState) {
        super.onSaveInstanceState(saveState);
    }

    @Override
    public void onResume() {
        super.onResume();
 	if (!DeviceUtils.isTablet(getActivity())) {
            mContainer.setPadding(0, 0, 0, 0);
        }
    }

    class StatusBarAdapter extends FragmentPagerAdapter {
        String titles[] = getTitles();
        private Fragment frags[] = new Fragment[titles.length];

        public StatusBarAdapter(FragmentManager fm) {
            super(fm);
	    frags[0] = new SystemSettings();
            frags[1] = new StatusBarSettings();
            frags[2] = new NavigationBarSettings();
            frags[3] = new LockscreenSettings();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return frags[position];
        }

        @Override
        public int getCount() {
            return frags.length;
        }
    }

    private String[] getTitles() {
        String titleString[];
        titleString = new String[]{
		    getString(R.string.system_category),
                    getString(R.string.status_bar_title),
                    getString(R.string.navigation_bar_title),
                    getString(R.string.lockscreen_title)};
        return titleString;
    }
}

