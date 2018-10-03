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

package com.droidlogic.tv.settings.tvoption;

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


import com.droidlogic.tv.settings.util.DroidUtils;
import com.droidlogic.tv.settings.SettingsConstant;
import com.droidlogic.tv.settings.R;

import com.droidlogic.app.tv.TvControlManager;

public class MultiSoundSeekBarFragment extends LeanbackPreferenceFragment implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "MultiSoundSeekBarFragment";

    private SeekBar seekbar_treble;
    private SeekBar seekbar_bass;
    private SeekBar seekbar_balance;

    private TextView text_treble;
    private TextView text_bass;
    private TextView text_balance;

    private TvOptionSettingManager mTvOptionSettingManager;
    private boolean isSeekBarInited = false;

    public static MultiSoundSeekBarFragment newInstance() {
        return new MultiSoundSeekBarFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.xml.tv_sound_seekbar, container, false);
        return view;
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        if (mTvOptionSettingManager == null) {
            mTvOptionSettingManager = new TvOptionSettingManager(getActivity());
        }
        initSeekBar(view);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    private void initSeekBar(View view) {
        int status = -1;
        boolean hasfocused = false;
        seekbar_treble = (SeekBar) view.findViewById(R.id.seekbar_tv_treble);
        text_treble = (TextView) view.findViewById(R.id.text_tv_treble);
        if (true) {
            status = mTvOptionSettingManager.getTrebleStatus();
            seekbar_treble.setOnSeekBarChangeListener(this);
            seekbar_treble.setProgress(status);
            setShow(R.id.seekbar_tv_treble, status);
            seekbar_treble.requestFocus();
            hasfocused = true;
        } else {
            seekbar_treble.setVisibility(View.GONE);
            text_treble.setVisibility(View.GONE);
        }
        seekbar_bass = (SeekBar) view.findViewById(R.id.seekbar_tv_bass);
        text_bass = (TextView) view.findViewById(R.id.text_tv_bass);
        if (true) {
            status = mTvOptionSettingManager.getBassStatus();
            seekbar_bass.setOnSeekBarChangeListener(this);
            seekbar_bass.setProgress(status);
            setShow(R.id.seekbar_tv_bass, status);
            if (!hasfocused) {
                seekbar_bass.requestFocus();
                hasfocused = true;
            }
        } else {
            seekbar_bass.setVisibility(View.GONE);
            text_bass.setVisibility(View.GONE);
        }
        seekbar_balance= (SeekBar) view.findViewById(R.id.seekbar_tv_balance);
        text_balance = (TextView) view.findViewById(R.id.text_tv_balance);
        if (true) {
            status = mTvOptionSettingManager.getBalanceStatus();
            seekbar_balance.setOnSeekBarChangeListener(this);
            seekbar_balance.setProgress(status);
            setShow(R.id.seekbar_tv_balance, status);
            if (!hasfocused) {
                seekbar_balance.requestFocus();
                hasfocused = true;
            }
        } else {
            seekbar_balance.setVisibility(View.GONE);
            text_balance.setVisibility(View.GONE);
        }
        isSeekBarInited = true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!isSeekBarInited) {
            return;
        }
        switch (seekBar.getId()) {
            case R.id.seekbar_tv_treble:{
                setShow(R.id.seekbar_tv_treble, progress);
                mTvOptionSettingManager.setTreble(progress - mTvOptionSettingManager.getTrebleStatus());
                break;
            }
            case R.id.seekbar_tv_bass:{
                setShow(R.id.seekbar_tv_bass, progress);
                mTvOptionSettingManager.setBass(progress - mTvOptionSettingManager.getBassStatus());
                break;
            }
            case R.id.seekbar_tv_balance:{
                setShow(R.id.seekbar_tv_balance, progress);
                mTvOptionSettingManager.setBalance(progress - mTvOptionSettingManager.getBalanceStatus());
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
            case R.id.seekbar_tv_treble:{
                text_treble.setText(getShowString(R.string.tv_treble, value));
                break;
            }
            case R.id.seekbar_tv_bass:{
                text_bass.setText(getShowString(R.string.tv_bass, value));
                break;
            }
            case R.id.seekbar_tv_balance:{
                text_balance.setText(getShowString(R.string.tv_balance, value));
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
