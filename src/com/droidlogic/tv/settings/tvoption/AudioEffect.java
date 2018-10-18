/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.droidlogic.tv.settings.tvoption;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.lang.ref.WeakReference;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.util.UUID;

public class AudioEffect {
    private final static String TAG = "AudioEffect-JAVA";

    // effect type UUIDs are taken from hardware/libhardware/include/hardware/audio_effect.h

    /**
     * The following UUIDs define effect types corresponding to standard audio
     * effects whose implementation and interface conform to the OpenSL ES
     * specification. The definitions match the corresponding interface IDs in
     * OpenSLES_IID.h
     */
    /**
     * UUID for environmental reverberation effect
     */
    public static final UUID EFFECT_TYPE_ENV_REVERB = UUID
            .fromString("c2e5d5f0-94bd-4763-9cac-4e234d06839e");
    /**
     * UUID for preset reverberation effect
     */
    public static final UUID EFFECT_TYPE_PRESET_REVERB = UUID
            .fromString("47382d60-ddd8-11db-bf3a-0002a5d5c51b");
    /**
     * UUID for equalizer effect
     */
    public static final UUID EFFECT_TYPE_EQUALIZER = UUID
            .fromString("0bed4300-ddd6-11db-8f34-0002a5d5c51b");
    /**
     * UUID for bass boost effect
     */
    public static final UUID EFFECT_TYPE_BASS_BOOST = UUID
            .fromString("0634f220-ddd4-11db-a0fc-0002a5d5c51b");
    /**
     * UUID for virtualizer effect
     */
    public static final UUID EFFECT_TYPE_VIRTUALIZER = UUID
            .fromString("37cc2c00-dddd-11db-8577-0002a5d5c51b");

    /**
     * UUIDs for effect types not covered by OpenSL ES.
     */
    /**
     * UUID for Automatic Gain Control (AGC)
     */
    public static final UUID EFFECT_TYPE_AGC = UUID
            .fromString("0a8abfe0-654c-11e0-ba26-0002a5d5c51b");

    /**
     * UUID for Acoustic Echo Canceler (AEC)
     */
    public static final UUID EFFECT_TYPE_AEC = UUID
            .fromString("7b491460-8d4d-11e0-bd61-0002a5d5c51b");

    /**
     * UUID for Noise Suppressor (NS)
     */
    public static final UUID EFFECT_TYPE_NS = UUID
            .fromString("58b4b260-8e06-11e0-aa8e-0002a5d5c51b");

    /**
     * UUID for Loudness Enhancer
     */
    public static final UUID EFFECT_TYPE_LOUDNESS_ENHANCER = UUID
              .fromString("fe3199be-aed0-413f-87bb-11260eb63cf1");

    /**
     * UUID for Dynamics Processing
     */
    public static final UUID EFFECT_TYPE_DYNAMICS_PROCESSING = UUID
              .fromString("7261676f-6d75-7369-6364-28e2fd3ac39e");

    /**
     * Null effect UUID. See {@link AudioEffect(UUID, UUID, int, int)} for use.
     * @hide
     */
    public static final UUID EFFECT_TYPE_NULL = UUID
            .fromString("ec7178ec-e5e1-4432-a3f4-4657e6795210");

    /**
     * Effect connection mode is insert. Specifying an audio session ID when creating the effect
     * will insert this effect after all players in the same audio session.
     */
    public static final String EFFECT_INSERT = "Insert";
    /**
     * Effect connection mode is auxiliary.
     * <p>Auxiliary effects must be created on session 0 (global output mix). In order for a
     * MediaPlayer or AudioTrack to be fed into this effect, they must be explicitely attached to
     * this effect and a send level must be specified.
     * <p>Use the effect ID returned by {@link #getId()} to designate this particular effect when
     * attaching it to the MediaPlayer or AudioTrack.
     */
    public static final String EFFECT_AUXILIARY = "Auxiliary";
    /**
     * Effect connection mode is pre processing.
     * The audio pre processing effects are attached to an audio input (AudioRecord).
     * @hide
     */
    public static final String EFFECT_PRE_PROCESSING = "Pre Processing";

    /**
     * Successful operation.
     */
    public static final int SUCCESS = 0;
    /**
     * Unspecified error.
     */
    public static final int ERROR = -1;

    public AudioEffect(UUID type, UUID uuid, int priority, int audioSession)
            throws IllegalArgumentException, UnsupportedOperationException,
            RuntimeException {
    }

    public int setEnabled(boolean enabled) {
        return 0;
    }

    public void release() {
    }

    public int setParameter(byte[] param, byte[] value) {
        return 0;
    }


    public int setParameter(int param, int value) {
        return 0;
    }

    public int setParameter(int param, short value) {
        return 0;
    }

    public int setParameter(int param, byte[] value) {
        return 0;
    }

    public int setParameter(int[] param, int[] value) {
            return 0;
    }

 
    public int setParameter(int[] param, short[] value) {
        return 0;
    }

    public int setParameter(int[] param, byte[] value) {
        return 0;
    }

    public int getParameter(byte[] param, byte[] value) {
        return 0;
    }

    public int getParameter(int param, byte[] value) {
        return 0;
    }

    public int getParameter(int param, int[] value) {
        return 0;
    }

    public int getParameter(int param, short[] value) {
        return 0;
    }

    public int getParameter(int[] param, int[] value)
            throws IllegalStateException {
        return 0;
    }

    public int getParameter(int[] param, short[] value)
            throws IllegalStateException {
        return 0;
    }

    public int getParameter(int[] param, byte[] value)  {
        return 0;
    }
}
