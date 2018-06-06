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

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.os.SystemProperties;
import android.os.SystemClock;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.content.pm.IPackageDataObserver;
import android.provider.Settings;
import android.widget.Toast;
import android.app.AlarmManager;
import android.app.PendingIntent;

import com.droidlogic.tv.settings.R;
import android.media.tv.TvContract;
import android.media.tv.TvContract.Channels;
import com.droidlogic.app.tv.ChannelInfo;
import com.droidlogic.app.tv.TvDataBaseManager;
import com.droidlogic.app.tv.TvInSignalInfo;
import com.droidlogic.app.tv.TvControlManager;
import com.droidlogic.app.tv.DroidLogicTvUtils;
import com.droidlogic.app.SystemControlManager;
import com.droidlogic.app.tv.TvChannelParams;
import com.droidlogic.app.DaylightSavingTime;

import com.droidlogic.tv.settings.TvSettingsActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.SimpleTimeZone;

public class TvOptionSettingManager {
    public static final int SET_DTMB = 0;
    public static final int SET_DVB_C = 1;
    public static final int SET_DVB_T = 2;
    public static final int SET_DVB_T2 = 3;
    public static final int SET_ATSC_T = 4;
    public static final int SET_ATSC_C= 5;
    public static final int SET_ISDB_T = 6;

    public static final String KEY_DYNAMIC_BACKLIGHT = "dynamic_backlight";
    public static final String KEY_MENU_TIME = "menu_time";
    public static final int DEFUALT_MENU_TIME = 10;

    public static final String STRING_NAME = "name";
    public static final String STRING_STATUS = "status";
    public static final String DTV_AUTOSYNC_TVTIME = "autosync_tvtime";

    public static final String TAG = "TvOptionSettingManager";

    private Resources mResources;
    private Context mContext;
    private ChannelInfo mCurrentChannel;
    private int mDeviceId;
    private String mInputInfoId = null;
    private TvDataBaseManager mTvDataBaseManager;
    private TvControlManager mTvControlManager = TvControlManager.getInstance();
    private SystemControlManager mSystemControlManager;
    private DaylightSavingTime mDaylightSavingTime = null;

    public TvOptionSettingManager (Context context) {
        mContext = context;
        mResources = mContext.getResources();
        mTvDataBaseManager = new TvDataBaseManager(mContext);
        mSystemControlManager = new SystemControlManager(mContext);

        if (SystemProperties.getBoolean("persist.sys.daylight.control", false)) {
            mDaylightSavingTime = DaylightSavingTime.getInstance();
        }
        mDeviceId = ((TvSettingsActivity)context).getIntent().getIntExtra("tv_current_device_id", -1);
        mInputInfoId = ((TvSettingsActivity)context).getIntent().getStringExtra("current_tvinputinfo_id");
        mCurrentChannel = mTvDataBaseManager.getChannelInfo(TvContract.buildChannelUri(((TvSettingsActivity)context).getIntent().getLongExtra("current_channel_id", -1)));
    }

    private boolean CanDebug() {
        return TvOptionFragment.CanDebug();
    }

    public int getSoundModeStatus () {
        int itemPosition = mTvControlManager.GetCurAudioSoundMode();
        if (CanDebug()) Log.d(TAG, "getSoundModeStatus = " + itemPosition);
        if (itemPosition < 0 || itemPosition > 4) {
            itemPosition = 0;
        }
        return itemPosition;
    }

    public int getTrebleStatus () {
        int itemPosition = mTvControlManager.GetCurAudioTrebleVolume();
        if (CanDebug()) Log.d(TAG, "getTrebleStatus = " + itemPosition);
        return itemPosition;
    }

    public int getBassStatus () {
        int itemPosition = mTvControlManager.GetCurAudioBassVolume();
        if (CanDebug()) Log.d(TAG, "getBassStatus = " + itemPosition);
        return itemPosition;
    }

