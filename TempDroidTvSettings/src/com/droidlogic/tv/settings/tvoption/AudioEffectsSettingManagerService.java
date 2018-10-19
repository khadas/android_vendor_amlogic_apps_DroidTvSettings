/*
 * Copyright (c) 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * PROPRIETARY/CONFIDENTIAL
 *
 * Use is subject to license terms.
 */
package com.droidlogic.tv.settings.tvoption;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.IBinder;
import android.os.UserHandle;
import android.os.Binder;

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

    // Service actions
    public static final String ACTION_STARTUP = "com.droidlogic.tv.settings.AudioEffectsSettingManagerService.STARTUP";

    public AudioEffectsSettingManagerService() {
        super("AudioEffectsSettingManagerService");
        mAudioEffectsSettingManagerService = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSoundEffectSettingManager = new SoundEffectSettingManager(this);
        if (DEBUG) Log.d(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSoundEffectSettingManager != null) {
            mSoundEffectSettingManager.cleanupAudioEffects();
        }
        if (DEBUG) Log.d(TAG, "onDestroy");

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
        context.startServiceAsUser(intent, UserHandle.OWNER);
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
}
