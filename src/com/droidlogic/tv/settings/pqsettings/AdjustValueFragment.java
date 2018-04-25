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

import android.os.Bundle;
import android.os.Handler;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.os.SystemProperties;
import android.util.Log;
import android.text.TextUtils;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.provider.Settings;

import com.droidlogic.tv.settings.util.DroidUtils;
import com.droidlogic.tv.settings.SettingsConstant;
import com.droidlogic.tv.settings.R;

public class AdjustValueFragment extends LeanbackPreferenceFragment implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "AdjustValueFragment";

    private SeekBar seekbar_brightness;
    private SeekBar seekbar_contrast;
    private SeekBar seekbar_color;
    private SeekBar seekbar_sharpness;
    private SeekBar seekbar_tone;
    private TextView text_brightness;
    private TextView text_contrast;
    private TextView text_color;
    private TextView text_sharpness;
    private TextView text_tone;
    private PQSettingsManager mPQSettingsManager;
    private boolean isSeekBarInited = false;

    public static AdjustValueFragment newInstance() {
        return new AdjustValueFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.xml.seekbar, container, false);
        return view;
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        initSeekBar(view);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    private void initSeekBar(View view) {
        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }
        int status = -1;
        boolean hasfocused = false;
        boolean isTv = SettingsConstant.needDroidlogicTvFeature(getActivity());
        seekbar_brightness = (SeekBar) view.findViewById(R.id.seekbar_brightness);
        text_brightness = (TextView) view.findViewById(R.id.text_brightness);
        if ((isTv && getActivity().getResources().getBoolean(R.bool.tv_pq_need_brightness)) ||
                (!isTv && getActivity().getResources().getBoolean(R.bool.box_pq_need_brightness))) {
            status = mPQSettingsManager.getBrightnessStatus();
            seekbar_brightness.setOnSeekBarChangeListener(this);
            seekbar_brightness.setProgress(status);
            setShow(R.id.seekbar_brightness, status);
            seekbar_brightness.requestFocus();
            hasfocused = true;
        } else {
            seekbar_brightness.setVisibility(View.GONE);
            text_brightness.setVisibility(View.GONE);
        }
        seekbar_contrast = (SeekBar) view.findViewById(R.id.seekbar_contrast);
        text_contrast = (TextView) view.findViewById(R.id.text_contrast);
        if ((isTv && getActivity().getResources().getBoolean(R.bool.tv_pq_need_contrast)) ||
                (!isTv && getActivity().getResources().getBoolean(R.bool.box_pq_need_contrast))) {
            status = mPQSettingsManager.getContrastStatus();
            seekbar_contrast.setOnSeekBarChangeListener(this);
            seekbar_contrast.setProgress(status);
            setShow(R.id.seekbar_contrast, status);
            if (!hasfocused) {
                seekbar_contrast.requestFocus();
                hasfocused = true;
            }
        } else {
            seekbar_contrast.setVisibility(View.GONE);
            text_contrast.setVisibility(View.GONE);
        }
        seekbar_color = (SeekBar) view.findViewById(R.id.seekbar_color);
        text_color = (TextView) view.findViewById(R.id.text_color);
        if ((isTv && getActivity().getResources().getBoolean(R.bool.tv_pq_need_color)) ||
                (!isTv && getActivity().getResources().getBoolean(R.bool.box_pq_need_color))) {
            status = mPQSettingsManager.getColorStatus();
            seekbar_color.setOnSeekBarChangeListener(this);
            seekbar_color.setProgress(status);
            setShow(R.id.seekbar_color, status);
            if (!hasfocused) {
                seekbar_color.requestFocus();
                hasfocused = true;
            }
        } else {
            seekbar_color.setVisibility(View.GONE);
            text_color.setVisibility(View.GONE);
        }
        seekbar_sharpness = (SeekBar) view.findViewById(R.id.seekbar_sharpness);
        text_sharpness = (TextView) view.findViewById(R.id.text_sharpness);
        if ((isTv && getActivity().getResources().getBoolean(R.bool.tv_pq_need_sharpness)) ||
                (!isTv && getActivity().getResources().getBoolean(R.bool.box_pq_need_sharpness))) {
            status = mPQSettingsManager.getSharpnessStatus();
            seekbar_sharpness.setOnSeekBarChangeListener(this);
            seekbar_sharpness.setProgress(status);
            setShow(R.id.seekbar_sharpness, status);
            if (!hasfocused) {
                seekbar_sharpness.requestFocus();
                hasfocused = true;
            }
        } else {
            seekbar_sharpness.setVisibility(View.GONE);
            text_sharpness.setVisibility(View.GONE);
        }
        seekbar_tone= (SeekBar) view.findViewById(R.id.seekbar_tone);
        text_tone= (TextView) view.findViewById(R.id.text_tone);
        if (((isTv && getActivity().getResources().getBoolean(R.bool.tv_pq_need_tone)) ||
                (!isTv && getActivity().getResources().getBoolean(R.bool.box_pq_need_tone))) && mPQSettingsManager.isNtscSignalOrNot()) {
            status = mPQSettingsManager.getToneStatus();
            seekbar_tone.setOnSeekBarChangeListener(this);
            seekbar_tone.setProgress(status);
            setShow(R.id.seekbar_tone, status);
            if (!hasfocused) {
                seekbar_tone.requestFocus();
                hasfocused = true;
            }
        } else {
            seekbar_tone.setVisibility(View.GONE);
            text_tone.setVisibility(View.GONE);
        }
        isSeekBarInited = true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!isSeekBarInited) {
            return;
        }
        switch (seekBar.getId()) {
            case R.id.seekbar_brightness:{
                setShow(R.id.seekbar_brightness, progress);
                mPQSettingsManager.setBrightness(progress - mPQSettingsManager.getBrightnessStatus());
                break;
            }
            case R.id.seekbar_contrast:{
                setShow(R.id.seekbar_contrast, progress);
                mPQSettingsManager.setContrast(progress - mPQSettingsManager.getContrastStatus());
                break;
            }
            case R.id.seekbar_color:{
                setShow(R.id.seekbar_color, progress);
                mPQSettingsManager.setColor(progress - mPQSettingsManager.getColorStatus());
                break;
            }
            case R.id.seekbar_sharpness:{
                setShow(R.id.seekbar_sharpness, progress);
                mPQSettingsManager.setSharpness(progress - mPQSettingsManager.getSharpnessStatus());
                break;
            }
            case R.id.seekbar_tone:{
                setShow(R.id.seekbar_tone, progress);
                mPQSettingsManager.setTone(progress - mPQSettingsManager.getToneStatus());
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    private void setShow(int id, int value) {
        switch (id) {
            case R.id.seekbar_brightness:{
                text_brightness.setText(getShowString(R.string.pq_brightness, value));
                break;
            }
            case R.id.seekbar_contrast:{
                text_contrast.setText(getShowString(R.string.pq_contrast, value));
                break;
            }
            case R.id.seekbar_color:{
                text_color.setText(getShowString(R.string.pq_color, value));
                break;
            }
            case R.id.seekbar_sharpness:{
                text_sharpness.setText(getShowString(R.string.pq_sharpness, value));
                break;
            }
            case R.id.seekbar_tone:{
                text_tone.setText(getShowString(R.string.pq_tone, value));
                break;
            }
            default:
                break;
        }
    }

    private String getShowString(int resid, int value) {
        return getActivity().getResources().getString(resid) + ": " + value + "%";
    }
}
