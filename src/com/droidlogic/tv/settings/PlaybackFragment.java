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
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.TwoStatePreference;

import com.droidlogic.app.PlayBackManager;
import com.droidlogic.tv.settings.R;

import android.util.Log;

public class PlaybackFragment extends LeanbackPreferenceFragment {
	private static final String TAG = "PlaybackFragment";
	private static final String KEY_PLAYBACK_HDMI_SELFADAPTION = "playback_hdmi_selfadaption";

	private PlayBackManager mPlayBackManager;

	private TwoStatePreference mHdmiSelfAdaptionSwitchPref;

	public static PlaybackFragment newInstance() {
		return new PlaybackFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPlayBackManager = new PlayBackManager(getContext());
	}

	@Override
	public void onResume() {
		super.onResume();
		refresh();
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.playback_settings, null);

		mHdmiSelfAdaptionSwitchPref = (TwoStatePreference) findPreference(KEY_PLAYBACK_HDMI_SELFADAPTION);
	}

	@Override
	public boolean onPreferenceTreeClick(Preference preference) {
		final String key = preference.getKey();
		if (key == null) {
			return super.onPreferenceTreeClick(preference);
		}
		switch (key) {
		case KEY_PLAYBACK_HDMI_SELFADAPTION:
			mPlayBackManager.setHdmiSelfadaption(mHdmiSelfAdaptionSwitchPref.isChecked());
			return true;
		}
		return super.onPreferenceTreeClick(preference);
	}

	private void refresh() {
		mHdmiSelfAdaptionSwitchPref.setChecked(mPlayBackManager.isHdmiSelfadaptionOn());
	}
}
