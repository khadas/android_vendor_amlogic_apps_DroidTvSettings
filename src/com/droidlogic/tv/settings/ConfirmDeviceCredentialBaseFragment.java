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

// TODO (b/35202196): move this class out of the root of the package.
package com.droidlogic.tv.settings;

import android.annotation.Nullable;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.IActivityManager;
import android.app.admin.DevicePolicyManager;
import android.app.trust.TrustManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v14.preference.PreferenceFragment;
import android.support.v17.preference.LeanbackSettingsFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.android.internal.widget.LockPatternUtils;

/**
 * Base fragment to be shared for PIN/Pattern/Password confirmation fragments.
 */
public abstract class ConfirmDeviceCredentialBaseFragment extends LeanbackSettingsFragment {

    public static final String PACKAGE = "com.droidlogic.tv.settings";
    public static final String TITLE_TEXT = PACKAGE + ".ConfirmCredentials.title";
    public static final String HEADER_TEXT = PACKAGE + ".ConfirmCredentials.header";
    public static final String DETAILS_TEXT = PACKAGE + ".ConfirmCredentials.details";
    public static final String ALLOW_FP_AUTHENTICATION =
            PACKAGE + ".ConfirmCredentials.allowFpAuthentication";
    public static final String DARK_THEME = PACKAGE + ".ConfirmCredentials.darkTheme";
    public static final String SHOW_CANCEL_BUTTON =
            PACKAGE + ".ConfirmCredentials.showCancelButton";
    public static final String SHOW_WHEN_LOCKED =
            PACKAGE + ".ConfirmCredentials.showWhenLocked";

