/*
 * Copyright (C) 2015 The Android Open Source Project
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
 * limitations under the License
 */

package com.droidlogic.tv.settings.tvoption;

import android.os.Bundle;
import android.os.Handler;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.util.Log;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import android.os.SystemProperties;
import com.droidlogic.tv.settings.util.DroidUtils;
import com.droidlogic.tv.settings.SettingsConstant;
import com.droidlogic.tv.settings.MainFragment;
import com.droidlogic.tv.settings.R;

public class SoundModeFragment extends LeanbackPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "SoundModeFragment";

    private static final String TV_SOUND_MODE = "tv_sound_mode";
    private static final String TV_MULTI_SETTINGS = "multi_settings";
    private static final String TV_TREBLE = "tv_treble";
    private static final String TV_BASS = "tv_bass";
    private static final String TV_BALANCE = "tv_balance";
    private static final String TV_SPDIF = "tv_spdif";
    private static final String TV_VIRTUAL_SURROUND = "tv_virtual_surround";
    private static final String TV_SURROUND = "tv_surround";
    private static final String TV_DIALOG_CLARITY = "tv_dialog_clarity";
    private static final String TV_BASS_BOOST = "tv_bass_boost";

    private TvOptionSettingManager mTvOptionSettingManager;

    public static SoundModeFragment newInstance() {
        return new SoundModeFragment();
    }

    private boolean CanDebug() {
        return TvOptionFragment.CanDebug();
    }

    @Override
    public void onResume() {
        super.onResume();
        final ListPreference soundmode = (ListPreference) findPreference(TV_SOUND_MODE);
        soundmode.setValueIndex(mTvOptionSettingManager.getSoundModeStatus());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        final View innerView = super.onCreateView(inflater, container, savedInstanceState);
        if (getActivity().getIntent().getIntExtra("from_live_tv", 0) == 1) {
            //MainFragment.changeToLiveTvStyle(innerView, getActivity());
        }
        return innerView;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.tv_sound_mode, null);
        if (mTvOptionSettingManager == null) {
            mTvOptionSettingManager = new TvOptionSettingManager(getActivity());
        }
        final ListPreference soundmode = (ListPreference) findPreference(TV_SOUND_MODE);
        //soundmode.setDialogLayoutResource(int dialogLayoutResId)
        soundmode.setValueIndex(mTvOptionSettingManager.getSoundModeStatus());
        soundmode.setOnPreferenceChangeListener(this);
        final ListPreference spdifmode = (ListPreference) findPreference(TV_SPDIF);
        spdifmode.setValueIndex(mTvOptionSettingManager.getSpdifStatus());
        spdifmode.setOnPreferenceChangeListener(this);
        final ListPreference virtualsurround = (ListPreference) findPreference(TV_VIRTUAL_SURROUND);
        virtualsurround.setValueIndex(mTvOptionSettingManager.getVirtualSurroundStatus());
        virtualsurround.setOnPreferenceChangeListener(this);
        final ListPreference surround = (ListPreference) findPreference(TV_SURROUND);
        surround.setValueIndex(mTvOptionSettingManager.getSurroundStatus());
        surround.setOnPreferenceChangeListener(this);
        final ListPreference dialogclarity = (ListPreference) findPreference(TV_DIALOG_CLARITY);
        dialogclarity.setValueIndex(mTvOptionSettingManager.getDialogClarityStatus());
        dialogclarity.setOnPreferenceChangeListener(this);
        final ListPreference bassboost = (ListPreference) findPreference(TV_BASS_BOOST);
        bassboost.setValueIndex(mTvOptionSettingManager.getBassBoostStatus());
        bassboost.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (CanDebug()) Log.d(TAG, "[onPreferenceTreeClick] preference.getKey() = " + preference.getKey());
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (CanDebug()) Log.d(TAG, "[onPreferenceChange] preference.getKey() = " + preference.getKey() + ", newValue = " + newValue);
        final int selection = Integer.parseInt((String)newValue);
        if (TextUtils.equals(preference.getKey(), TV_SOUND_MODE)) {
            mTvOptionSettingManager.setSoundMode(selection);
        } else if (TextUtils.equals(preference.getKey(), TV_SPDIF)) {
            mTvOptionSettingManager.setSpdif(selection);
        } else if (TextUtils.equals(preference.getKey(), TV_VIRTUAL_SURROUND)) {
            mTvOptionSettingManager.setVirtualSurround(selection);
        } else if (TextUtils.equals(preference.getKey(), TV_SURROUND)) {
            mTvOptionSettingManager.setSurround(selection);
        } else if (TextUtils.equals(preference.getKey(), TV_DIALOG_CLARITY)) {
            mTvOptionSettingManager.setDialogClarity(selection);
        } else if (TextUtils.equals(preference.getKey(), TV_BASS_BOOST)) {
            mTvOptionSettingManager.setBassBoost(selection);
        }
        return true;
    }
}