    public int getBalanceStatus () {
        int itemPosition = mTvControlManager.GetCurAudioBalance();
        if (CanDebug()) Log.d(TAG, "getBalanceStatus = " + itemPosition);
        return itemPosition;
    }

    public int getSpdifStatus () {
        //0 ~ off
        if (mTvControlManager.GetCurAudioSPDIFSwitch() == 0) {
            if (CanDebug()) Log.d(TAG, "getSpdifStatus = " + 0);
            return 0;
        }
        // 0 1 ~ pcm raw
        int itemPosition = mTvControlManager.GetCurAudioSPDIFMode();
        if (CanDebug()) Log.d(TAG, "getSpdifStatus = " + itemPosition);
        if (itemPosition < 0 || itemPosition > 1) {
            itemPosition = -1;
        }
        return itemPosition + 1;
    }

    // 0 1 ~ off on
    public int getSurroundStatus () {
        int itemPosition = mTvControlManager.GetCurAudioSrsSurround();
        if (CanDebug()) Log.d(TAG, "getSurroundStatus = " + itemPosition);
        if (itemPosition != 0) {
            itemPosition = 1;
        }
        return itemPosition;
    }

    // 0 1 ~ off on
    public int getVirtualSurroundStatus() {
        int itemPosition = mTvControlManager.GetAudioVirtualizerEnable();
        if (CanDebug()) Log.d(TAG, "getVirtualSurroundStatus = " + itemPosition);
        if (itemPosition != 0) {
            itemPosition = 1;
        }
        return itemPosition;
    }

    // 0 1 ~ off on
    public int getDialogClarityStatus () {
        int itemPosition = mTvControlManager.GetCurAudioSrsDialogClarity();
        if (CanDebug()) Log.d(TAG, "getDialogClarityStatus = " + itemPosition);
        if (itemPosition != 0) {
            itemPosition = 1;
        }
        return itemPosition;
    }

    // 0 1 ~ off on
    public int getBassBoostStatus () {
        int itemPosition = mTvControlManager.GetCurAudioSrsTruBass();
        if (CanDebug()) Log.d(TAG, "getBassBoostStatus = " + itemPosition);
        if (itemPosition != 0) {
            itemPosition = 1;
        }
        return itemPosition;
    }

    public void setSoundMode (int mode) {
        if (CanDebug()) Log.d(TAG, "setSoundMode = " + mode);
        if (mode == 0) {
            mTvControlManager.SetAudioSoundMode(TvControlManager.Sound_Mode.SOUND_MODE_STD);
            mTvControlManager.SaveCurAudioSoundMode(TvControlManager.Sound_Mode.SOUND_MODE_STD.toInt());
        } else if (mode == 1) {
            mTvControlManager.SetAudioSoundMode(TvControlManager.Sound_Mode.SOUND_MODE_MUSIC);
            mTvControlManager.SaveCurAudioSoundMode(TvControlManager.Sound_Mode.SOUND_MODE_MUSIC.toInt());
        } else if (mode == 2) {
            mTvControlManager.SetAudioSoundMode(TvControlManager.Sound_Mode.SOUND_MODE_NEWS);
            mTvControlManager.SaveCurAudioSoundMode(TvControlManager.Sound_Mode.SOUND_MODE_NEWS.toInt());
        } else if (mode == 3) {
            mTvControlManager.SetAudioSoundMode(TvControlManager.Sound_Mode.SOUND_MODE_THEATER);
            mTvControlManager.SaveCurAudioSoundMode(TvControlManager.Sound_Mode.SOUND_MODE_THEATER.toInt());
        } else if (mode == 4) {
            mTvControlManager.SetAudioSoundMode(TvControlManager.Sound_Mode.SOUND_MODE_USER);
            mTvControlManager.SaveCurAudioSoundMode(TvControlManager.Sound_Mode.SOUND_MODE_USER.toInt());
        }
    }

