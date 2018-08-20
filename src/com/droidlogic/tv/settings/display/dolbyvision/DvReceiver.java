package com.droidlogic.tv.settings.display.dolbyvision;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//import android.os.Process;
import android.widget.Toast;
import android.util.Log;
import android.content.ContentResolver;

import com.droidlogic.app.tv.TvControlDataManager;

public class DvReceiver extends BroadcastReceiver {
    //static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    private static final String TAG = "DvReceiver";

    @Override
    public void onReceive (Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
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
}


