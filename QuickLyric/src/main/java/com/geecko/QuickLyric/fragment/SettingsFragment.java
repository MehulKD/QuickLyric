/*
 * *
 *  * This file is part of QuickLyric
 *  * Created by geecko
 *  *
 *  * QuickLyric is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * QuickLyric is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  * You should have received a copy of the GNU General Public License
 *  * along with QuickLyric.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.geecko.QuickLyric.fragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.geecko.QuickLyric.MainActivity;
import com.geecko.QuickLyric.R;
import com.geecko.QuickLyric.adapter.DrawerAdapter;
import com.geecko.QuickLyric.utils.NightTimeVerifier;

public class SettingsFragment extends PreferenceFragment implements
        Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    public boolean showTransitionAnim = true;
    public boolean isActiveFragment = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        findPreference("pref_contribute").setOnPreferenceClickListener(this);
        findPreference("pref_issues").setOnPreferenceClickListener(this);
        findPreference("pref_about").setOnPreferenceClickListener(this);
        findPreference("pref_theme").setOnPreferenceChangeListener(this);
        findPreference("pref_night_mode").setOnPreferenceChangeListener(this);
        findPreference("pref_notifications").setOnPreferenceChangeListener(this);
        findPreference("pref_providers").setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {
        switch (pref.getKey()) {
            case "pref_theme":
                if (!pref.getSharedPreferences().getString("pref_theme", "0").equals(newValue)) {
                    getActivity().finish();
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setAction("android.intent.action.MAIN");
                    startActivity(intent);
                }
                return true;
            case "pref_notifications":
                if (newValue.equals("0")) {
                    ((NotificationManager) getActivity()
                            .getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
                } else {
                    SharedPreferences current = getActivity().getSharedPreferences("current_music", Context.MODE_PRIVATE);
                    Intent intent = new Intent();
                    intent.setAction("com.geecko.QuickLyric.SHOW_NOTIFICATION");
                    intent.putExtra("artist", current.getString("artist", "Michael Jackson"));
                    intent.putExtra("track", current.getString("track", "Bad"));
                    intent.putExtra("playing", current.getBoolean("playing", false));
                    getActivity().sendBroadcast(intent);
                }
                return true;
            case "pref_night_mode":
                if (NightTimeVerifier.check()
                        && pref.getSharedPreferences().getBoolean(pref.getKey(), false) != newValue) {
                    getActivity().finish();
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setAction("android.intent.action.MAIN");
                    startActivity(intent);
                }
                return true;
            default:
                return true;
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case "pref_about":
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setView(getActivity().getLayoutInflater().inflate(R.layout.about_dialog, (ViewGroup) getView(), false));
                dialog.create().show();
                break;
            case "pref_contribute":
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse("https://github.com/geecko86/QuickLyric"));
                if (browserIntent.resolveActivity(getActivity().getPackageManager()) != null)
                    startActivity(browserIntent);
                break;
            case "pref_issues":
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"quicklyricapp@gmail.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Issues with QuickLyric");
                if (emailIntent.resolveActivity(getActivity().getPackageManager()) != null)
                    startActivity(emailIntent);
                break;
        }
        return true;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (this.isHidden())
            return;
        final MainActivity activity = ((MainActivity) this.getActivity());
        View fragmentView = getView();
        TypedValue typedValue = new TypedValue();
        view.getContext().getTheme().resolveAttribute(android.R.attr.colorBackground, typedValue, true);
        if (fragmentView != null)
            fragmentView.setBackgroundColor(typedValue.data);
        DrawerAdapter drawerAdapter = ((DrawerAdapter) ((ListView) activity.findViewById(R.id.drawer_list)).getAdapter());

        if (drawerAdapter.getSelectedItem() != 2) {
            drawerAdapter.setSelectedItem(2);
            drawerAdapter.notifyDataSetChanged();
        }
        this.isActiveFragment = true;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden)
            this.onViewCreated(getView(), null);
        else
            this.isActiveFragment = false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MainActivity mainActivity = (MainActivity) this.getActivity();
        ActionBar actionBar = (mainActivity).getSupportActionBar();
        if (mainActivity.focusOnFragment && actionBar != null) // focus is on Fragment
        {
            if (actionBar.getTitle() == null || !actionBar.getTitle().equals(this.getString(R.string.settings_title)))
                actionBar.setTitle(R.string.settings_title);
        } else
            menu.clear();
    }

    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        final MainActivity mainActivity = (MainActivity) getActivity();
        Animator anim = null;
        if (showTransitionAnim) {
            if (nextAnim != 0)
                anim = AnimatorInflater.loadAnimator(getActivity(), nextAnim);
            showTransitionAnim = false;
            if (anim != null)
                anim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        if (mainActivity.drawer instanceof DrawerLayout)
                            ((DrawerLayout) mainActivity.drawer).closeDrawer(mainActivity.drawerView);
                        mainActivity.setDrawerListener(true);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                    }

                    @Override
                    public void onAnimationStart(Animator animator) {
                        mainActivity.setDrawerListener(false);
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                    }
                });
        } else
            anim = AnimatorInflater.loadAnimator(getActivity(), R.animator.none);
        return anim;
    }
}
