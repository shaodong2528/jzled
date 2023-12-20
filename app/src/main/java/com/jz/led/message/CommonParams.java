package com.jz.led.message;

import android.os.HandlerThread;
import android.os.Looper;

/**
 * Copyright (C)
 * FileName: CommonParams
 * Author: JZ
 * Date: 2022/5/9 14:12
 * Description: CommonParams
 */
public class CommonParams {

    public static final int MSG_ARG_INVALID = -1;

    public static final int MSG_REC_REGISTER_CLIENT = 0x0A01;
    public static final int MSG_REC_UNREGISTER_CLIENT = 0x0A02;
    public static final int MSG_REC_HELLO_SERVER = 0x0A03;

    public static final int MSG_KEY_EVENT_TYPE = 0x0A05;
    public static final int MSG_KEY_EVENT_DOWN = 0x0A06;

    public static final int MSG_BACK_TO_ACCESSORY = 0x0A07; //返回到原车

    public static final int MSG_SETTING_ANIMATION_MUTE = 0x0A08; //开机动画声音是否静音 1:静音 0:非静音

    public static final int MSG_SETTING_ANIMATION_FORCE = 0x0A09; //开机动画是否强制显示 1:显示 0:不显示

    public static final int MSG_PANDORA_HOME_START = 0x0A0A; //进入桌面

    public static final int MSG_SETTING_BLUETOOTH_ENABLED = 0x0A0B; //蓝牙使能 1:power on 0:power off

    public static final int MSG_SETTING_CARPLAY_DISCONNECT = 0x0A0C; //断开carplay

    public static final int MSG_SETTING_AUTOPLAY_DISCONNECT = 0x0A0D; //断开autoplay

    public static final int MSG_PHONE_NEW_CALL = 0x0A0E;

    public static final int MSG_PHONE_END_CALL = 0x0A0F;

    public static final int MSG_MOUSE_ENABLE = 0x0A10;

    public static final int MSG_MOUSE_DISABLE = 0x0A11;

    public static final int MSG_DISCONNECT_ACCESSORY = 0x0A13; //断开连接

    public static final int KEY_HOME = 3;
    public static final int KEY_BACK = 4;
    public static final int KEY_CALL = 5;
    public static final int KEY_END_CALL = 6;
    public static final int DPAD_UP = 19;
    public static final int DPAD_DOWN = 20;
    public static final int DPAD_LEFT = 21;
    public static final int DPAD_RIGHT = 22;
    public static final int DPAD_CENTER = 23;
    public static final int MEDIA_PLAY_PAUSE = 85;
    public static final int MEDIA_STOP = 86;
    public static final int MEDIA_NEXT = 87;
    public static final int MEDIA_PREVIOUS = 88;
    public static final int MEDIA_REWIND = 89;
    public static final int MEDIA_FAST_FORWARD = 90;
    public static final int MEDIA_PLAY = 126;
    public static final int MEDIA_PAUSE = 127;

    public class EventType {
        public static final int AUDIO_FOCUS_LOSS = 1;
        public static final int AUDIO_FOCUS_IDLE = 2;
        public static final int SCREEN_LOSS = 3;
        public static final int SCREEN_RENDER = 4;
        public static final int DPAD_UP = 5;
        public static final int DPAD_DOWN = 6;
        public static final int DPAD_LEFT = 7;
        public static final int DPAD_RIGHT = 8;
        public static final int DPAD_CENTER = 9;
        public static final int AUDIO_SETUP = 10;
        public static final int AUDIO_TEARDOWN = 11;
        public static final int VEHICLE_SPEED = 12;
        public static final int NIGHT_MODE = 13;
        public static final int REQUEST_SPEECH_RECOGNITION = 14;
        public static final int KEY_EVENT_DOWN = 15;
        public static final int TOUCH_EVENT = 16;
        public static final int KEY_EVENT_UP = 17;

        public static final int DPAD_CENTER_LONG_CLICK = 105;
    }

    public static Looper getLooper(HandlerThread handlerThread) {
        Looper looper = handlerThread.getLooper();
        while (looper == null) {
            handlerThread.start();
            looper = handlerThread.getLooper();
            if (looper != null) {
                break;
            }
        }
        return looper;
    }
}
