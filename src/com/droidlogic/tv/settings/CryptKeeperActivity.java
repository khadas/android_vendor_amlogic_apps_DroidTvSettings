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
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.content.Intent;
import android.app.Fragment;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.view.View;
import android.util.Log;

public class CryptKeeperActivity extends TvSettingsActivity{

    @Override
    protected Fragment createSettingsFragment() {
        return SettingsFragment.newInstance();
    }
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
		Log.d("CryptKeeper","xxxonActivityResult"+requestCode);
	}

    public static class SettingsFragment extends BaseSettingsFragment {

        public static SettingsFragment newInstance() {
            return new SettingsFragment();
        }
        @Override
        public void onPreferenceStartInitialScreen() {
            final CryptKeeperFragment fragment = new CryptKeeperFragment();
            startPreferenceFragment(fragment);
        }
    }

}