    public void setTreble (int step) {
        if (CanDebug()) Log.d(TAG, "setTreble = " + step);
        int treble_value = mTvControlManager.GetCurAudioTrebleVolume() + step;

        int bass_value = -1;
        if (mTvControlManager.GetCurAudioSoundMode() != 4)
            bass_value = mTvControlManager.GetCurAudioBassVolume();

        if (treble_value >= 0 && treble_value <= 100) {
            mTvControlManager.SetAudioTrebleVolume(treble_value);
            mTvControlManager.SaveCurAudioTrebleVolume(treble_value);
        }

        if (bass_value != -1) {
            mTvControlManager.SetAudioBassVolume(bass_value);
            mTvControlManager.SaveCurAudioBassVolume(bass_value);
        }
    }

    public void setBass (int step) {
        if (CanDebug()) Log.d(TAG, "setBass = " + step);
        int bass_value = mTvControlManager.GetCurAudioBassVolume() + step;

        int treble_value = -1;
        if (mTvControlManager.GetCurAudioSoundMode() != 4)
            treble_value = mTvControlManager.GetCurAudioTrebleVolume();

        if (bass_value >= 0 && bass_value <= 100) {
            mTvControlManager.SetAudioBassVolume(bass_value);
            mTvControlManager.SaveCurAudioBassVolume(bass_value);
        }

        if (treble_value != -1) {
            mTvControlManager.SetAudioTrebleVolume(treble_value);
            mTvControlManager.SaveCurAudioTrebleVolume(treble_value);
        }
    }

    public void setBalance (int step) {
        if (CanDebug()) Log.d(TAG, "setBalance = " + step);
        int balance_value = mTvControlManager.GetCurAudioBalance() + step;
        if (balance_value >= 0 && balance_value <= 100) {
            mTvControlManager.SetAudioBalance(balance_value);
            mTvControlManager.SaveCurAudioBalance(balance_value);
        }
    }

    public void setSpdif (int mode) {
        if (CanDebug()) Log.d(TAG, "setSpdif = " + mode);
        if (mode == 0) {
            mTvControlManager.SetAudioSPDIFSwitch(0);
            mTvControlManager.SaveCurAudioSPDIFSwitch(0);
        } else if (mode == 1) {
            mTvControlManager.SetAudioSPDIFSwitch(1);
            mTvControlManager.SaveCurAudioSPDIFSwitch(1);
            mTvControlManager.SetAudioSPDIFMode(0);
            mTvControlManager.SaveCurAudioSPDIFMode(0);
            sendBroadcastToLiveTv("audio.replay");
        } else if (mode == 2) {
            mTvControlManager.SetAudioSPDIFSwitch(1);
            mTvControlManager.SaveCurAudioSPDIFSwitch(1);
            mTvControlManager.SetAudioSPDIFMode(1);
            mTvControlManager.SaveCurAudioSPDIFMode(1);
            sendBroadcastToLiveTv("audio.replay");
        }
    }

    private void sendBroadcastToLiveTv(String extra) {
        Intent intent = new Intent(DroidLogicTvUtils.ACTION_UPDATE_TV_PLAY);
        intent.putExtra("tv_play_extra", extra);
        mContext.sendBroadcast(intent);
    }

    public void setSurround (int mode) {
        if (CanDebug()) Log.d(TAG, "setSurround = " + mode);
        if (mode == 1) {
            mTvControlManager.SetAudioSrsSurround(1);
            mTvControlManager.SaveCurAudioSrsSurround(1);
        } else if (mode == 0) {
            setDialogClarity(mode);
            setBassBoost(mode);
            mTvControlManager.SetAudioSrsSurround(0);
            mTvControlManager.SaveCurAudioSrsSurround(0);
        }
    }

    public void setVirtualSurround (int mode) {
        if (CanDebug()) Log.d(TAG, "setVirtualSurround = " + mode);
        if (mode == 1) {
            mTvControlManager.SetAudioVirtualizer(1,50);
        } else if (mode == 0) {
            mTvControlManager.SetAudioVirtualizer(0,50);
        }
    }

