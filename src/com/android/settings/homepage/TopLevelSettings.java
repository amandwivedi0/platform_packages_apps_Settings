/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.settings.homepage;

import static com.android.settings.search.actionbar.SearchMenuController.NEED_SEARCH_ICON_IN_ACTION_BAR;
import static com.android.settingslib.search.SearchIndexable.MOBILE;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceFragmentCompat;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.support.SupportPreferenceController;
import com.android.settingslib.core.instrumentation.Instrumentable;
import com.android.settingslib.search.SearchIndexable;
import com.android.settingslib.widget.AdaptiveIcon;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@SearchIndexable(forTarget = MOBILE)
public class TopLevelSettings extends DashboardFragment implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TAG = "TopLevelSettings";
    private static final String KEY_COLTENIGMA = "top_level_colt_settings";

    private int mIconStyle;
    private int mNormalColor;
    private int mAccentColor;

    public TopLevelSettings() {
        final Bundle args = new Bundle();
        // Disable the search icon because this page uses a full search view in actionbar.
        args.putBoolean(NEED_SEARCH_ICON_IN_ACTION_BAR, false);
        setArguments(args);
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.top_level_settings;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DASHBOARD_SUMMARY;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        use(SupportPreferenceController.class).setActivity(getActivity());
        updateColtSummary();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateColtSummary();
	boolean mCustomIcon = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.DASHBOARD_ICONS, 0, UserHandle.USER_CURRENT) == 1;
        if (mCustomIcon) {
            updateTheme();
        } else {
            // nothing todo
        }
    }

    @Override
    public int getHelpResource() {
        // Disable the help icon because this page uses a full search view in actionbar.
        return 0;
    }

    @Override
    public Fragment getCallbackFragment() {
        return this;
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        new SubSettingLauncher(getActivity())
                .setDestination(pref.getFragment())
                .setArguments(pref.getExtras())
                .setSourceMetricsCategory(caller instanceof Instrumentable
                        ? ((Instrumentable) caller).getMetricsCategory()
                        : Instrumentable.METRICS_CATEGORY_UNKNOWN)
                .setTitleRes(-1)
                .launch();
        return true;
    }

    @Override
    protected boolean shouldForceRoundedIcon() {
        return getContext().getResources()
                .getBoolean(R.bool.config_force_rounded_icon_TopLevelSettings);
    }

    private void updateTheme() {
        int[] attrs = new int[] {
            android.R.attr.colorControlNormal,
            android.R.attr.colorAccent,
        };
        TypedArray ta = getContext().getTheme().obtainStyledAttributes(attrs);
        mNormalColor = ta.getColor(0, 0xff808080);
        mAccentColor = ta.getColor(1, 0xff808080);
        ta.recycle();

        mIconStyle = Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.THEMING_SETTINGS_DASHBOARD_ICONS, 0);
        themePreferences(getPreferenceScreen());
    }

    private void themePreferences(PreferenceGroup prefGroup) {
        themePreference(prefGroup);
        for (int i = 0; i < prefGroup.getPreferenceCount(); i++) {
            Preference pref = prefGroup.getPreference(i);
            if (pref instanceof PreferenceGroup) {
                themePreferences(prefGroup);
            } else {
                themePreference(pref);
            }
        }
    }

    private void themePreference(Preference pref) {
        Drawable icon = pref.getIcon();
        if (icon != null) {
            if (icon instanceof AdaptiveIcon) {
                AdaptiveIcon aIcon = (AdaptiveIcon) icon;
                // Clear colors from previous calls
                aIcon.resetCustomColors();
                switch (mIconStyle) {
                    case 0:
                    default:
                        break;
                    case 1:
                        aIcon.setCustomForegroundColor(getResources().getColor(android.R.color.white));
                        break;
                    case 2:
                        aIcon.setCustomForegroundColor(mAccentColor);
                        break;
                    case 3:
                        aIcon.setCustomBackgroundColor(mAccentColor);
                        break;
                    case 4:
                        aIcon.setCustomBackgroundColor(mAccentColor);
                        aIcon.setCustomForegroundColor(getResources().getColor(android.R.color.white));
                        break;
                    case 5:
                        aIcon.setCustomForegroundColor(mNormalColor);
                        aIcon.setCustomBackgroundColor(0);
                        break;
                    case 6:
                        aIcon.setCustomForegroundColor(mAccentColor);
                        aIcon.setCustomBackgroundColor(0);
                        break;
                }
            } else if (icon instanceof LayerDrawable) {
                LayerDrawable lIcon = (LayerDrawable) icon;
                if (lIcon.getNumberOfLayers() == 2) {
                    Drawable fg = lIcon.getDrawable(1);
                    Drawable bg = lIcon.getDrawable(0);
                    // Clear tints from previous calls
                    bg.setTintList(null);
                    fg.setTintList(null);
                    switch (mIconStyle) {
                        case 0:
                        default:
                            break;
                        case 1:
                            fg.setTint(getResources().getColor(android.R.color.white));
                            break;
                        case 2:
                            fg.setTint(mAccentColor);
                            break;
                        case 3:
                            bg.setTint(mAccentColor);
                            break;
                        case 4:
                            bg.setTint(mAccentColor);
                            fg.setTint(getResources().getColor(android.R.color.white));
                            break;
                        case 5:
                            fg.setTint(mNormalColor);
                            bg.setTint(0);
                            break;
                        case 6:
                            fg.setTint(mAccentColor);
                            bg.setTint(0);
                            break;
                    }
                }
            }
        }
    }

    private void updateColtSummary() {
        Preference coltenigma = findPreference(KEY_COLTENIGMA);
        if (coltenigma != null) {
            String[] summaries = getContext().getResources().getStringArray(
                    R.array.coltenigma_summaries);
            Random rnd = new Random();
            int summNO = rnd.nextInt(summaries.length);
            coltenigma.setSummary(summaries[summNO]);
        }
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.top_level_settings) {

                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    // Never searchable, all entries in this page are already indexed elsewhere.
                    return false;
                }
            };
}
