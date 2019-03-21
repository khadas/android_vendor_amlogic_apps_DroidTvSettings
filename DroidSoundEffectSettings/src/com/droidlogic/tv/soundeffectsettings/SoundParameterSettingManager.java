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

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.media.AudioManager;
import android.app.ActivityManager;
import android.provider.Settings;
import android.content.SharedPreferences;
import android.content.ContentResolver;

import com.droidlogic.tv.soundeffectsettings.R;
import com.droidlogic.app.AudioOutputManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.SimpleTimeZone;

public class SoundParameterSettingManager {

    public static final String TAG = "SoundParameterSettingManager";

    private Resources mResources;
    private Context mContext;
    private AudioManager mAudioManager;
    private AudioOutputManager mAudioOutputManager;

    public SoundParameterSettingManager (Context context) {
        mContext = context;
        mResources = mContext.getResources();
        mAudioManager = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);
        mAudioOutputManager = new AudioOutputManager(context);
    }

    private boolean CanDebug() {
        return OptionParameterManager.CanDebug();
    }

    public int getSoundOutputStatus () {
        final int itemPosition =  Settings.Global.getInt(mContext.getContentResolver(),
                AudioOutputManager.SOUND_OUTPUT_DEVICE, AudioOutputManager.SOUND_OUTPUT_DEVICE_SPEAKER);
        if (CanDebug()) Log.d(TAG, "getSoundOutputStatus = " + itemPosition);
        return itemPosition;
    }

    public void setSoundOutputStatus (int mode) {
        if (CanDebug()) Log.d(TAG, "setSoundOutputStatus = " + mode);
        mAudioOutputManager.setSoundOutputStatus(mode);
        Settings.Global.putInt(mContext.getContentResolver(), AudioOutputManager.SOUND_OUTPUT_DEVICE, mode);
        Settings.Global.putInt(mContext.getContentResolver(),
                "hdmi_system_audio_status_enabled" /* Settings.Global.HDMI_SYSTEM_AUDIO_STATUS_ENABLED */,
                mode == AudioOutputManager.SOUND_OUTPUT_DEVICE_ARC ? AudioOutputManager.TV_ARC_ON : AudioOutputManager.TV_ARC_OFF);
    }
}

