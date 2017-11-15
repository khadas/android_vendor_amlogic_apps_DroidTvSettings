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

import android.app.Activity;
import android.app.Fragment;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.os.UserManager;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.LinearLayout;

public abstract class ConfirmDeviceCredentialBaseActivity extends TvSettingsActivity {

    private static final String STATE_IS_KEYGUARD_LOCKED = "STATE_IS_KEYGUARD_LOCKED";

    enum ConfirmCredentialTheme {
        INTERNAL,
        DARK,
        WORK
    }

    private boolean mRestoring;
    private boolean mEnterAnimationPending;
    private boolean mFirstTimeVisible = true;
    private boolean mIsKeyguardLocked = false;
    private ConfirmCredentialTheme mConfirmCredentialTheme;

    @Override
    protected void onCreate(Bundle savedState) {
        int credentialOwnerUserId = Utils.getCredentialOwnerUserId(this,
                Utils.getUserIdFromBundle(this, getIntent().getExtras()));
        if (UserManager.get(this).isManagedProfile(credentialOwnerUserId)) {
            mConfirmCredentialTheme = ConfirmCredentialTheme.WORK;
        } else if (getIntent().getBooleanExtra(
                ConfirmDeviceCredentialBaseFragment.DARK_THEME, false)) {
            mConfirmCredentialTheme = ConfirmCredentialTheme.DARK;
        } else {
            mConfirmCredentialTheme = ConfirmCredentialTheme.INTERNAL;
        }
        super.onCreate(savedState);
        mRestoring = savedState != null;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_IS_KEYGUARD_LOCKED, mIsKeyguardLocked);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isChangingConfigurations() && !mRestoring
                && mConfirmCredentialTheme == ConfirmCredentialTheme.DARK && mFirstTimeVisible) {
            mFirstTimeVisible = false;
            prepareEnterAnimation();
            mEnterAnimationPending = true;
        }
    }

    private ConfirmDeviceCredentialBaseFragment getFragment() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.main_content);
        if (fragment != null && fragment instanceof ConfirmDeviceCredentialBaseFragment) {
            return (ConfirmDeviceCredentialBaseFragment) fragment;
        }
        return null;
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        if (mEnterAnimationPending) {
            startEnterAnimation();
            mEnterAnimationPending = false;
        }
    }

    public void prepareEnterAnimation() {
        getFragment().prepareEnterAnimation();
    }

    public void startEnterAnimation() {
        getFragment().startEnterAnimation();
    }

    public ConfirmCredentialTheme getConfirmCredentialTheme() {
        return mConfirmCredentialTheme;
    }
}
