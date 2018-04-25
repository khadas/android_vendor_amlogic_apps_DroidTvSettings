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

package com.droidlogic.tv.settings.pqsettings;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioSystem;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.media.tv.TvInputInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.droidlogic.tv.settings.SettingsConstant;
import com.droidlogic.tv.settings.MainFragment;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.SettingsConstant;

import com.droidlogic.app.OutputModeManager;
import com.droidlogic.app.tv.DroidLogicTvUtils;
import com.droidlogic.app.tv.TvControlManager;

public class PictrueModeFragment extends LeanbackPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "PictrueModeFragment";
    private static final String PQ_PICTRUE_MODE = "pq_pictrue_mode";
    private static final String PQ_BRIGHTNESS = "pq_brightness";
    private static final String PQ_CONTRAST = "pq_contrast";
    private static final String PQ_COLOR = "pq_color";
    private static final String PQ_SHARPNESS = "pq_sharpness";
    private static final String PQ_BACKLIGHT = "pq_backlight";
    private static final String PQ_COLOR_TEMPRATURE = "pq_color_temprature";
    private static final String PQ_ASPECT_RATIO = "pq_aspect_ratio";
    private static final String PQ_DNR = "pq_dnr";
    private static final String PQ_CUSTOM = "pq_custom";

    private static final String CURRENT_DEVICE_ID = "current_device_id";
    private static final String TV_CURRENT_DEVICE_ID = "tv_current_device_id";

    private PQSettingsManager mPQSettingsManager;

    public static PictrueModeFragment newInstance() {
        return new PictrueModeFragment();
    }

    private boolean CanDebug() {
        return PQSettingsManager.CanDebug();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final ListPreference picturemodePref = (ListPreference) findPreference(PQ_PICTRUE_MODE);
        if (TvInputInfo.TYPE_HDMI == getInputType()) {
            picturemodePref.setEntries(setHdmiPicEntries());
            picturemodePref.setEntryValues(setHdmiPicEntryValues());
        }
        picturemodePref.setValue(mPQSettingsManager.getPictureModeStatus());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        final View innerView = super.onCreateView(inflater, container, savedInstanceState);
        if (getActivity().getIntent().getIntExtra("from_live_tv", 0) == 1) {
            //MainFragment.changeToLiveTvStyle(innerView, getActivity());
        }
        return innerView;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pq_pictrue_mode, null);
        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }

        int is_from_live_tv = getActivity().getIntent().getIntExtra("from_live_tv", 0);
        boolean isTv = SettingsConstant.needDroidlogicTvFeature(getActivity());
        boolean hasMboxFeature = SettingsConstant.hasMboxFeature(getActivity());
        final ListPreference picturemodePref = (ListPreference) findPreference(PQ_PICTRUE_MODE);
        if (TvInputInfo.TYPE_HDMI == getInputType()) {
            picturemodePref.setEntries(setHdmiPicEntries());
            picturemodePref.setEntryValues(setHdmiPicEntryValues());
        }
        if ((isTv && getActivity().getResources().getBoolean(R.bool.tv_pq_need_pictrue_mode)) ||
                (!isTv && getActivity().getResources().getBoolean(R.bool.box_pq_need_pictrue_mode))) {
            picturemodePref.setValue(mPQSettingsManager.getPictureModeStatus());
            picturemodePref.setOnPreferenceChangeListener(this);
        } else {
            picturemodePref.setVisible(false);
        }
        final ListPreference aspectratioPref = (ListPreference) findPreference(PQ_ASPECT_RATIO);
        if ((isTv && getActivity().getResources().getBoolean(R.bool.tv_pq_need_aspect_ratio)) ||
                (!isTv && getActivity().getResources().getBoolean(R.bool.box_pq_need_aspect_ratio))) {
            if (is_from_live_tv == 1) {
                TvControlManager mTvControlManager = TvControlManager.getInstance();
                int mSourceInputType = mTvControlManager.GetCurrentSourceInput();
                //int mDeviceId = Settings.System.getInt(getActivity().getContentResolver(), TV_CURRENT_DEVICE_ID, DroidLogicTvUtils.DEVICE_ID_ADTV);
                if (mSourceInputType == -1) {
                    aspectratioPref.setEnabled(false);
                } else {
                    aspectratioPref.setValueIndex(mPQSettingsManager.getAspectRatioStatus());
                }
            } else {
                aspectratioPref.setVisible(false);
            }
            aspectratioPref.setOnPreferenceChangeListener(this);
        } else {
            aspectratioPref.setVisible(false);
        }
        final ListPreference colortemperaturePref = (ListPreference) findPreference(PQ_COLOR_TEMPRATURE);
        if (!hasMboxFeature) {
            if ((isTv && getActivity().getResources().getBoolean(R.bool.tv_pq_need_color_temprature)) ||
                (!isTv && getActivity().getResources().getBoolean(R.bool.box_pq_need_color_temprature))) {
                if (is_from_live_tv == 1) {
                    colortemperaturePref.setValueIndex(mPQSettingsManager.getColorTemperatureStatus());
                } else {
                    colortemperaturePref.setVisible(false);
                }
                colortemperaturePref.setOnPreferenceChangeListener(this);
            } else {
                colortemperaturePref.setVisible(false);
            }
        } else {
            colortemperaturePref.setVisible(false);
        }
        final ListPreference dnrPref = (ListPreference) findPreference(PQ_DNR);
        if ((isTv && getActivity().getResources().getBoolean(R.bool.tv_pq_need_dnr)) ||
                (!isTv && getActivity().getResources().getBoolean(R.bool.box_pq_need_dnr))) {
            dnrPref.setValueIndex(mPQSettingsManager.getDnrStatus());
            dnrPref.setOnPreferenceChangeListener(this);
        } else {
            dnrPref.setVisible(false);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (CanDebug()) Log.d(TAG, "[onPreferenceTreeClick] preference.getKey() = " + preference.getKey());
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "[onPreferenceChange] preference.getKey() = " + preference.getKey() + ", newValue = " + newValue);
        //final int selection = Integer.parseInt((String)newValue);
        if (TextUtils.equals(preference.getKey(), PQ_PICTRUE_MODE)) {
            mPQSettingsManager.setPictureMode((String)newValue);
        } else if (TextUtils.equals(preference.getKey(), PQ_COLOR_TEMPRATURE)) {
            final int selection = Integer.parseInt((String)newValue);
            mPQSettingsManager.setColorTemperature(selection);
        } else if (TextUtils.equals(preference.getKey(), PQ_DNR)) {
            final int selection = Integer.parseInt((String)newValue);
            mPQSettingsManager.setDnr(selection);
        } else if (TextUtils.equals(preference.getKey(), PQ_ASPECT_RATIO)) {
            final int selection = Integer.parseInt((String)newValue);
            mPQSettingsManager.setAspectRatio(selection);
        }
        return true;
    }

    private final int[] HDMI_PIC_RES = {R.string.pq_standard, R.string.pq_vivid, R.string.pq_soft, R.string.pq_monitor,R.string.pq_user};
    private final String[] HDMI_PIC_MODE = {PQSettingsManager.STATUS_STANDARD, PQSettingsManager.STATUS_VIVID, PQSettingsManager.STATUS_SOFT,
        PQSettingsManager.STATUS_MONITOR, PQSettingsManager.STATUS_USER};

    private String[] setHdmiPicEntries() {
        String[] temp = new String[HDMI_PIC_RES.length];
        if (TvInputInfo.TYPE_HDMI == getInputType()) {
            for (int i = 0; i < HDMI_PIC_RES.length; i++) {
                temp[i] = getString(HDMI_PIC_RES[i]);
            }
        }
        return temp;
    }

    private String[] setHdmiPicEntryValues() {
        String[] temp = new String[HDMI_PIC_MODE.length];
        if (TvInputInfo.TYPE_HDMI == getInputType()) {
            for (int i = 0; i < HDMI_PIC_MODE.length; i++) {
                temp[i] = HDMI_PIC_MODE[i];
            }
        }
        return temp;
    }

    private int getInputType() {
        final int DEFAULT = -1;
        return Settings.System.getInt(getActivity().getContentResolver(), "current_input_type", DEFAULT);
    }
}
