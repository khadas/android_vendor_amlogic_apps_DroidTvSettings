/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.droidlogic.tv.settings;

import android.content.Context;
import com.droidlogic.app.SystemControlManager;

/**
 * Settings related constants
 */
public class SettingsConstant {

    public static String PACKAGE = "com.droidlogic.tv.settings";

    public static boolean needDroidlogicMboxFeature(Context context){
        SystemControlManager sm = new SystemControlManager(context);
        return sm.getPropertyBoolean("ro.platform.has.mbxuimode", false);
    }

    public static boolean needDroidlogicTvFeature(Context context){
        SystemControlManager sm = new SystemControlManager(context);
        return sm.getPropertyBoolean("ro.platform.has.tvuimode", false);
    }
    public static boolean needDroidlogicHdrFeature(Context context){
        return context.getResources().getBoolean(R.bool.display_need_hdr_function);
    }
    public static boolean needDroidlogicDigitalSounds(Context context){
        return context.getResources().getBoolean(R.bool.display_need_digital_sounds);
    }
    public static boolean needScreenResolutionFeture(Context context){
        return context.getResources().getBoolean(R.bool.display_need_screen_resolution);
    }
}
