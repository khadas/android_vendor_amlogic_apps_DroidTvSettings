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

package com.droidlogic.tv.soundeffectsettings;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.util.Log;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.content.Context;
import android.app.AlertDialog;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;

import com.droidlogic.tv.soundeffectsettings.R;

public class SoundModeFragment extends LeanbackPreferenceFragment implements Preference.OnPreferenceChangeListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "SoundModeFragment";

    private static final String TV_EQ_MODE = "tv_sound_mode";
    private static final String TV_TREBLE_BASS_SETTINGS = "treble_bass_effect_settings";
    private static final String TV_BALANCE_SETTINGS = "balance_effect_settings";
    private static final String TV_DTS_SETTINGS = "dts_effect_settings";
    private static final String TV_VIRTUAL_SURROUND_SETTINGS = "tv_sound_virtual_surround";
    private static final String TV_SOUND_OUT = "tv_sound_output_device";
    private static final String TV_AGC = "effect_agc";

    private SoundEffectSettingManager mSoundEffectSettingManager;
    private SoundParameterSettingManager mSoundParameterSettingManager;

    private static final int UI_LOAD_TIMEOUT = 50;//100ms
    private static final int LOAD_UI = 0;

    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOAD_UI:
                    if (!initView()) {
                        myHandler.sendEmptyMessageDelayed(LOAD_UI, UI_LOAD_TIMEOUT);
                    } else {
                        myHandler.removeCallbacksAndMessages(null);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public static SoundModeFragment newInstance() {
        return new SoundModeFragment();
    }

    private boolean CanDebug() {
        return OptionParameterManager.CanDebug();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSoundEffectSettingManager != null) {
            final ListPreference eqmode = (ListPreference) findPreference(TV_EQ_MODE);
            if (mSoundEffectSettingManager.getSoundModule() == SoundEffectSettingManager.DAP_MODULE) {
                eqmode.setEntries(getArrayString(R.array.tv_sound_mode_extend_entries));
                eqmode.setEntryValues(getArrayString(R.array.tv_sound_mode_extend_entry_values));
            }
            eqmode.setValueIndex(mSoundEffectSettingManager.getSoundModeStatus());
            final Preference treblebass = (Preference) findPreference(TV_TREBLE_BASS_SETTINGS);
            String treblebasssummary = getShowString(R.string.tv_treble, mSoundEffectSettingManager.getTrebleStatus()) + " " +
                    getShowString(R.string.tv_bass, mSoundEffectSettingManager.getBassStatus());
            treblebass.setSummary(treblebasssummary);
            final Preference balance = (Preference) findPreference(TV_BALANCE_SETTINGS);
            balance.setSummary(getShowString(R.string.tv_balance_effect, mSoundEffectSettingManager.getBalanceStatus()));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void init() {
        mSoundEffectSettingManager = ((TvSettingsActivity)getActivity()).getSoundEffectSettingManager();
        mSoundParameterSettingManager = ((TvSettingsActivity)getActivity()).getSoundParameterSettingManager();
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
        setPreferencesFromResource(R.xml.tv_sound_mode, null);
        myHandler.sendEmptyMessage(LOAD_UI);
    }

    private boolean initView() {
        init();
        if (mSoundEffectSettingManager == null) {
            Log.e(TAG, "onCreatePreferences mSoundEffectSettingManager == null");
            return false;
        }
        final ListPreference eqmode = (ListPreference) findPreference(TV_EQ_MODE);
        if (mSoundEffectSettingManager.getSoundModule() == SoundEffectSettingManager.DAP_MODULE) {
            eqmode.setEntries(getArrayString(R.array.tv_sound_mode_extend_entries));
            eqmode.setEntryValues(getArrayString(R.array.tv_sound_mode_extend_entry_values));
        }
        eqmode.setValueIndex(mSoundEffectSettingManager.getSoundModeStatus());
        eqmode.setOnPreferenceChangeListener(this);

        final ListPreference virtualsurround = (ListPreference) findPreference(TV_VIRTUAL_SURROUND_SETTINGS);
        virtualsurround.setValueIndex(mSoundEffectSettingManager.getVirtualSurroundStatus());
        virtualsurround.setOnPreferenceChangeListener(this);

        final ListPreference soundout = (ListPreference) findPreference(TV_SOUND_OUT);
        soundout.setValueIndex(mSoundParameterSettingManager.getSoundOutputStatus());
        soundout.setOnPreferenceChangeListener(this);

        final Preference treblebass = (Preference) findPreference(TV_TREBLE_BASS_SETTINGS);
        String treblebasssummary = getShowString(R.string.tv_treble, mSoundEffectSettingManager.getTrebleStatus()) + " " +
                getShowString(R.string.tv_bass, mSoundEffectSettingManager.getBassStatus());
        treblebass.setSummary(treblebasssummary);

        final Preference balance = (Preference) findPreference(TV_BALANCE_SETTINGS);
        balance.setSummary(getShowString(R.string.tv_balance_effect, mSoundEffectSettingManager.getBalanceStatus()));
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (CanDebug()) Log.d(TAG, "[onPreferenceTreeClick] preference.getKey() = " + preference.getKey());
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (CanDebug()) Log.d(TAG, "[onPreferenceChange] preference.getKey() = " + preference.getKey() + ", newValue = " + newValue);
        final int selection = Integer.parseInt((String)newValue);
        if (TextUtils.equals(preference.getKey(), TV_EQ_MODE)) {
            mSoundEffectSettingManager.setSoundMode(selection);
            if (selection == SoundEffectSettingManager.MODE_CUSTOM) {
                createUiDialog();
            }
        } else if (TextUtils.equals(preference.getKey(), TV_VIRTUAL_SURROUND_SETTINGS)) {
            mSoundEffectSettingManager.setVirtualSurround(selection);
        }else if (TextUtils.equals(preference.getKey(), TV_SOUND_OUT)) {
            mSoundParameterSettingManager.setSoundOutputStatus(selection);
        }
        return true;
    }

    private void createUiDialog () {
        Context context = (Context) (getActivity());
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.xml.tv_sound_effect_ui, null);//tv_sound_effect_ui
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final AlertDialog mAlertDialog = builder.create();
        mAlertDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isSeekBarInited = false;
            }
        });
        mAlertDialog.show();
        mAlertDialog.getWindow().setContentView(view);
        //mAlertDialog.getWindow().setLayout(150, 320);
        initSeekBar(view);
    }

    private boolean isSeekBarInited = false;
    private SeekBar mBand1Seekbar;
    private TextView mBand1Text;
    private SeekBar mBand2Seekbar;
    private TextView mBand2Text;
    private SeekBar mBand3Seekbar;
    private TextView mBand3Text;
    private SeekBar mBand4Seekbar;
    private TextView mBand4Text;
    private SeekBar mBand5Seekbar;
    private TextView mBand5Text;

    private void initSeekBar(View view) {
        if (mSoundEffectSettingManager == null) {
            mSoundEffectSettingManager = ((TvSettingsActivity)getActivity()).getSoundEffectSettingManager();
        }
        int status = -1;
        mBand1Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band1);
        mBand1Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band1);
        status = mSoundEffectSettingManager.getParameters(SoundEffectSettingManager.SET_EFFECT_BAND1);
        mBand1Seekbar.setOnSeekBarChangeListener(this);
        mBand1Seekbar.setProgress(status);
        setShow(SoundEffectSettingManager.SET_EFFECT_BAND1, status);
        mBand1Seekbar.requestFocus();
        mBand2Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band2);
        mBand2Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band2);
        status = mSoundEffectSettingManager.getParameters(SoundEffectSettingManager.SET_EFFECT_BAND2);
        mBand2Seekbar.setOnSeekBarChangeListener(this);
        mBand2Seekbar.setProgress(status);
        setShow(SoundEffectSettingManager.SET_EFFECT_BAND2, status);
        mBand3Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band3);
        mBand3Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band3);
        status = mSoundEffectSettingManager.getParameters(SoundEffectSettingManager.SET_EFFECT_BAND3);
        mBand3Seekbar.setOnSeekBarChangeListener(this);
        mBand3Seekbar.setProgress(status);
        setShow(SoundEffectSettingManager.SET_EFFECT_BAND3, status);
        mBand4Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band4);
        mBand4Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band4);
        status = mSoundEffectSettingManager.getParameters(SoundEffectSettingManager.SET_EFFECT_BAND4);
        mBand4Seekbar.setOnSeekBarChangeListener(this);
        mBand4Seekbar.setProgress(status);
        setShow(SoundEffectSettingManager.SET_EFFECT_BAND4, status);
        mBand5Seekbar = (SeekBar) view.findViewById(R.id.seekbar_tv_audio_effect_band5);
        mBand5Text = (TextView) view.findViewById(R.id.text_tv_audio_effect_band5);
        status = mSoundEffectSettingManager.getParameters(SoundEffectSettingManager.SET_EFFECT_BAND5);
        mBand5Seekbar.setOnSeekBarChangeListener(this);
        mBand5Seekbar.setProgress(status);
        setShow(SoundEffectSettingManager.SET_EFFECT_BAND5, status);
        isSeekBarInited = true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!isSeekBarInited) {
            return;
        }
        ((TvSettingsActivity)getActivity()).startShowActivityTimer();
        switch (seekBar.getId()) {
            case R.id.seekbar_tv_audio_effect_band1:{
                setShow(SoundEffectSettingManager.SET_EFFECT_BAND1, progress);
                mSoundEffectSettingManager.setParameters(SoundEffectSettingManager.SET_EFFECT_BAND1, progress);
                break;
            }
            case R.id.seekbar_tv_audio_effect_band2:{
                setShow(SoundEffectSettingManager.SET_EFFECT_BAND2, progress);
                mSoundEffectSettingManager.setParameters(SoundEffectSettingManager.SET_EFFECT_BAND2, progress);
                break;
            }
            case R.id.seekbar_tv_audio_effect_band3:{
                setShow(SoundEffectSettingManager.SET_EFFECT_BAND3, progress);
                mSoundEffectSettingManager.setParameters(SoundEffectSettingManager.SET_EFFECT_BAND3, progress);
                break;
            }
            case R.id.seekbar_tv_audio_effect_band4:{
                setShow(SoundEffectSettingManager.SET_EFFECT_BAND4, progress);
                mSoundEffectSettingManager.setParameters(SoundEffectSettingManager.SET_EFFECT_BAND4, progress);
                break;
            }
            case R.id.seekbar_tv_audio_effect_band5:{
                setShow(SoundEffectSettingManager.SET_EFFECT_BAND5, progress);
                mSoundEffectSettingManager.setParameters(SoundEffectSettingManager.SET_EFFECT_BAND5, progress);
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
            case SoundEffectSettingManager.SET_EFFECT_BAND1:{
                mBand1Text.setText(getShowString(R.string.tv_audio_effect_band1, value));
                break;
            }
            case SoundEffectSettingManager.SET_EFFECT_BAND2:{
                mBand2Text.setText(getShowString(R.string.tv_audio_effect_band2, value));
                break;
            }
            case SoundEffectSettingManager.SET_EFFECT_BAND3:{
                mBand3Text.setText(getShowString(R.string.tv_audio_effect_band3, value));
                break;
            }
            case SoundEffectSettingManager.SET_EFFECT_BAND4:{
                mBand4Text.setText(getShowString(R.string.tv_audio_effect_band4, value));
                break;
            }
            case SoundEffectSettingManager.SET_EFFECT_BAND5:{
                mBand5Text.setText(getShowString(R.string.tv_audio_effect_band5, value));
                break;
            }
            default:
                break;
        }
    }

    private String getShowString(int resid, int value) {
        return getActivity().getResources().getString(resid) + " " + value + "%";
    }

    private String[] getArrayString(int resid) {
        return getActivity().getResources().getStringArray(resid);
    }
}
