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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;

import com.droidlogic.tv.settings.util.DroidUtils;
import com.droidlogic.tv.settings.SettingsConstant;

import java.util.ArrayList;
import java.util.Set;

public class MainFragment extends LeanbackPreferenceFragment {
    private static final String TAG = "MainFragment";

    private static final String KEY_UPGRADE_BLUTOOTH_REMOTE = "upgrade_bluetooth_remote";
    private static final String KEY_HDMICEC = "hdmicec";
    private static final String KEY_PLAYBACK_SETTINGS = "playback_settings";
    private static final String KEY_SOUNDS = "sound_effects";
    private static final String KEY_NETFLIX_ESN = "netflix_esn";
    private boolean mTvUiMode;

    private Preference mUpgradeBluetoothRemote;
    private Preference mSoundsPref;

    private String mEsnText;

    private BroadcastReceiver esnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mEsnText = intent.getStringExtra("ESNValue");
            findPreference(KEY_NETFLIX_ESN).setSummary(mEsnText);
        }
    };

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main_prefs, null);
        mTvUiMode = DroidUtils.hasTvUiMode();

        //tvFlag, is true when TV and T962E as TV, false when Mbox and T962E as Mbox.
        boolean needTvFeature = SettingsConstant.needDroidlogicTvFeature(getContext());
        boolean tvFlag = needTvFeature
                && (SystemProperties.getBoolean("ro.tvsoc.as.mbox", false) == false);

        //BluetoothRemote/HDMI cec/Playback Settings display only in Mbox
        mUpgradeBluetoothRemote = findPreference(KEY_UPGRADE_BLUTOOTH_REMOTE);
        mUpgradeBluetoothRemote.setVisible(SettingsConstant.needDroidlogicBluetoothRemoteFeature(getContext()) && !tvFlag);

        final Preference hdmicecPref = findPreference(KEY_HDMICEC);
        hdmicecPref.setVisible(getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_HDMI_CEC)
                    && SettingsConstant.needDroidlogicHdmicecFeature(getContext()) && !tvFlag);

        final Preference playbackPref = findPreference(KEY_PLAYBACK_SETTINGS);
        playbackPref.setVisible(SettingsConstant.needDroidlogicPlaybackSetFeature(getContext()) && !tvFlag);

        mSoundsPref = findPreference(KEY_SOUNDS);

        final Preference netflixesnPref = findPreference(KEY_NETFLIX_ESN);
        if (netflixesnPref != null) {
            if (getContext().getPackageManager().hasSystemFeature("droidlogic.software.netflix")) {
                netflixesnPref.setVisible(true);
                netflixesnPref.setSummary(mEsnText);
            } else {
                netflixesnPref.setVisible(false);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSounds();
        IntentFilter esnIntentFilter = new IntentFilter("com.netflix.ninja.intent.action.ESN_RESPONSE");
        getActivity().getApplicationContext().registerReceiver(esnReceiver, esnIntentFilter,
                "com.netflix.ninja.permission.ESN", null);
        Intent esnQueryIntent = new Intent("com.netflix.ninja.intent.action.ESN");
        esnQueryIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        getActivity().getApplicationContext().sendBroadcast(esnQueryIntent);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void updateSounds() {
        if (mSoundsPref == null) {
            return;
        }

        mSoundsPref.setIcon(SoundFragment.getSoundEffectsEnabled(getContext().getContentResolver())
                ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);
    }

    private void hideIfIntentUnhandled(Preference preference) {
        if (preference == null) {
            return;
        }
        preference.setVisible(systemIntentIsHandled(preference.getIntent()) != null);
    }

    private ResolveInfo systemIntentIsHandled(Intent intent) {
        if (intent == null) {
            return null;
        }

        final PackageManager pm = getContext().getPackageManager();

        for (ResolveInfo info : pm.queryIntentActivities(intent, 0)) {
            if (info.activityInfo != null && info.activityInfo.enabled && (info.activityInfo.applicationInfo.flags
                    & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                return info;
            }
        }
        return null;
    }
}
