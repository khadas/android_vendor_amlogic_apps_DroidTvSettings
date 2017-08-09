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

package com.droidlogic.tv.settings.display.outputmode;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.text.format.DateFormat;

import android.util.Log;
import com.droidlogic.tv.settings.R;



public class ScreenResolutionFragment extends LeanbackPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_DEEPCOLOR = "deepcolor_setting";
    private static final String KEY_DISPLAYMODE = "displaymode_setting";
    private static final String KEY_BEST_RESOLUTION = "best_resolution";
    private static final String DEFAULT_VALUE = "444,8bit";

    private Preference mBestResolutionPref;
    private Preference mDisplayModePref;
    private Preference mDeepColorPref;
    private OutputUiManager mOutputUiManager;
    private IntentFilter mIntentFilter;
    public boolean hpdFlag = false;
    private static final int MSG_FRESH_UI = 0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FRESH_UI:
                    updateScreenResolutionDisplay();
                    break;
            }
        }
    };
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            hpdFlag = intent.getBooleanExtra ("state", false);
            mHandler.sendEmptyMessageDelayed(MSG_FRESH_UI, hpdFlag ^ isHdmiMode() ? 1000 : 0);
        }
    };

    public static ScreenResolutionFragment newInstance() {
        return new ScreenResolutionFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOutputUiManager = new OutputUiManager(getActivity());
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.screen_resolution, null);

        mBestResolutionPref = findPreference(KEY_BEST_RESOLUTION);
        mBestResolutionPref.setOnPreferenceChangeListener(this);
        mDisplayModePref = findPreference(KEY_DISPLAYMODE);
        mDeepColorPref = findPreference(KEY_DEEPCOLOR);
        mIntentFilter = new IntentFilter("android.intent.action.HDMI_PLUGGED");
        getActivity().registerReceiver(mIntentReceiver, mIntentFilter);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateScreenResolutionDisplay();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mIntentReceiver);
    }
    @Override
    public void onPause() {
        super.onPause();
    }

    private void updateScreenResolutionDisplay() {
        mOutputUiManager.updateUiMode();
        ((SwitchPreference)mBestResolutionPref).setChecked(isBestResolution());
        if (isBestResolution()) {
           mBestResolutionPref.setSummary(R.string.captions_display_on);
        }else {
           mBestResolutionPref.setSummary(R.string.captions_display_off);
        }
        mDisplayModePref.setSummary(getCurrentDisplayMode());
        if (isHdmiMode()) {
            mBestResolutionPref.setVisible(true);
            mDeepColorPref.setVisible(true);
            mDeepColorPref.setSummary(getCurrentDeepColor());
        } else {
            mBestResolutionPref.setVisible(false);
            mDeepColorPref.setVisible(false);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (TextUtils.equals(preference.getKey(), KEY_BEST_RESOLUTION)) {
            setBestResolution();
            updateScreenResolutionDisplay();
        }
        return true;
    }
    private boolean isBestResolution() {
        return mOutputUiManager.isBestOutputmode();
    }
    private void setBestResolution() {
        mOutputUiManager.change2BestMode();
    }
    private String getCurrentDisplayMode() {
        return mOutputUiManager.getCurrentMode().trim();
    }
    private String getCurrentDeepColor() {
        String value = mOutputUiManager.getCurrentColorAttribute().toString().trim();
        if (value.equals("default") || value == "" || value.equals(""))
            return DEFAULT_VALUE;
        return value;
    }
    private boolean isHdmiMode() {
        return mOutputUiManager.isHdmiMode();
    }
}
