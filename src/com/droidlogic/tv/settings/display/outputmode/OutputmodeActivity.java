package com.droidlogic.tv.settings.display.outputmode;


import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.dialog.old.Action;
import com.droidlogic.tv.settings.dialog.old.ActionAdapter;
import com.droidlogic.tv.settings.dialog.old.ActionFragment;
import com.droidlogic.tv.settings.dialog.old.ContentFragment;
import com.droidlogic.tv.settings.dialog.old.DialogActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class OutputmodeActivity extends DialogActivity implements ActionAdapter.Listener, OnClickListener, OnFocusChangeListener{
    private final static String BEST_RESOLUTION          = "best resolution";
    private final static String DEEP_COLOR               = "deep_color";
    private final static String COLOR_SPACE_DEEP_COLOR   = "color space deep color";
    private final static String DOLBY_VISION             = "dolby_vision";

    private ContentFragment mContentFragment;
    private ActionFragment mActionFragment;
    private OutputUiManager mOutputUiManager;
    private IntentFilter mIntentFilter;
    private static final int MSG_FRESH_UI = 0;
    private static final int MSG_COUNT_DOWN = 1;
    private static boolean saveDeepColor = false;
    private static String saveMode;
    private View view_dialog;
    private TextView tx_title;
    private TextView tx_content;
    private Timer timer;
    private TimerTask task;
    private AlertDialog mAlertDialog = null;
    private int countdown = 15;
    private static String mode = null;

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
        mode = action.getKey();

        saveDeepColor = mOutputUiManager.isDeepColor();
        saveMode = mOutputUiManager.getCurrentMode();

        if (mode.equals(BEST_RESOLUTION)) {
            mOutputUiManager.change2BestMode();
        } else if (mode.equals(COLOR_SPACE_DEEP_COLOR)) {
            Intent intent = new Intent(OutputmodeActivity.this,ColorAttributeActivity.class);
            startActivity(intent);
            return;
        } else if (mode.equals(DOLBY_VISION)) {
            if (!mOutputUiManager.isDolbyVisionEnable()
                && mOutputUiManager.isTvSupportDolbyVision()) {
                mOutputUiManager.checkOutputmodeDeepcolor();
            }
            mOutputUiManager.switchDolbyVisionState();
        }else {
            mOutputUiManager.change2NewMode(mode);
        }
        updateMainScreen();

        if (saveDeepColor != mOutputUiManager.isDeepColor()
                || !saveMode.equals(mOutputUiManager.getCurrentMode())) {
            showDialog();
        }
    }

    private void goToMainScreen() {
        updateMainScreen();
        getFragmentManager().popBackStack(null, 0);
    }

    private void updateMainScreen() {
        mOutputUiManager.updateUiMode();
        ((ActionAdapter) mActionFragment.getAdapter()).setActions(getMainActions());
    }

    private ContentFragment createMainMenuContentFragment() {
        return ContentFragment.newInstance(
                getString(R.string.device_outputmode), getString(R.string.device_display),
                null, R.drawable.ic_settings_display,
                getResources().getColor(R.color.icon_background));
    }

    private ArrayList<Action> getMainActions() {
        ArrayList<Action> actions = new ArrayList<Action>();
        ArrayList<String> outputmodeTitleList = mOutputUiManager.getOutputmodeTitleList();
        ArrayList<String> outputmodeValueList = mOutputUiManager.getOutputmodeValueList();

        if (mOutputUiManager.getUiMode().equals(mOutputUiManager.HDMI_MODE)) {
            String best_resolution_description;
            String deep_color_description;
            String dolby_vision_description;
            best_resolution_description = mOutputUiManager.isBestOutputmode() ?
                getString(R.string.captions_display_on) : getString(R.string.captions_display_off);
            deep_color_description = mOutputUiManager.getCurrentColorAttribute();
            dolby_vision_description = mOutputUiManager.isDolbyVisionEnable() ?
                getString(R.string.captions_display_on) : getString(R.string.captions_display_off);

            boolean flag = mOutputUiManager.isDolbyVisionEnable()
                            && mOutputUiManager.isTvSupportDolbyVision();
            Action resolution = constructAction(BEST_RESOLUTION, getString(R.string.device_outputmode_auto),
                                    "                " + best_resolution_description);

            Action deep_color = constructAction(COLOR_SPACE_DEEP_COLOR,
                                    getString(R.string.device_outputmode_deep_color),
                                    "                " + deep_color_description);

            Action dolby_vision = constructAction(DOLBY_VISION, getString(R.string.dolby_vision_set),
                                    "                " + dolby_vision_description);

            if (flag) {
                resolution.setEnabled(false);
                deep_color.setEnabled(false);
            }
            actions.add(resolution);
            actions.add(deep_color);
            if (SystemProperties.getBoolean("ro.platform.support.dolbyvision", false) == true) {
                actions.add(dolby_vision);
            }
        }

        int currentModeIndex = mOutputUiManager.getCurrentModeIndex();
        for (int i = 0; i < outputmodeTitleList.size(); i++) {
            if (i == currentModeIndex) {
                Action mode = constructAction(outputmodeValueList.get(i), outputmodeTitleList.get(i), "");
                mode.setChecked(true);
                actions.add(mode);
            }else {
                actions.add(constructAction(outputmodeValueList.get(i), outputmodeTitleList.get(i), ""));
            }
        }
        return actions;
    }

    private Action constructAction(String key, String title, String description) {
        return new Action.Builder().key(key)
                        .title("        " + title)
                        .description(description).build();
    }
    private void showDialog () {
        if (mAlertDialog == null) {
            LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view_dialog = inflater.inflate(R.layout.dialog_outputmode, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            mAlertDialog = builder.create();
            mAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

            tx_title = (TextView)view_dialog.findViewById(R.id.dialog_title);
            tx_content = (TextView)view_dialog.findViewById(R.id.dialog_content);

            TextView button_cancel = (TextView)view_dialog.findViewById(R.id.dialog_cancel);
            button_cancel.setOnClickListener(this);
            button_cancel.setOnFocusChangeListener(this);

            TextView button_ok = (TextView)view_dialog.findViewById(R.id.dialog_ok);
            button_ok.setOnClickListener(this);
            button_ok.setOnFocusChangeListener(this);
        }
        mAlertDialog.show();
        mAlertDialog.getWindow().setContentView(view_dialog);

        if (mode.equals(DEEP_COLOR)) {
            if (saveDeepColor != mOutputUiManager.isDeepColor() && mOutputUiManager.isDeepColor()) {
                tx_content.setText(getResources().getString(R.string.device_outputmode_confirm_deepcolor));
            } else {
                tx_content.setText(getResources().getString(R.string.device_outputmode_confirm_mode));
            }
        } else {
            if (mOutputUiManager.getOutputmodeTitleList().size() <= 0) {
                tx_content.setText("Get outputmode empty!");
            } else if (mOutputUiManager.getCurrentModeIndex() < mOutputUiManager.getOutputmodeTitleList().size()) {
                tx_content.setText(getResources().getString(R.string.device_outputmode_change)
                    + " " +mOutputUiManager.getOutputmodeTitleList().get(mOutputUiManager.getCurrentModeIndex()));
            }
        }

        countdown = 15;
        if (timer == null)
            timer = new Timer();
        if (task != null)
            task.cancel();
        task = new DialogTimerTask();
        timer.schedule(task, 0, 1000);
    }

    private void recoverOutputMode() {
        if (saveDeepColor == mOutputUiManager.isDeepColor()) {
            mOutputUiManager.change2NewMode(saveMode);
        } else {
            mOutputUiManager.change2DeepColorMode();
        }
        mHandler.sendEmptyMessage(MSG_FRESH_UI);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_cancel:
                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                }
                recoverOutputMode();
                break;
            case R.id.dialog_ok:
                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                }
                break;
        }
        task.cancel();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v instanceof TextView) {
            if (hasFocus) {
            } else {
            }
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FRESH_UI:
                    updateMainScreen();
                    break;
                case MSG_COUNT_DOWN:
                    tx_title.setText(Integer.toString(countdown) + " " + getResources().getString(R.string.device_outputmode_countdown));
                    if (countdown == 0) {
                        if (mAlertDialog != null) {
                            mAlertDialog.dismiss();
                        }
                        recoverOutputMode();
                        task.cancel();
                    }
                    countdown--;
                    break;
            }
        }
    };

    private class DialogTimerTask extends TimerTask {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(MSG_COUNT_DOWN);
        }
    };
}
