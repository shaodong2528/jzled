package com.jz.led;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.jz.led.message.MessengerClient;


/**
 * Copyright (C)
 * FileName: JZApplication
 * Author: JZ
 * Date: 2022/5/7 19:43
 * Description: application
 */
public class MainApplication extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        if(!getContext().getPackageName().equals("com.jz.server")){
            MessengerClient.getInstance().init(getApplicationContext());
        }
        startService(new Intent(this, LedService.class));
    }

    public static Context getContext() {
        return mContext;
    }
}