    public void setVirtualSurroundLevel(int step){
        if (CanDebug()) Log.d(TAG, "setVirtualSurroundLevel = " + step);
        int level = mTvControlManager.GetAudioVirtualizerLevel() + step;
        if (level >= 0 && level <= 100) {
            mTvControlManager.SetAudioVirtualizer(1, level);
        }
    }

    public void setDialogClarity (int mode) {
        if (CanDebug()) Log.d(TAG, "setDialogClarity = " + mode);
        if (mode == 1) {
            setSurround(1);
            mTvControlManager.SetAudioSrsDialogClarity(1);
            mTvControlManager.SaveCurAudioSrsDialogClarity(1);
        } else if (mode == 0) {
            mTvControlManager.SetAudioSrsDialogClarity(0);
            mTvControlManager.SaveCurAudioSrsDialogClarity(0);
        }
    }

    public void setBassBoost (int mode) {
        if (CanDebug()) Log.d(TAG, "setBassBoost = " + mode);
        if (mode == 1) {
            setSurround(1);
            mTvControlManager.SetAudioSrsTruBass(1);
            mTvControlManager.SaveCurAudioSrsTruBass(1);
        } else if (mode == 0) {
            mTvControlManager.SetAudioSrsTruBass(0);
            mTvControlManager.SaveCurAudioSrsTruBass(0);;
        }
    }

    public int getDtvTypeStatus () {
        String type = getDtvType();
        int ret = SET_ATSC_T;
        if (type != null) {
            if (CanDebug()) Log.d(TAG, "getDtvTypeStatus = " + type);
            if (TextUtils.equals(type, TvContract.Channels.TYPE_DTMB)) {
                    ret = SET_DTMB;
            } else if (TextUtils.equals(type, TvContract.Channels.TYPE_DVB_C)) {
                    ret = SET_DVB_C;
            } else if (TextUtils.equals(type, TvContract.Channels.TYPE_DVB_T)) {
                    ret = SET_DVB_T;
            } else if (TextUtils.equals(type, TvContract.Channels.TYPE_DVB_T2)) {
                    ret = SET_DVB_T2;
            } else if (TextUtils.equals(type, TvContract.Channels.TYPE_ATSC_T)) {
                    ret = SET_ATSC_T;
            } else if (TextUtils.equals(type, TvContract.Channels.TYPE_ATSC_C)) {
                    ret = SET_ATSC_C;
            } else if (TextUtils.equals(type, TvContract.Channels.TYPE_ISDB_T)) {
                    ret = SET_ISDB_T;
            }
            return ret;
        } else {
            ret = -1;
            return ret;
        }
    }

    public String getDtvType() {
        String type = Settings.System.getString(mContext.getContentResolver(),
            DroidLogicTvUtils.TV_KEY_DTV_TYPE);
        return type;
    }

    public int getSoundChannelStatus () {
        int type = 0;
        if (mCurrentChannel != null) {
            type = mCurrentChannel.getAudioChannel();
        }
        if (type < 0 || type > 2) {
            type = 0;
        }
        if (CanDebug()) Log.d(TAG, "getSoundChannelStatus = " + type);
        return type;
    }

