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



public class CryptKeeperFragment extends LeanbackSettingsFragment implements View.OnClickListener {
    private static final String TAG = "CryptKeeper";
    private static final String TYPE = "type";
    private static final String PASSWORD = "password";
    private static final int KEYGUARD_REQUEST = 55;
    @Override
    public void onPreferenceStartInitialScreen() {
        CryptKeeperSettings fragment = new CryptKeeperSettings();
        fragment.setOnClickListener(this);
        startPreferenceFragment(fragment);
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        return false;
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragment caller, PreferenceScreen pref) {
        return false;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,"onActivityResult"+requestCode);
        if (requestCode != KEYGUARD_REQUEST) {
            return;
        }

        // If the user entered a valid keyguard trace, present the final
        // confirmation prompt; otherwise, go back to the initial state.
        if (resultCode == Activity.RESULT_OK && data != null) {
            int type = data.getIntExtra(ChooseLockSettingsHelper.EXTRA_KEY_TYPE, -1);
            String password = data.getStringExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD);
            if (!TextUtils.isEmpty(password)) {
                Log.d(TAG,"password"+password+"type"+type);
                showFinalConfirmation(type, password);
            }
        }
    }
    @Override
    public void onClick(View v) {
        if (!runKeyguardConfirmation(KEYGUARD_REQUEST)) {
                // TODO replace (or follow) this dialog with an explicit launch into password UI
                new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.crypt_keeper_dialog_need_password_title)
                    .setMessage(R.string.crypt_keeper_dialog_need_password_message)
                    .setPositiveButton(android.R.string.ok, null)
                    .create()
                    .show();
            }
    }

    private void showFinalConfirmation(int type, String password) {
        Bundle bund = new Bundle();
        Activity activity = getActivity();
        DevicePolicyManager dpm = (DevicePolicyManager)
                activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        bund.putInt(TYPE, type);
        bund.putString(PASSWORD, password);
        Fragment f=new CryptKeeperConfirm();
        f.setArguments(bund);
        startPreferenceFragment(f);
    }
    private boolean runKeyguardConfirmation(int request) {
        Resources res = getActivity().getResources();
        ChooseLockSettingsHelper helper = new ChooseLockSettingsHelper(getActivity(), this);

        if (helper.utils().getKeyguardStoredPasswordQuality(UserHandle.USER_SYSTEM)
                == DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED) {
            showFinalConfirmation(StorageManager.CRYPT_TYPE_DEFAULT, "");
        }
        else {
            Intent intent = new Intent();
            intent.putExtra("title",res.getText(R.string.crypt_keeper_encrypt_title));
            intent.setClass(getActivity(),ConfirmLockPassword.InternalActivity.class);
            startActivityForResult(intent,request);
        }
        return true;
    }
     @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        Intent intent = activity.getIntent();
        if (DevicePolicyManager.ACTION_START_ENCRYPTION.equals(intent.getAction())) {
            DevicePolicyManager dpm = (DevicePolicyManager)
                    activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
            if (dpm != null) {
                int status = dpm.getStorageEncryptionStatus();
                if (status != DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE) {
                    // There is nothing to do here, so simply finish() (which returns to caller)
                    activity.finish();
                }
            }
        }
    }

}