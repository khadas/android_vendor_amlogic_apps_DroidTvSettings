/*
 * Copyright (c) 2014 Amlogic, Inc. All rights reserved.
 *
 * This source code is subject to the terms and conditions defined in the
 * file 'LICENSE' which is part of this source code package.
 *
 * Description:
 *     AMLOGIC AudioEffectsSettingManagerService
 */

package com.droidlogic.tv.soundeffectsettings;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.IBinder;
import android.os.UserHandle;
import android.os.Binder;
import android.os.Handler;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.database.ContentObserver;
import android.provider.Settings;
import android.net.Uri;

/**
 * This Service modifies Audio and Picture Quality TV Settings.
 * It contains platform specific implementation of the TvTweak IOemSettings interface.
 */
public class AudioEffectsSettingManagerService extends PersistentService {
    private static final String TAG = AudioEffectsSettingManagerService.class.getSimpleName();
    private static boolean DEBUG = true;
    private MyBinder mMyBinder = new MyBinder();
    private SoundEffectSettingManager mSoundEffectSettingManager;
    private AudioEffectsSettingManagerService mAudioEffectsSettingManagerService;
    private Context mContext = null;

    // Service actions
    public static final String ACTION_STARTUP = "com.droidlogic.tv.settings.AudioEffectsSettingManagerService.STARTUP";

    public static final String SOUND_EFFECT_SOUND_MODE            = "sound_effect_sound_mode";
    public static final String SOUND_EFFECT_SOUND_MODE_TYPE       = "sound_effect_sound_mode_type";
    public static final String SOUND_EFFECT_SOUND_MODE_TYPE_DAP   = "type_dap";
    public static final String SOUND_EFFECT_SOUND_MODE_TYPE_EQ    = "type_eq";
    public static final String SOUND_EFFECT_SOUND_MODE_DAP_VALUE  = "sound_effect_sound_mode_dap";
    public static final String SOUND_EFFECT_SOUND_MODE_EQ_VALUE   = "sound_effect_sound_mode_eq";
    public static final int MODE_STANDARD = 0;

    public AudioEffectsSettingManagerService() {
        super("AudioEffectsSettingManagerService");
        mAudioEffectsSettingManagerService = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG) Log.d(TAG, "onCreate");
        mContext = this;
        mSoundEffectSettingManager = new SoundEffectSettingManager(this);
        registerCommandReceiver(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) Log.d(TAG, "onDestroy");
        if (mSoundEffectSettingManager != null) {
            mSoundEffectSettingManager.cleanupAudioEffects();
        }
        unregisterCommandReceiver(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (DEBUG) Log.d(TAG, "onLowMemory");
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (DEBUG) Log.d(TAG, "onBind");
        return mMyBinder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        final String action = intent.getAction();
        if (ACTION_STARTUP.equals(action)) {
            if (DEBUG) Log.d(TAG, "processing " + ACTION_STARTUP);
            handleActionStartUp();
        } else {
            Log.w(TAG, "Unknown intent: " + action);
        }
    }

    /**
     * Starts this service to perform ACTION_STARTUP.
     *
     * @param context
     */
    public static void startActionStartup(Context context) {
        Log.d(TAG, "startActionStartup");
        Intent intent = new Intent(context, AudioEffectsSettingManagerService.class);
        intent.setAction(ACTION_STARTUP);
        context.startService(intent);
    }


    private void handleActionStartUp() {
        // This will apply the saved audio settings on boot
        mSoundEffectSettingManager.initSoundEffectSettings();
    }

    public class MyBinder extends Binder{
        public AudioEffectsSettingManagerService getService(){
            return mAudioEffectsSettingManagerService;
        }
    }

    public SoundEffectSettingManager getSoundEffectSettingManager() {
       return mSoundEffectSettingManager;
    }

    private static final String RESET_ACTION = "droid.action.resetsoundeffect";
    private static final String AVL_SOURCE_ACTION = "droid.action.avlmodule";

    private void registerCommandReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RESET_ACTION);
        intentFilter.addAction(AVL_SOURCE_ACTION);
        context.registerReceiver(mSoundEffectSettingsReceiver, intentFilter);
        context.getContentResolver().registerContentObserver(Settings.Global.getUriFor(SOUND_EFFECT_SOUND_MODE), false,
                mSoundEffectParametersObserver);
        context.getContentResolver().registerContentObserver(Settings.Global.getUriFor(SOUND_EFFECT_SOUND_MODE_EQ_VALUE), false,
                mSoundEffectParametersObserver);
        context.getContentResolver().registerContentObserver(Settings.Global.getUriFor(SOUND_EFFECT_SOUND_MODE_DAP_VALUE), false,
                mSoundEffectParametersObserver);
    }

    private void unregisterCommandReceiver(Context context) {
        context.unregisterReceiver(mSoundEffectSettingsReceiver);
        context.getContentResolver().unregisterContentObserver(mSoundEffectParametersObserver);
    }

    private ContentObserver mSoundEffectParametersObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (mSoundEffectSettingManager != null && uri != null) {
                if (uri.equals(Settings.Global.getUriFor(SOUND_EFFECT_SOUND_MODE)) || uri.equals(Settings.Global.getUriFor(SOUND_EFFECT_SOUND_MODE_EQ_VALUE))
                        || uri.equals(Settings.Global.getUriFor(SOUND_EFFECT_SOUND_MODE_DAP_VALUE))) {
                    int mode = Settings.Global.getInt(mContext.getContentResolver(), uri.getLastPathSegment(), MODE_STANDARD);
                    Log.d(TAG, "onChange setSoundMode " + uri.getLastPathSegment() + ":" + mode);
                    mSoundEffectSettingManager.setSoundModeByObserver(mode);
                }
            }
        }
    };

    private final BroadcastReceiver mSoundEffectSettingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.d(TAG, "intent = " + intent);
            if (intent != null) {
                if (RESET_ACTION.equals(intent.getAction())) {
                    mSoundEffectSettingManager.resetSoundEffectSettings();
                } else if (AVL_SOURCE_ACTION.equals(intent.getAction())) {
                    mSoundEffectSettingManager.setSourceIdForAvl(intent.getIntExtra("source_id", SoundEffectSettingManager.DEFAULT_AGC_SOURCE_ID));
                }
            }
        }
    };
}