    protected Button mCancelButton;
    protected ImageView mFingerprintIcon;
    protected int mEffectiveUserId;
    protected int mUserId;
    protected UserManager mUserManager;
    protected LockPatternUtils mLockPatternUtils;
    protected TextView mErrorTextView;
    protected final Handler mHandler = new Handler();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Only take this argument into account if it belongs to the current profile.
        Intent intent = getActivity().getIntent();
        mUserId = Utils.getUserIdFromBundle(getActivity(), intent.getExtras());
        mUserManager = UserManager.get(getActivity());
        mEffectiveUserId = mUserManager.getCredentialOwnerProfile(mUserId);
        mLockPatternUtils = new LockPatternUtils(getActivity());
    }

    // User could be locked while Effective user is unlocked even though the effective owns the
    // credential. Otherwise, fingerprint can't unlock fbe/keystore through
    // verifyTiedProfileChallenge. In such case, we also wanna show the user message that
    // fingerprint is disabled due to device restart.
    protected boolean isFingerprintDisallowedByStrongAuth() {
        return !(mLockPatternUtils.isFingerprintAllowedForUser(mEffectiveUserId)
                && mUserManager.isUserUnlocked(mUserId));
    }


    @Override
    public void onResume() {
        super.onResume();
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

    protected void setAccessibilityTitle(CharSequence supplementalText) {
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            CharSequence titleText = intent.getCharSequenceExtra(
                    ConfirmDeviceCredentialBaseFragment.TITLE_TEXT);
            if (supplementalText == null) {
                return;
            }
            if (titleText == null) {
                getActivity().setTitle(supplementalText);
            } else {
                String accessibilityTitle =
                        new StringBuilder(titleText).append(",").append(supplementalText).toString();
                getActivity().setTitle(Utils.createAccessibleSequence(titleText, accessibilityTitle));
            }
        }
    }


    protected abstract void authenticationSucceeded();


    public void prepareEnterAnimation() {
    }

    public void startEnterAnimation() {
    }
    protected boolean isProfileChallenge() {
        return mUserManager.isManagedProfile(mEffectiveUserId);
    }

    protected void reportSuccessfullAttempt() {
        if (isProfileChallenge()) {
            mLockPatternUtils.reportSuccessfulPasswordAttempt(mEffectiveUserId);
            // Keyguard is responsible to disable StrongAuth for primary user. Disable StrongAuth
            // for work challenge only here.
            mLockPatternUtils.userPresent(mEffectiveUserId);
        }
    }

    protected void reportFailedAttempt() {
        if (isProfileChallenge()) {
            // + 1 for this attempt.
            updateErrorMessage(
                    mLockPatternUtils.getCurrentFailedPasswordAttempts(mEffectiveUserId) + 1);
            mLockPatternUtils.reportFailedPasswordAttempt(mEffectiveUserId);
        }
    }

    protected void updateErrorMessage(int numAttempts) {
        final int maxAttempts =
                mLockPatternUtils.getMaximumFailedPasswordsForWipe(mEffectiveUserId);
        if (maxAttempts > 0 && numAttempts > 0) {
            int remainingAttempts = maxAttempts - numAttempts;
            if (remainingAttempts == 1) {
                // Last try
                final String title = getActivity().getString(
                        R.string.lock_profile_wipe_warning_title);
                LastTryDialog.show(getFragmentManager(), title, getLastTryErrorMessage(),
                        android.R.string.ok, false /* dismiss */);
            } else if (remainingAttempts <= 0) {
                // Profile is wiped
                LastTryDialog.show(getFragmentManager(), null /* title */,
                        R.string.lock_profile_wipe_content, R.string.lock_profile_wipe_dismiss,
                        true /* dismiss */);
            }
            if (mErrorTextView != null) {
                final String message = getActivity().getString(R.string.lock_profile_wipe_attempts,
                        numAttempts, maxAttempts);
                showError(message, 0);
            }
        }
    }

    protected abstract int getLastTryErrorMessage();

    private final Runnable mResetErrorRunnable = new Runnable() {
        @Override
        public void run() {
            mErrorTextView.setText("");
        }
    };

    protected void showError(CharSequence msg, long timeout) {
        mErrorTextView.setText(msg);
        onShowError();
        mHandler.removeCallbacks(mResetErrorRunnable);
        if (timeout != 0) {
            mHandler.postDelayed(mResetErrorRunnable, timeout);
        }
    }

    protected abstract void onShowError();

    protected void showError(int msg, long timeout) {
        showError(getText(msg), timeout);
    }

    public static class LastTryDialog extends DialogFragment {
        private static final String TAG = LastTryDialog.class.getSimpleName();

        private static final String ARG_TITLE = "title";
        private static final String ARG_MESSAGE = "message";
        private static final String ARG_BUTTON = "button";
        private static final String ARG_DISMISS = "dismiss";

        static boolean show(FragmentManager from, String title, int message, int button,
                boolean dismiss) {
            LastTryDialog existent = (LastTryDialog) from.findFragmentByTag(TAG);
            if (existent != null && !existent.isRemoving()) {
                return false;
            }
            Bundle args = new Bundle();
            args.putString(ARG_TITLE, title);
            args.putInt(ARG_MESSAGE, message);
            args.putInt(ARG_BUTTON, button);
            args.putBoolean(ARG_DISMISS, dismiss);

            DialogFragment dialog = new LastTryDialog();
            dialog.setArguments(args);
            dialog.show(from, TAG);
            return true;
        }

        static void hide(FragmentManager from) {
            LastTryDialog dialog = (LastTryDialog) from.findFragmentByTag(TAG);
            if (dialog != null) {
                dialog.dismissAllowingStateLoss();
                from.executePendingTransactions();
            }
        }

        /**
         * Dialog setup.
         * <p>
         * To make it less likely that the dialog is dismissed accidentally, for example if the
         * device is malfunctioning or if the device is in a pocket, we set
         * {@code setCanceledOnTouchOutside(false)}.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = new AlertDialog.Builder(getActivity())
                    .setTitle(getArguments().getString(ARG_TITLE))
                    .setMessage(getArguments().getInt(ARG_MESSAGE))
                    .setPositiveButton(getArguments().getInt(ARG_BUTTON), null)
                    .create();
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }

        @Override
        public void onDismiss(final DialogInterface dialog) {
            super.onDismiss(dialog);
            if (getActivity() != null && getArguments().getBoolean(ARG_DISMISS)) {
                getActivity().finish();
            }
        }
    }
}
