/*
 * Copyright (c) 2014 Amlogic, Inc. All rights reserved.
 *
 * This source code is subject to the terms and conditions defined in the
 * file 'LICENSE' which is part of this source code package.
 *
 * Description:
 *     AMLOGIC SetEffectReceiver
 */

package com.droidlogic.tv.soundeffectsettings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
//import android.os.Process;
import android.widget.Toast;
import android.util.Log;
import android.content.ContentResolver;
import com.droidlogic.app.AudioOutputManager;

public class SetEffectReceiver extends BroadcastReceiver {
    static final String TAG = "SetEffectReceiver";
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    private AudioOutputManager mAudioOutputManager;
    @Override
    public void onReceive (Context context, Intent intent) {
        Log.d(TAG, "onReceive = " + intent);
        if (intent.getAction().equalsIgnoreCase(ACTION)) {
            initAudioEffectService(context);//only init sound effect if package exist
        }
    }

    private void initAudioEffectService(Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "initAudioEffectService = " + context);
                mAudioOutputManager = new AudioOutputManager(context);
                mAudioOutputManager.initSoundParametersAfterBoot();
            }
        }).start();
    }
}


