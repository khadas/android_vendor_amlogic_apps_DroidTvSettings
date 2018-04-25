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
import android.app.ActivityManager;
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
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.os.Bundle;
import android.os.Binder;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.RemoteException;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;

import com.droidlogic.tv.settings.util.DroidUtils;
import com.droidlogic.tv.settings.SettingsConstant;

import com.droidlogic.app.tv.TvControlManager;
import com.droidlogic.app.tv.DroidLogicTvUtils;

import java.util.ArrayList;
import java.util.Set;

public class MainFragment extends LeanbackPreferenceFragment {
    private static final String TAG = "MainFragment";

    private static final String KEY_MAIN_MENU = "droidsettings";
    private static final String KEY_DISPLAY = "display";
    private static final String KEY_MBOX_SOUNDS = "mbox_sound";
    private static final String KEY_POWERKEY = "powerkey_action";
    private static final String MORE_SETTINGS_APP_PACKAGE = "com.android.settings";
    private static final String KEY_UPGRADE_BLUTOOTH_REMOTE = "upgrade_bluetooth_remote";
    private static final String KEY_HDMICEC = "hdmicec";
    private static final String KEY_PLAYBACK_SETTINGS = "playback_settings";
    private static final String KEY_SOUNDS = "sound_effects";
    private static final String KEY_NETFLIX_ESN = "netflix_esn";
    private static final String KEY_MORE_SETTINGS = "more";
    private static final String KEY_ENCRYPT_MBX = "encrypt";
    private static final String KEY_PICTURE = "pictrue_mode";
    private static final String KEY_TV_OPTION = "tv_option";
    private static final String KEY_TV_CHANNEL = "channel";
    private static final String KEY_TV_SETTINGS = "tv_settings";
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
        boolean is_from_live_tv = getActivity().getIntent().getIntExtra("from_live_tv", 0) == 1;
        mTvUiMode = DroidUtils.hasTvUiMode();
        //tvFlag, is true when TV and T962E as TV, false when Mbox and T962E as Mbox.
        boolean tvFlag = SettingsConstant.needDroidlogicTvFeature(getContext())
                && (SystemProperties.getBoolean("ro.tvsoc.as.mbox", false) == false);

        final Preference mainPref = findPreference(KEY_MAIN_MENU);
        final Preference displayPref = findPreference(KEY_DISPLAY);
        final Preference hdmicecPref = findPreference(KEY_HDMICEC);
        final Preference playbackPref = findPreference(KEY_PLAYBACK_SETTINGS);
        mSoundsPref = findPreference(KEY_SOUNDS);
        final Preference mboxSoundsPref = findPreference(KEY_MBOX_SOUNDS);
        final Preference powerKeyPref = findPreference(KEY_POWERKEY);
        //BluetoothRemote/HDMI cec/Playback Settings display only in Mbox
        mUpgradeBluetoothRemote = findPreference(KEY_UPGRADE_BLUTOOTH_REMOTE);
        final Preference netflixesnPref = findPreference(KEY_NETFLIX_ESN);

        mUpgradeBluetoothRemote.setVisible(is_from_live_tv ? false : (SettingsConstant.needDroidlogicBluetoothRemoteFeature(getContext()) && !tvFlag));
        hdmicecPref.setVisible(is_from_live_tv ? false : (getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_HDMI_CEC)
                    && SettingsConstant.needDroidlogicHdmicecFeature(getContext()) && !tvFlag));
        playbackPref.setVisible(is_from_live_tv ? false : (SettingsConstant.needDroidlogicPlaybackSetFeature(getContext()) && !tvFlag));
        if (netflixesnPref != null) {
            if (is_from_live_tv) {
                netflixesnPref.setVisible(false);
            } else if (getContext().getPackageManager().hasSystemFeature("droidlogic.software.netflix")) {
                netflixesnPref.setVisible(true);
                netflixesnPref.setSummary(mEsnText);
            } else {
                netflixesnPref.setVisible(false);
            }
        }

        final Preference moreSettingsPref = findPreference(KEY_MORE_SETTINGS);
        final Preference securePref = findPreference(KEY_ENCRYPT_MBX);
        final String state = SystemProperties.get("vold.decrypt");
        final String useFilecrypto = SystemProperties.get("ro.crypto.type");
        if (is_from_live_tv) {
            securePref.setVisible(false);
            moreSettingsPref.setVisible(false);
         } else if (!isPackageInstalled(getActivity(), MORE_SETTINGS_APP_PACKAGE)) {
            getPreferenceScreen().removePreference(moreSettingsPref);
            if (useFilecrypto.equals("file")) {
                getPreferenceScreen().removePreference(securePref);
            }else if (getCurrentUserId() != UserHandle.USER_SYSTEM) {
                getPreferenceScreen().removePreference(securePref);
            }else if (CryptKeeper.DECRYPT_STATE.equals(state)) {
                securePref.setSummary(getString(R.string.crypt_keeper_encrypted_summary));
                securePref.setEnabled(false);
            }
        } else {
            getPreferenceScreen().removePreference(securePref);
        }

        final Preference picturePref = findPreference(KEY_PICTURE);
        final Preference mTvOption = findPreference(KEY_TV_OPTION);
        final Preference channelPref = findPreference(KEY_TV_CHANNEL);
        final Preference settingsPref = findPreference(KEY_TV_SETTINGS);

        if (is_from_live_tv) {
            mainPref.setTitle(R.string.settings_menu);
            displayPref.setVisible(false);
            mboxSoundsPref.setVisible(false);
            powerKeyPref.setVisible(false);
            mTvOption.setVisible(false);
            moreSettingsPref.setVisible(false);
            TvControlManager tvControlManager = TvControlManager.getInstance();
            int sourceinputtype = tvControlManager.GetCurrentSourceInput();
            if (sourceinputtype != DroidLogicTvUtils.DEVICE_ID_ADTV
                && sourceinputtype != DroidLogicTvUtils.DEVICE_ID_ATV
                && sourceinputtype != DroidLogicTvUtils.DEVICE_ID_DTV) {
                if (sourceinputtype == -1) {
                    channelPref.setVisible(true);
                } else {
                    channelPref.setVisible(false);
                }
            } else {
                channelPref.setVisible(true);
            }
        } else {
            mSoundsPref.setVisible(false);
            channelPref.setVisible(false);
            settingsPref.setVisible(false);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        super.onPreferenceTreeClick(preference);
        if (TextUtils.equals(preference.getKey(), KEY_TV_CHANNEL)) {
            startUiInLiveTv(KEY_TV_CHANNEL);
        }
        return false;
    }

    private void startUiInLiveTv(String value) {
        Intent intent = new Intent();
        intent.setAction("action.startlivetv.settingui");
        intent.putExtra(value, true);
        getActivity().sendBroadcast(intent);
        getActivity().finish();
    }

    private int getCurrentUserId() {
        final long ident = Binder.clearCallingIdentity();
        try {
            UserInfo currentUser = ActivityManager.getService().getCurrentUser();
            return currentUser.id;
        } catch (RemoteException e) {
            // Activity manager not running, nothing we can do assume user 0.
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
        return UserHandle.USER_SYSTEM;
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

    private static boolean isPackageInstalled(Context context, String packageName) {
        try {
            return context.getPackageManager().getPackageInfo(packageName, 0) != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
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