    public ArrayList<HashMap<String, String>> getChannelInfoStatus() {
        ArrayList<HashMap<String, String>> list =  new ArrayList<HashMap<String, String>>();
        TvControlManager.SourceInput_Type tvSource = DroidLogicTvUtils.parseTvSourceTypeFromDeviceId(mDeviceId);
        TvControlManager.SourceInput_Type virtualTvSource = tvSource;
        if (tvSource == TvControlManager.SourceInput_Type.SOURCE_TYPE_ADTV) {
            if (mCurrentChannel != null) {
                tvSource = DroidLogicTvUtils.parseTvSourceTypeFromSigType(DroidLogicTvUtils.getSigType(mCurrentChannel));
            }
            if (virtualTvSource == tvSource) {//no channels in adtv input, DTV for default.
                tvSource = TvControlManager.SourceInput_Type.SOURCE_TYPE_DTV;
            }
        }
        if (mCurrentChannel != null) {
            HashMap<String, String> item = new HashMap<String, String>();
            item.put(STRING_NAME, mResources.getString(R.string.channel_info_channel));
            item.put(STRING_STATUS, mCurrentChannel.getDisplayNameLocal());
            list.add(item);

            item = new HashMap<String, String>();
            item.put(STRING_NAME, mResources.getString(R.string.channel_info_frequency));
            item.put(STRING_STATUS, Integer.toString(mCurrentChannel.getFrequency() + mCurrentChannel.getFineTune()));
            list.add(item);

            if (tvSource == TvControlManager.SourceInput_Type.SOURCE_TYPE_DTV) {
                item = new HashMap<String, String>();
                item.put(STRING_NAME, mResources.getString(R.string.channel_info_type));
                item.put(STRING_STATUS, mCurrentChannel.getType());
                list.add(item);

                item = new HashMap<String, String>();
                item.put(STRING_NAME, mResources.getString(R.string.channel_info_service_id));
                item.put(STRING_STATUS, Integer.toString(mCurrentChannel.getServiceId()));
                list.add(item);

                item = new HashMap<String, String>();
                item.put(STRING_NAME, mResources.getString(R.string.channel_info_pcr_id));
                item.put(STRING_STATUS, Integer.toString(mCurrentChannel.getPcrPid()));
                list.add(item);
            }
        }
        return list;
    }

    public int getMenuTimeStatus () {
        int type = Settings.System.getInt(mContext.getContentResolver(), KEY_MENU_TIME, DEFUALT_MENU_TIME);
        if (CanDebug()) Log.d(TAG, "getMenuTimeStatus = " + type);
        if (type == 10) {
            type = 0;
        }
        return type;
    }

    public int getSleepTimerStatus () {
        String ret = "";
        int time = mSystemControlManager.getPropertyInt("tv.sleep_timer", 0);
        Log.d(TAG, "getSleepTimerStatus:" + time);
        return time;
    }

    //0 1 ~ luncher livetv
    public int getStartupSettingStatus () {
        int type = Settings.System.getInt(mContext.getContentResolver(), DroidLogicTvUtils.TV_START_UP_ENTER_APP, 0);
        if (CanDebug()) Log.d(TAG, "getStartupSettingStatus = " + type);
        if (type != 0) {
            type = 1;
        }
        return type;
    }

    public int getAutoSyncTVTimeStatus () {
        int type = Settings.System.getInt(mContext.getContentResolver(), DTV_AUTOSYNC_TVTIME, 0);
        if (CanDebug()) Log.d(TAG, "getAutoSyncTVTimeStatus = " + type);
        if (type != 0) {
            type = 1;
        }
        return type;
    }

    // 0 1 ~ off on
    public int getDynamicBacklightStatus () {
        int switchVal = Settings.System.getInt(mContext.getContentResolver(), KEY_DYNAMIC_BACKLIGHT, 0);
        if (CanDebug()) Log.d(TAG, "getDynamicBacklightStatus = " + switchVal);
        if (switchVal != 0) {
            switchVal = 1;
        }
        return switchVal;
    }

    // 0 1 ~ off on others on
    public int getADSwitchStatus () {
        int switchVal = Settings.System.getInt(mContext.getContentResolver(), DroidLogicTvUtils.TV_KEY_AD_SWITCH, 0);
        if (CanDebug()) Log.d(TAG, "getADSwitchStatus = " + switchVal);
        if (switchVal != 0) {
            switchVal = 1;
        }
        return switchVal;
    }

    public int getVolumeCompensateStatus () {
        int value = 0;
        if (mCurrentChannel != null)
            value = mCurrentChannel.getAudioCompensation();
        else
            value = 0;
        return value;
    }

    public int getSwitchChannelStatus () {
        if (mTvControlManager.SSMReadBlackoutEnalbe() == 0)
            return 0;
        else
            return 1;
    }

