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
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.ListPreference;
import android.os.SystemProperties;
import android.util.Log;
import android.text.TextUtils;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.droidlogic.tv.settings.util.DroidUtils;
import com.droidlogic.tv.settings.SettingsConstant;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.TvSettingsActivity;

import com.droidlogic.app.tv.TvControlManager;

public class DtsSoundSettingFragment extends LeanbackPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "DtsSoundSettingFragment";

    private static final String TV_SURROUND = "tv_surround";
    private static final String TV_DIALOG_CLARITY = "tv_dialog_clarity";
    private static final String TV_BASS_BOOST = "tv_bass_boost";

    private SoundEffectSettingManager mSoundEffectSettingManager;
    private SoundParameterSettingManager mSoundParameterSettingManager;
    private boolean isSeekBarInited = false;

    public static DtsSoundSettingFragment newInstance() {
        return new DtsSoundSettingFragment();
    }

    private boolean CanDebug() {
        return TvOptionFragment.CanDebug();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.tv_sound_dts_setting, null);
        if (mSoundEffectSettingManager == null) {
            mSoundEffectSettingManager = ((TvSettingsActivity)getActivity()).getSoundEffectSettingManager();
        }
        if (mSoundParameterSettingManager == null) {
            mSoundParameterSettingManager = new SoundParameterSettingManager(getActivity());
        }
        if (mSoundEffectSettingManager == null) {
            Log.e(TAG, "onCreatePreferences mSoundEffectSettingManager == null");
            return;
        }
        final ListPreference surround = (ListPreference) findPreference(TV_SURROUND);
        surround.setValueIndex(mSoundEffectSettingManager.getSurroundStatus());
        surround.setOnPreferenceChangeListener(this);
        final ListPreference dialogclarity = (ListPreference) findPreference(TV_DIALOG_CLARITY);
        dialogclarity.setValueIndex(mSoundEffectSettingManager.getDialogClarityStatus());
        dialogclarity.setOnPreferenceChangeListener(this);
        final ListPreference bassboost = (ListPreference) findPreference(TV_BASS_BOOST);
        bassboost.setValueIndex(mSoundEffectSettingManager.getBassBoostStatus());
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
        if (TextUtils.equals(preference.getKey(), TV_SURROUND)) {
            mSoundEffectSettingManager.setSurround(selection);
        } else if (TextUtils.equals(preference.getKey(), TV_DIALOG_CLARITY)) {
            mSoundEffectSettingManager.setDialogClarity(selection);
        } else if (TextUtils.equals(preference.getKey(), TV_BASS_BOOST)) {
            mSoundEffectSettingManager.setBassBoost(selection);
        }
        return true;
    }
}
