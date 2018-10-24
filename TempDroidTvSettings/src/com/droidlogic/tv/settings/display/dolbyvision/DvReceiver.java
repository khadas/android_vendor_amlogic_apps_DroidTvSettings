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

public class DvReceiver extends BroadcastReceiver {
    static final String TAG = "DvReceiver";
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive (Context context, Intent intent) {
        Log.d(TAG, "onReceive = " + intent);
        if (intent.getAction().equalsIgnoreCase(ACTION)) {
            Intent serviceIntent = new Intent(context, DolbyVisionService.class);
            context.startService(serviceIntent);
        }
    }
}