    public int getADMixStatus () {
        int val = Settings.System.getInt(mContext.getContentResolver(), DroidLogicTvUtils.TV_KEY_AD_MIX, 50);
        if (CanDebug()) Log.d(TAG, "getADMixStatus = " + val);
        return val;
    }

    public int[] getFourHdmi20Status() {
        int[] fourhdmistatus = new int[4];
        TvControlManager.HdmiPortID[] allport = {TvControlManager.HdmiPortID.HDMI_PORT_1, TvControlManager.HdmiPortID.HDMI_PORT_2,
            TvControlManager.HdmiPortID.HDMI_PORT_3, TvControlManager.HdmiPortID.HDMI_PORT_4};
        for (int i = 0; i < allport.length; i++) {
            if (mTvControlManager.GetHdmiEdidVersion(allport[i]) == TvControlManager.HdmiEdidVer.HDMI_EDID_VER_20.toInt()) {
                fourhdmistatus[i] = 1;
            } else {
                fourhdmistatus[i] = 0;
            }
        }
        if (CanDebug()) Log.d(TAG, "getFourHdmi20Status 1 to 4 " + fourhdmistatus[0] + ", " + fourhdmistatus[1] + ", " + fourhdmistatus[2] + ", " + fourhdmistatus[3]);
        return fourhdmistatus;
    }

    public int getCurrentHdmiNo() {
        return getHdmiNo(mDeviceId);
    }

    private int getHdmiNo(int id) {
        int ret = -1;
        switch (id) {
            case DroidLogicTvUtils.DEVICE_ID_HDMI1:
                ret = 0;
                break;
            case DroidLogicTvUtils.DEVICE_ID_HDMI2:
                ret = 1;
                break;
            case DroidLogicTvUtils.DEVICE_ID_HDMI3:
                ret = 2;
                break;
            case DroidLogicTvUtils.DEVICE_ID_HDMI4:
                ret = 3;
                break;
            default:
                ret = -1;
                break;
        }
        return ret;
    }

    public void setDtvType (int value) {
        if (CanDebug()) Log.d(TAG, "setDtvType = " + value);
        String type = null;
        switch (value) {
            case SET_DTMB:
                type = TvContract.Channels.TYPE_DTMB;
                break;
            case SET_DVB_C:
                type = TvContract.Channels.TYPE_DVB_C;
                break;
            case SET_DVB_T:
                type = TvContract.Channels.TYPE_DVB_T;
                break;
            case SET_DVB_T2:
                type = TvContract.Channels.TYPE_DVB_T2;
                break;
            case SET_ATSC_T:
                type = TvContract.Channels.TYPE_ATSC_T;
                break;
            case SET_ATSC_C:
                type = TvContract.Channels.TYPE_ATSC_C;
                break;
            case SET_ISDB_T:
                type = TvContract.Channels.TYPE_ISDB_T;
                break;
        }
        if (type != null) {
            Settings.System.putString(mContext.getContentResolver(), DroidLogicTvUtils.TV_KEY_DTV_TYPE, type);
        }
    }

    public void setSoundChannel (int type) {
        if (CanDebug()) Log.d(TAG, "setSoundChannel = " + type);
        if (mCurrentChannel != null) {
            mCurrentChannel.setAudioChannel(type);
            mTvDataBaseManager.updateChannelInfo(mCurrentChannel);
            mTvControlManager.DtvSetAudioChannleMod(mCurrentChannel.getAudioChannel());
        }
    }

    public void setStartupSetting (int type) {
        if (CanDebug()) Log.d(TAG, "setStartupSetting = " + type);
        Settings.System.putInt(mContext.getContentResolver(), DroidLogicTvUtils.TV_START_UP_ENTER_APP, type);
    }

