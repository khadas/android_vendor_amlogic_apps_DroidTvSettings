package com.droidlogic.tv.settings.display.outputmode;

import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.dialog.old.Action;
import com.droidlogic.tv.settings.dialog.old.ActionAdapter;
import com.droidlogic.tv.settings.dialog.old.ActionFragment;
import com.droidlogic.tv.settings.dialog.old.ContentFragment;
import com.droidlogic.tv.settings.dialog.old.DialogActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ColorAttributeActivity extends DialogActivity implements ActionAdapter.Listener{

    private static final String LOG_TAG = "ColorAttributeActivity";
    private ContentFragment mContentFragment;
    private ActionFragment mActionFragment;
    private OutputUiManager mOutputUiManager;
    private static String saveValue = null;
    private static String curValue = null;
    private static String curMode = null;
    private static final int MSG_FRESH_UI = 0;
    private IntentFilter mIntentFilter;

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mHandler.sendEmptyMessageDelayed(MSG_FRESH_UI, 1000);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mOutputUiManager = new OutputUiManager(this);
        mContentFragment = createMainMenuContentFragment();
        mActionFragment = ActionFragment.newInstance(getMainActions());
        setContentAndActionFragments(mContentFragment, mActionFragment);

        mIntentFilter = new IntentFilter("android.intent.action.HDMI_PLUGGED");
        mIntentFilter.addAction(Intent.ACTION_TIME_TICK);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mIntentReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mIntentReceiver);
    }

    @Override
    public void onActionClicked(Action action) {
        curValue= action.getKey().toString().trim();
        saveValue= mOutputUiManager.getCurrentColorAttribute().toString().trim();
        curMode = mOutputUiManager.getCurrentMode().trim();
        Log.i(LOG_TAG,"curValue: "+curValue);
        Log.i(LOG_TAG,"saveValue: "+saveValue);
        if (!curValue.equals(saveValue)) {
            if (isModeSupportColor(curMode,curValue)) {
                mOutputUiManager.changeColorAttribte(curValue);
                updateMainScreen();
           }
           else{
               String msg  = "Not support this color format & depth!";
               Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
               toast.show();
               finish();
           }
        }
    }

    private boolean isModeSupportColor(final String curMode, final String curValue){
        boolean  ret = false;
        ret = mOutputUiManager.isModeSupportColor(curMode, curValue);
        return ret;
    }
    private ContentFragment createMainMenuContentFragment() {
        return ContentFragment.newInstance(
                getString(R.string.device_colorattribute), getString(R.string.device_color),
                null, R.drawable.ic_settings_display,
                getResources().getColor(R.color.icon_background));
    }

    private void goToMainScreen() {
        updateMainScreen();
        getFragmentManager().popBackStack(null, 0);
    }

    private void updateMainScreen() {
        mOutputUiManager.updateUiMode();
        ((ActionAdapter) mActionFragment.getAdapter()).setActions(getMainActions());
    }

    private ArrayList<Action> getMainActions() {
        ArrayList<Action> actions = new ArrayList<Action>();
        ArrayList<String> colorTitleList = mOutputUiManager.getColorTitleList();
        ArrayList<String> colorValueList = mOutputUiManager.getColorValueList();
        String value = null;
        String  curColorValue = mOutputUiManager.getCurrentColorAttribute().toString().trim();
        Log.i(LOG_TAG,"curColorValue: "+curColorValue);

        for (int i = 0; i < colorTitleList.size(); i++) {
            value = colorValueList.get(i).trim();
            curMode = mOutputUiManager.getCurrentMode().trim();
            if (!isModeSupportColor(curMode, value)) {
                continue;
            }

            if (curColorValue.equals(value)) {
                actions.add(new Action.Builder().key(value)
                        .title("        " + colorTitleList.get(i))
                        .checked(true).build());
            }else {
                actions.add(new Action.Builder().key(value)
                        .title("        " + colorTitleList.get(i))
                        .description("").build());
            }
        }
        return actions;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FRESH_UI:
                    updateMainScreen();
                    break;
            }
        }
    };
}

