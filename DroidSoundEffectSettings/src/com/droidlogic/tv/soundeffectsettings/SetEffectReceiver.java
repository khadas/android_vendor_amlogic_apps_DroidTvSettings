package com.droidlogic.tv.soundeffectsettings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
//import android.os.Process;
import android.widget.Toast;
import android.util.Log;
import android.content.ContentResolver;

public class SetEffectReceiver extends BroadcastReceiver {
    static final String TAG = "SetEffectReceiver";
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

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
                AudioEffectsSettingManagerService.startActionStartup(context);
            }
        }).start();
    }
}