    public void setAutoSyncTVTime (int type) {
        if (CanDebug()) Log.d(TAG, "setAutoSyncTVTime = " + type);
        Settings.System.putInt(mContext.getContentResolver(), DTV_AUTOSYNC_TVTIME, type);
        if (type == 0) {
            mSystemControlManager.setProperty("persist.sys.getdtvtime.isneed", "false");
            Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.AUTO_TIME, 0);
            Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.AUTO_TIME, 1);
        } else if (type == 1) {
            mSystemControlManager.setProperty("persist.sys.getdtvtime.isneed", "true");
        }
    }

    public void setMenuTime (int type) {
        if (CanDebug()) Log.d(TAG, "setMenuTime = " + type);
        Settings.System.putInt(mContext.getContentResolver(), KEY_MENU_TIME, type);
    }

    public void setSleepTimer (int mode) {
        AlarmManager alarm = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0,
                new Intent("droidlogic.intent.action.TIMER_SUSPEND"), 0);
        alarm.cancel(pendingIntent);

        mSystemControlManager.setProperty("tv.sleep_timer", mode+"");

        long timeout = 0;
        if (mode == 0) {
            return;
        } else if (mode < 5) {
            timeout = (mode * 15  - 1) * 60 * 1000;
        } else {
            timeout = ((mode - 4) * 30 + 4 * 15  - 1) * 60 * 1000;
        }

        alarm.setExact(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + timeout, pendingIntent);
        Log.d(TAG, "start time count down after " + timeout + " ms");
    }


    public void setAutoBacklightStatus(int value) {
        if (CanDebug()) Log.d(TAG, "setAutoBacklightStatus = " + value);
        Settings.System.putInt(mContext.getContentResolver(), KEY_DYNAMIC_BACKLIGHT, value);
        if (value == 0) {
            mTvControlManager.stopAutoBacklight();
        } else {
            mTvControlManager.startAutoBacklight();
        }
    }

    public void setAudioADSwitch (int switchVal) {
        if (CanDebug()) Log.d(TAG, "setAudioADSwitch = " + switchVal);
        Settings.System.putInt(mContext.getContentResolver(), DroidLogicTvUtils.TV_KEY_AD_SWITCH, switchVal);
        Intent intent = new Intent(DroidLogicTvUtils.ACTION_AD_SWITCH);
        intent.putExtra(DroidLogicTvUtils.EXTRA_SWITCH_VALUE, switchVal);
        mContext.sendBroadcast(intent);
    }

    public void setVolumeCompensate (int value) {
        if (mCurrentChannel != null) {
            int current = mCurrentChannel.getAudioCompensation();
            int offset = 0;
            if (value > current) {
                offset = 1;
            } else if (value < current) {
                offset = -1;
            }
            if ((current < 20 && offset > 0)
                || (current > -20 && offset < 0)) {
                mCurrentChannel.setAudioCompensation(current + offset);
                mTvDataBaseManager.updateChannelInfo(mCurrentChannel);
                mTvControlManager.SetCurProgVolumeCompesition(mCurrentChannel.getAudioCompensation());
            }
        }
    }

    public void setBlackoutEnable(int status) {
        mTvControlManager.setBlackoutEnable(status);
    }

    public void setADMix (int step) {
        if (CanDebug()) Log.d(TAG, "setADMix = " + step);
        int level = getADMixStatus() + step;
        if (level <= 100 && level >= 0) {
            Settings.System.putInt(mContext.getContentResolver(), DroidLogicTvUtils.TV_KEY_AD_MIX, level);
            Intent intent = new Intent(DroidLogicTvUtils.ACTION_AD_MIXING_LEVEL);
            intent.putExtra(DroidLogicTvUtils.PARA_VALUE1, level);
            mContext.sendBroadcast(intent);
        }
    }

    public void doFactoryReset() {
        if (CanDebug()) Log.d(TAG, "doFactoryReset");
        mTvControlManager.StopTv();
        setStartupSetting(0);
        setAudioADSwitch(0);
        setDefAudioStreamVolume();
        clearHdmi20Mode();
        // SystemControlManager mSystemControlManager = new SystemControlManager(mContext);
        // mSystemControlManager.setBootenv("ubootenv.var.upgrade_step", "1");
        final String[] tvPackages = {"com.android.providers.tv"};
        for (int i = 0; i < tvPackages.length; i++) {
            ClearPackageData(tvPackages[i]);
        }
        mTvControlManager.stopAutoBacklight();
        mTvControlManager.SSMInitDevice();
        mTvControlManager.FactoryCleanAllTableForProgram();
    }

    private void setDefAudioStreamVolume() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = SystemProperties.getInt("ro.config.media_vol_steps", 100);
        int streamMaxVolume = audioManager.getStreamMaxVolume(AudioSystem.STREAM_MUSIC);
        int defaultVolume = maxVolume == streamMaxVolume ? (maxVolume * 3) / 10 : (streamMaxVolume * 3) / 4;
        audioManager.setStreamVolume(AudioSystem.STREAM_MUSIC, defaultVolume, 0);
    }

    private  void ClearPackageData(String packageName) {
        Log.d(TAG, "ClearPackageData:" + packageName);
        //clear data
        ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ClearUserDataObserver mClearDataObserver = new ClearUserDataObserver();
        boolean res = am.clearApplicationUserData(packageName, mClearDataObserver);
        if (!res) {
            Log.i(TAG, " clear " + packageName + " data failed");
        } else {
            Log.i(TAG, " clear " + packageName + " data succeed");
        }

        //clear cache
        PackageManager packageManager = mContext.getPackageManager();
        ClearUserDataObserver mClearCacheObserver = new ClearUserDataObserver();
        packageManager.deleteApplicationCacheFiles(packageName, mClearCacheObserver);

        //clear default
        packageManager.clearPackagePreferredActivities(packageName);
    }

    private class ClearUserDataObserver extends IPackageDataObserver.Stub {
        public void onRemoveCompleted(final String packageName, final boolean succeeded) {
        }
    }

    public void doFbcUpgrade() {
        Log.d(TAG, "doFactoryReset need add");
    }

    public void setHdmi20Mode(int order, int mode) {
        if (CanDebug()) Log.d(TAG, "setHdmi20Mode order = " + order + ", mode = " + mode);
        TvControlManager.HdmiPortID[] allport = {TvControlManager.HdmiPortID.HDMI_PORT_1, TvControlManager.HdmiPortID.HDMI_PORT_2,
            TvControlManager.HdmiPortID.HDMI_PORT_3, TvControlManager.HdmiPortID.HDMI_PORT_4};
        if (order < 0 || order > 3) {
            Log.d(TAG, "setHdmi20Mode device id erro");
            return;
        }
        if (mode == 1) {
            // set HDMI mode sequence: save than set
            mTvControlManager.SaveHdmiEdidVersion(allport[order],
                TvControlManager.HdmiEdidVer.HDMI_EDID_VER_20);
            mTvControlManager.SetHdmiEdidVersion(allport[order],
                TvControlManager.HdmiEdidVer.HDMI_EDID_VER_20);
        } else {
            mTvControlManager.SaveHdmiEdidVersion(allport[order],
                TvControlManager.HdmiEdidVer.HDMI_EDID_VER_14);
            mTvControlManager.SetHdmiEdidVersion(allport[order],
                TvControlManager.HdmiEdidVer.HDMI_EDID_VER_14);
        }
    }

    public void clearHdmi20Mode() {
        Log.d(TAG, "reset Hdmi20Mode status");
        TvControlManager.HdmiPortID[] allport = {TvControlManager.HdmiPortID.HDMI_PORT_1, TvControlManager.HdmiPortID.HDMI_PORT_2,
                TvControlManager.HdmiPortID.HDMI_PORT_3, TvControlManager.HdmiPortID.HDMI_PORT_4};

        for (int i = 0; i  < allport.length; i++) {
            mTvControlManager.SaveHdmiEdidVersion(allport[i],
                    TvControlManager.HdmiEdidVer.HDMI_EDID_VER_14);
        }
    }

    public void setDaylightSavingTime(int value) {
        mDaylightSavingTime.setDaylightSavingTime(value);
    }

    public int getDaylightSavingTime() {
        return mDaylightSavingTime.getDaylightSavingTime();
    }
}

