/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.droidlogic.tv.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.support.v14.preference.PreferenceFragment;
import android.support.v17.preference.LeanbackSettingsFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.util.AttributeSet;
public class CryptKeeperSettings extends LeanbackSettingsFragment {
    private static final String TAG = "CryptKeeper";
    private static final String TYPE = "type";
    private static final String PASSWORD = "password";

    private static final int KEYGUARD_REQUEST = 55;

    private View mContentView;
    private Button mInitiateButton;
    private ViewGroup parentView;
    private View.OnClickListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup  root= (ViewGroup)super.onCreateView(inflater, container, savedInstanceState);
        parentView = root.findViewById(R.id.settings_preference_fragment_container);
        parentView.setBackgroundColor(R.color.quiet);
        mContentView = inflater.inflate(R.layout.crypt_keeper_settings, parentView);
        mInitiateButton = (Button) mContentView.findViewById(R.id.initiate_encrypt);
        mInitiateButton.setOnClickListener(listener);
        return root;
    }

    public void setOnClickListener(View.OnClickListener l){
        listener = l;
    }


    @Override
    public void onPreferenceStartInitialScreen() {

    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        return false;
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragment caller, PreferenceScreen pref) {
        return false;
    }


}
