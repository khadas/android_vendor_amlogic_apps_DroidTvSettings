/*
 * Copyright (c) 2014 Amlogic, Inc. All rights reserved.
 *
 * This source code is subject to the terms and conditions defined in the
 * file 'LICENSE' which is part of this source code package.
 *
 * Description:
 *     AMLOGIC BluetoothAutoPairReceiver
 */

package com.droidlogic.tv.settings;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.SystemProperties;
import android.content.BroadcastReceiver;

public class BluetoothAutoPairReceiver extends BroadcastReceiver {
    private static final String TAG = "BluetoothAutoPairReceiver";
    private static final boolean DEBUG = true;

    private void Log(String msg) {
        if (DEBUG) {
            Log.i(TAG, msg);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Log("Received ACTION_BOOT_COMPLETED");
            if (SystemProperties.get("ro.autoconnectbt.isneed").equals("true")) {
                Log("Need autoconnectBT!");
                Intent serviceintent = new Intent(context, BluetoothAutoPairService.class);
                context.startService(serviceintent);
            } else {
                Log("No need autoconnectBT!");
            }
        }
    }
}
