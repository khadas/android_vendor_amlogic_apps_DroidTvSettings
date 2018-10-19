package com.droidlogic.tv.settings.display.dolbyvision;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
//import android.os.Process;
import android.widget.Toast;
import android.os.SystemProperties;
import android.util.Log;
import android.content.ContentResolver;

import com.droidlogic.app.tv.TvControlDataManager;

import com.droidlogic.tv.settings.tvoption.TvOptionSettingManager;
import com.droidlogic.tv.settings.tvoption.AudioEffectsSettingManagerService;
import com.droidlogic.tv.settings.tvoption.SoundParameterSettingManager;

public class DvReceiver extends BroadcastReceiver {
    static final String TAG = "DvReceiver";
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive (Context context, Intent intent) {
        Log.d(TAG, "onReceive = " + intent);
        if (intent.getAction().equalsIgnoreCase(ACTION)) {
            if (SystemProperties.getBoolean("ro.platform.has.tvuimode", false)) {
                initAudioEffectService(context);
            } else {
                SoundParameterSettingManager sound = new SoundParameterSettingManager(context);
                sound.initParameterAfterBoot();
            }
            Intent serviceIntent = new Intent(context, DolbyVisionService.class);
            context.startService(serviceIntent);
            checkTvControlDataProvider(context);
        }
    }

    private void checkTvControlDataProvider(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "checkTvControlDataProvider = " + context);
                TvControlDataManager tvcontroldata = TvControlDataManager.getInstance(context);
                ContentResolver content = context.getContentResolver();
                if (!tvcontroldata.getBoolean(content, TvControlDataManager.KEY_INIT, false)) {
                    //init the tv_control_data.db if not exist
                    tvcontroldata.putBoolean(content, TvControlDataManager.KEY_INIT, true);
                }
            }
        }).start();
    }

    private void initAudioEffectService(Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "initAudioEffectService = " + context);
                AudioEffectsSettingManagerService.startActionStartup(context);
            }
        }).start();
    }
}


