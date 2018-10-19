/*
 * Copyright (C) 2011 The Android Open Source Project
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
import android.app.ActivityManager;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.Bundle;
import android.os.storage.IStorageManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.support.v14.preference.PreferenceFragment;
import android.support.v17.preference.LeanbackSettingsFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;


import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.widget.LockPatternUtils;
import java.util.Locale;

public class CryptKeeperConfirm extends LeanbackSettingsFragment{

    private static final String TAG = "CryptKeeperConfirm";
    private Bundle arg;

    public static class Blank extends Activity {
        private Handler mHandler = new Handler();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.crypt_keeper_blank);

            if (ActivityManager.isUserAMonkey()) {
                finish();
            }

            StatusBarManager sbm = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
            sbm.disable(StatusBarManager.DISABLE_EXPAND
                    | StatusBarManager.DISABLE_NOTIFICATION_ICONS
                    | StatusBarManager.DISABLE_NOTIFICATION_ALERTS
                    | StatusBarManager.DISABLE_SYSTEM_INFO
                    | StatusBarManager.DISABLE_HOME
                    | StatusBarManager.DISABLE_SEARCH
                    | StatusBarManager.DISABLE_RECENT
                    | StatusBarManager.DISABLE_BACK);

            // Post a delayed message in 700 milliseconds to enable encryption.
            // NOTE: The animation on this activity is set for 500 milliseconds
            // I am giving it a little extra time to complete.
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    IBinder service = ServiceManager.getService("mount");
                    if (service == null) {
                        Log.e("CryptKeeper", "Failed to find the mount service");
                        finish();
                        return;
                    }

                    IStorageManager storageManager = IStorageManager.Stub.asInterface(service);
                    try {
                        Bundle args = getIntent().getExtras();
                        storageManager.encryptStorage(args.getInt("type", -1), args.getString("password"));
                    } catch (Exception e) {
                        Log.e("CryptKeeper", "Error while encrypting...", e);
                    }
                }
            }, 700);
        }
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

    private View mContentView;
    private Button mFinalButton;
    private Button.OnClickListener mFinalClickListener = new Button.OnClickListener() {

        public void onClick(View v) {
            if (ActivityManager.isUserAMonkey()) {
                return;
            }
            LockPatternUtils utils = new LockPatternUtils(getActivity());
            utils.setVisiblePatternEnabled(
                    utils.isVisiblePatternEnabled(UserHandle.USER_SYSTEM),
                    UserHandle.USER_SYSTEM);
            if (utils.isOwnerInfoEnabled(UserHandle.USER_SYSTEM)) {
                utils.setOwnerInfo(utils.getOwnerInfo(UserHandle.USER_SYSTEM),
                                   UserHandle.USER_SYSTEM);
            }
            int value = Settings.System.getInt(getContext().getContentResolver(),
                                               Settings.System.TEXT_SHOW_PASSWORD,
                                               1);
            utils.setVisiblePasswordEnabled(value != 0, UserHandle.USER_SYSTEM);

            Intent intent = new Intent(getActivity(), Blank.class);
            intent.putExtras(getArguments());
            startActivity(intent);

            // 2. The system locale.
            try {
                IBinder service = ServiceManager.getService("mount");
                IStorageManager storageManager = IStorageManager.Stub.asInterface(service);
                storageManager.setField("SystemLocale", Locale.getDefault().toLanguageTag());
            } catch (Exception e) {
                Log.e(TAG, "Error storing locale for decryption UI", e);
            }
        }
    };

    private void establishFinalConfirmationState() {
        mFinalButton = (Button) mContentView.findViewById(R.id.execute_encrypt);
        mFinalButton.setOnClickListener(mFinalClickListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        ViewGroup  root= (ViewGroup)super.onCreateView(inflater, container, savedInstanceState);
        mContentView = inflater.inflate(R.layout.crypt_keeper_confirm, root);
        establishFinalConfirmationState();
        return root;
    }
}
