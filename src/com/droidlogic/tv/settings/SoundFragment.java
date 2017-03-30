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
import android.media.AudioManager;
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

import com.droidlogic.app.OutputModeManager;

import com.droidlogic.tv.settings.R;
import android.util.Log;

public class SoundFragment extends LeanbackPreferenceFragment implements Preference.OnPreferenceChangeListener {
	public static final String LINE = "2";
	public static final String RF = "3";
	public static final String TAG = "SoundFragment";
	private static final String KEY_SOUND_EFFECTS = "sound_effects";
	private static final String KEY_SURROUND_PASSTHROUGH = "surround_passthrough";
	private static final String KEY_DRCMODE_PASSTHROUGH = "drc_mode";
	private static final String KEY_DIGITALSOUND_PASSTHROUGH = "digital_sound";
	private static final String KEY_DTSDRCMODE_PASSTHROUGH = "dtsdrc_mode";
	private static final String KEY_DTSDRCCUSTOMMODE_PASSTHROUGH = "dtsdrc_custom_mode";

	private static final String VAL_SURROUND_SOUND_AUTO = "auto";
	private static final String VAL_SURROUND_SOUND_ALWAYS = "always";
	private static final String VAL_SURROUND_SOUND_NEVER = "never";
	public static final String DRC_MODE = "dolbydrc";
	public static final String DRC_OFF = "off";
	public static final String DRC_LINE = "line";
	public static final String DRC_RF = "rf";
	public static final String AUTO = "auto";
	public static final String PCM = "pcm";
	public static final String HDMI = "hdmi";
	public static final String SPDIF = "spdif";
	public static final String DTSDRC_SCALE_DEFAULT = "0";

	private OutputModeManager mOMM;

	private AudioManager mAudioManager;

	public static SoundFragment newInstance() {
		return new SoundFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
		mOMM = new OutputModeManager(getActivity());
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.sound, null);

		final TwoStatePreference soundPref = (TwoStatePreference) findPreference(KEY_SOUND_EFFECTS);
		soundPref.setChecked(getSoundEffectsEnabled());

		final ListPreference surroundPref = (ListPreference) findPreference(KEY_SURROUND_PASSTHROUGH);
		final ListPreference drcmodePref = (ListPreference) findPreference(KEY_DRCMODE_PASSTHROUGH);
		final ListPreference digitalsoundPref = (ListPreference) findPreference(KEY_DIGITALSOUND_PASSTHROUGH);
		final ListPreference dtsdrccustommodePref = (ListPreference) findPreference(KEY_DTSDRCCUSTOMMODE_PASSTHROUGH);
		final ListPreference dtsdrcmodePref = (ListPreference) findPreference(KEY_DTSDRCMODE_PASSTHROUGH);

		surroundPref.setValue(getSurroundPassthroughSetting());
		surroundPref.setOnPreferenceChangeListener(this);
		drcmodePref.setValue(getDrcModePassthroughSetting());
		drcmodePref.setOnPreferenceChangeListener(this);
		digitalsoundPref.setValue(getDigitalSoundPassthroughSetting());
		digitalsoundPref.setOnPreferenceChangeListener(this);
		dtsdrcmodePref.setValue(getDtsDrcModePassthroughSetting());
		dtsdrcmodePref.setOnPreferenceChangeListener(this);
		if (SystemProperties.getBoolean("ro.platform.has.tvuimode", false)) {
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
		if (TextUtils.equals(preference.getKey(), KEY_SOUND_EFFECTS)) {
			final TwoStatePreference soundPref = (TwoStatePreference) preference;
			setSoundEffectsEnabled(soundPref.isChecked());
		}
		return super.onPreferenceTreeClick(preference);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (TextUtils.equals(preference.getKey(), KEY_SURROUND_PASSTHROUGH)) {
			final String selection = (String) newValue;
			switch (selection) {
			case VAL_SURROUND_SOUND_AUTO:
				setSurroundPassthroughSetting(Settings.Global.ENCODED_SURROUND_OUTPUT_AUTO);
				break;
			case VAL_SURROUND_SOUND_ALWAYS:
				setSurroundPassthroughSetting(Settings.Global.ENCODED_SURROUND_OUTPUT_ALWAYS);
				break;
			case VAL_SURROUND_SOUND_NEVER:
				setSurroundPassthroughSetting(Settings.Global.ENCODED_SURROUND_OUTPUT_NEVER);
				break;
			default:
				throw new IllegalArgumentException("Unknown surround sound pref value");
			}
			return true;
		} else if (TextUtils.equals(preference.getKey(), KEY_DRCMODE_PASSTHROUGH)) {
			final String selection = (String) newValue;
			switch (selection) {
			case DRC_OFF:
				mOMM.enableDobly_DRC(false);
				setDrcModePassthroughSetting(0);
				break;
			case DRC_LINE:
				mOMM.enableDobly_DRC(true);
				mOMM.setDoblyMode(LINE);
				setDrcModePassthroughSetting(1);
				break;
			case DRC_RF:
				mOMM.setDoblyMode(RF);
				setDrcModePassthroughSetting(2);
				break;
			default:
				throw new IllegalArgumentException("Unknown drc mode pref value");
			}
			return true;
		} else if (TextUtils.equals(preference.getKey(), KEY_DIGITALSOUND_PASSTHROUGH)) {
			final String selection = (String) newValue;
			switch (selection) {
			case AUTO:
				autoSwitchDigitalSound();
				setDigitalSoundPassthroughSetting(0);
				break;
			case PCM:
				setDigitalSoundMode(OutputModeManager.PCM);
				setDigitalSoundPassthroughSetting(1);
				break;
			case HDMI:
				setDigitalSoundMode(OutputModeManager.HDMI_RAW);
				setDigitalSoundPassthroughSetting(2);
				break;
			case SPDIF:
				setDigitalSoundMode(OutputModeManager.SPDIF_RAW);
				setDigitalSoundPassthroughSetting(3);
				break;
			default:
				throw new IllegalArgumentException("Unknown digital sound pref value");
			}
			return true;
		} else if (TextUtils.equals(preference.getKey(), KEY_DTSDRCMODE_PASSTHROUGH)) {
			final String selection = (String) newValue;
			mOMM.setDtsDrcScale(selection);
			setDtsDrcModePassthroughSetting(selection);
			return true;
		}
		return true;
	}

