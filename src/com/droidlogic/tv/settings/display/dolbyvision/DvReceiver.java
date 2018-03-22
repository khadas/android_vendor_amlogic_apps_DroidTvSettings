package com.droidlogic.tv.settings.display.dolbyvision;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//import android.os.Process;
import android.widget.Toast;

public class DvReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive (Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(ACTION)) {
            Intent serviceIntent = new Intent(context, DolbyVisionService.class);
            context.startService(serviceIntent);
        }
    }

}


