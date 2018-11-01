package com.droidlogic.tv.settings.tvoption;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.SystemProperties;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;

import com.droidlogic.app.tv.TvControlDataManager;
import com.droidlogic.app.SystemControlManager;
import com.droidlogic.tv.settings.tvoption.SoundParameterSettingManager;

public class SoundSetReceiver extends BroadcastReceiver {
    private static final String TAG = "SoundSetReceiver";
    private static final boolean DEBUG = true;
    private static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive (Context context, Intent intent) {
        if (DEBUG) Log.d(TAG, "onReceive = " + intent);
        if (intent.getAction().equalsIgnoreCase(ACTION)) {
            SoundParameterSettingManager sound = new SoundParameterSettingManager(context);
            sound.initParameterAfterBoot();
            SystemControlManager mSystenControlManager = SystemControlManager.getInstance();
            final boolean istv = mSystenControlManager.getPropertyBoolean("ro.vendor.platform.has.tvuimode", false);
            if (istv) {
                checkTvControlDataProvider(context);
            }
            //set sound effect in com.droidlogic.tv.soundeffectsettings
        }
    }

    private void checkTvControlDataProvider(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (DEBUG) Log.d(TAG, "checkTvControlDataProvider = " + context);
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