	private int autoSwitchDigitalSound() {
		return mOMM.autoSwitchHdmiPassthough();
	}

	private void setDigitalSoundMode(String mode) {
		mOMM.setDigitalMode(mode);
	}

	private boolean getSoundEffectsEnabled() {
		return getSoundEffectsEnabled(getActivity().getContentResolver());
	}

	public static boolean getSoundEffectsEnabled(ContentResolver contentResolver) {
		return Settings.System.getInt(contentResolver, Settings.System.SOUND_EFFECTS_ENABLED, 1) != 0;
	}

	private void setSoundEffectsEnabled(boolean enabled) {
		if (enabled) {
			mAudioManager.loadSoundEffects();
		} else {
			mAudioManager.unloadSoundEffects();
		}
		Settings.System.putInt(getActivity().getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED,
				enabled ? 1 : 0);
	}

	private void setSurroundPassthroughSetting(int newVal) {
		Settings.Global.putInt(getContext().getContentResolver(), Settings.Global.ENCODED_SURROUND_OUTPUT, newVal);
	}

	private void setDrcModePassthroughSetting(int newVal) {
		Settings.Global.putInt(getContext().getContentResolver(), "drc_mode", newVal);
	}

	private void setDigitalSoundPassthroughSetting(int newVal) {
		Settings.Global.putInt(getContext().getContentResolver(), "digital_sound", newVal);
	}

	private void setDtsDrcModePassthroughSetting(String newVal) {
		Settings.Global.putString(getContext().getContentResolver(), "dtsdrc_mode", newVal);
	}

	private String getSurroundPassthroughSetting() {
		final int value = Settings.Global.getInt(getContext().getContentResolver(),
				Settings.Global.ENCODED_SURROUND_OUTPUT, Settings.Global.ENCODED_SURROUND_OUTPUT_AUTO);

		switch (value) {
		case Settings.Global.ENCODED_SURROUND_OUTPUT_AUTO:
		default:
			return VAL_SURROUND_SOUND_AUTO;
		case Settings.Global.ENCODED_SURROUND_OUTPUT_ALWAYS:
			return VAL_SURROUND_SOUND_ALWAYS;
		case Settings.Global.ENCODED_SURROUND_OUTPUT_NEVER:
			return VAL_SURROUND_SOUND_NEVER;
		}
	}

	private String getDrcModePassthroughSetting() {
		final int value = Settings.Global.getInt(getContext().getContentResolver(), "drc_mode", 0);

		switch (value) {
		case 0:
		default:
			return DRC_OFF;
		case 1:
			return DRC_LINE;
		case 2:
			return DRC_RF;
		}
	}

	private String getDigitalSoundPassthroughSetting() {
		final int value = Settings.Global.getInt(getContext().getContentResolver(), "digital_sound", 0);

		switch (value) {
		case 0:
		default:
			return AUTO;
		case 1:
			return PCM;
		case 2:
			return HDMI;
		case 3:
			return SPDIF;
		}
	}

	private String getDtsDrcModePassthroughSetting() {
		String dtsdrc_mode_value = Settings.Global.getString(getContext().getContentResolver(), "dtsdrc_mode");
		return dtsdrc_mode_value == null ? DTSDRC_SCALE_DEFAULT : dtsdrc_mode_value;
	}
}
