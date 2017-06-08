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

package com.droidlogic.tv.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioSystem;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import android.text.TextUtils;
import android.util.Log;

import com.droidlogic.app.OutputModeManager;
import com.droidlogic.tv.settings.SettingsConstant;
import com.droidlogic.tv.settings.R;

public class SoundFragment extends LeanbackPreferenceFragment implements Preference.OnPreferenceChangeListener {
    public static final String TAG = "SoundFragment";
    private static final String KEY_DRCMODE_PASSTHROUGH = "drc_mode";
    private static final String KEY_DIGITALSOUND_PASSTHROUGH = "digital_sound";
    private static final String KEY_DTSDRCMODE_PASSTHROUGH = "dtsdrc_mode";
    private static final String KEY_DTSDRCCUSTOMMODE_PASSTHROUGH = "dtsdrc_custom_mode";

    public static final String DRC_OFF = "off";
    public static final String DRC_LINE = "line";
    public static final String DRC_RF = "rf";
    public static final String PCM = "pcm";
    public static final String HDMI = "hdmi";
    public static final String SPDIF = "spdif";

    private OutputModeManager mOutputModeManager;

    public static SoundFragment newInstance() {
        return new SoundFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOutputModeManager = new OutputModeManager(getActivity());
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.sound, null);

        final ListPreference drcmodePref = (ListPreference) findPreference(KEY_DRCMODE_PASSTHROUGH);
        final ListPreference digitalsoundPref = (ListPreference) findPreference(KEY_DIGITALSOUND_PASSTHROUGH);
        final ListPreference dtsdrccustommodePref = (ListPreference) findPreference(KEY_DTSDRCCUSTOMMODE_PASSTHROUGH);
        final ListPreference dtsdrcmodePref = (ListPreference) findPreference(KEY_DTSDRCMODE_PASSTHROUGH);

        drcmodePref.setValue(getDrcModePassthroughSetting());
        drcmodePref.setOnPreferenceChangeListener(this);
        digitalsoundPref.setValue(getDigitalSoundPassthroughSetting());
        digitalsoundPref.setOnPreferenceChangeListener(this);
        dtsdrcmodePref.setValue(SystemProperties.get("persist.sys.dtsdrcscale", OutputModeManager.DEFAULT_DRC_SCALE));
        dtsdrcmodePref.setOnPreferenceChangeListener(this);
        if (!SettingsConstant.needDroidlogicDigitalSounds(getContext())) {
            digitalsoundPref.setVisible(false);
            Log.d(TAG, "tv don't need digital sound switch!");
        }
        if (!SystemProperties.getBoolean("ro.platform.support.dolby", false)) {
            drcmodePref.setVisible(false);
            Log.d(TAG, "platform doesn't support dolby");
        }
        if (!SystemProperties.getBoolean("ro.platform.support.dts", false)) {
            dtsdrcmodePref.setVisible(false);
            dtsdrccustommodePref.setVisible(false);
            Log.d(TAG, "platform doesn't support dts");
        } else if (SystemProperties.getBoolean("persist.sys.dtsdrccustom", false)) {
            dtsdrcmodePref.setVisible(false);
        } else {
            dtsdrccustommodePref.setVisible(false);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (TextUtils.equals(preference.getKey(), KEY_DRCMODE_PASSTHROUGH)) {
            final String selection = (String) newValue;
            switch (selection) {
            case DRC_OFF:
                mOutputModeManager.enableDobly_DRC(false);
                mOutputModeManager.setDoblyMode(OutputModeManager.LINE_DRCMODE);
                setDrcModePassthroughSetting(OutputModeManager.IS_DRC_OFF);
                break;
            case DRC_LINE:
                mOutputModeManager.enableDobly_DRC(true);
                mOutputModeManager.setDoblyMode(OutputModeManager.LINE_DRCMODE);
                setDrcModePassthroughSetting(OutputModeManager.IS_DRC_LINE);
                break;
            case DRC_RF:
                mOutputModeManager.enableDobly_DRC(false);
                mOutputModeManager.setDoblyMode(OutputModeManager.RF_DRCMODE);
                setDrcModePassthroughSetting(OutputModeManager.IS_DRC_RF);
                break;
            default:
                throw new IllegalArgumentException("Unknown drc mode pref value");
            }
            return true;
        } else if (TextUtils.equals(preference.getKey(), KEY_DIGITALSOUND_PASSTHROUGH)) {
            final String selection = (String) newValue;
            switch (selection) {
            case PCM:
                AudioSystem.setDeviceConnectionState(
                        AudioSystem.DEVICE_OUT_SPDIF,
                        AudioSystem.DEVICE_STATE_UNAVAILABLE,
                        "Amlogic", "Amlogic-S/PDIF");
                setDigitalSoundMode(OutputModeManager.PCM);
                setDigitalSoundPassthroughSetting(OutputModeManager.IS_PCM);
                break;
            case SPDIF:
                AudioSystem.setDeviceConnectionState(
                        AudioSystem.DEVICE_OUT_SPDIF,
                        AudioSystem.DEVICE_STATE_AVAILABLE,
                        "Amlogic", "Amlogic-S/PDIF");
                setDigitalSoundMode(OutputModeManager.SPDIF_RAW);
                setDigitalSoundPassthroughSetting(OutputModeManager.IS_SPDIF_RAW);
                break;
            case HDMI:
                AudioSystem.setDeviceConnectionState(
                        AudioSystem.DEVICE_OUT_SPDIF,
                        AudioSystem.DEVICE_STATE_UNAVAILABLE,
                        "Amlogic", "Amlogic-S/PDIF");
                autoSwitchDigitalSound();
                setDigitalSoundPassthroughSetting(OutputModeManager.IS_HDMI_RAW);
                break;
            default:
                throw new IllegalArgumentException("Unknown digital sound pref value");
            }
            return true;
        } else if (TextUtils.equals(preference.getKey(), KEY_DTSDRCMODE_PASSTHROUGH)) {
            final String selection = (String) newValue;
            mOutputModeManager.setDtsDrcScale(selection);
            return true;
        }
        return true;
    }

    private int autoSwitchDigitalSound() {
        return mOutputModeManager.autoSwitchHdmiPassthough();
    }

    private void setDigitalSoundMode(String mode) {
        mOutputModeManager.setDigitalMode(mode);
    }

    private void setDrcModePassthroughSetting(int newVal) {
        Settings.Global.putInt(getContext().getContentResolver(),
                OutputModeManager.DRC_MODE, newVal);
    }

    private void setDigitalSoundPassthroughSetting(int newVal) {
        Settings.Global.putInt(getContext().getContentResolver(),
                OutputModeManager.DIGITAL_SOUND, newVal);
    }

    public static boolean getSoundEffectsEnabled(ContentResolver contentResolver) {
        return Settings.System.getInt(contentResolver, Settings.System.SOUND_EFFECTS_ENABLED, 1) != 0;
    }

    private String getDrcModePassthroughSetting() {
        final int value = Settings.Global.getInt(getContext().getContentResolver(),
                OutputModeManager.DRC_MODE, OutputModeManager.IS_DRC_LINE);

        switch (value) {
        case OutputModeManager.IS_DRC_OFF:
            return DRC_OFF;
        case OutputModeManager.IS_DRC_LINE:
        default:
            return DRC_LINE;
        case OutputModeManager.IS_DRC_RF:
            return DRC_RF;
        }
    }

    private String getDigitalSoundPassthroughSetting() {
        final int value = Settings.Global.getInt(getContext().getContentResolver(),
                OutputModeManager.DIGITAL_SOUND, OutputModeManager.IS_PCM);

        switch (value) {
        case OutputModeManager.IS_PCM:
        default:
            return PCM;
        case OutputModeManager.IS_SPDIF_RAW:
            return SPDIF;
        case OutputModeManager.IS_HDMI_RAW:
            return HDMI;
        }
    }
}
