package com.jz.led;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.jz.led.utils.LedUtil;

public class LedService extends Service {

    private String TAG = "==zxd"+LedService.class.getSimpleName();
    private LedUtil ledUtil;
    @Override
    public void onCreate() {
        Log.d(TAG, "LedService onCreate");
        if(ledUtil == null){
            ledUtil = new LedUtil();
        }
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "LedService onStartCommand...");
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return ledUtil;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this,"server destory",0).show();
        Log.d(TAG, "LedService onStartCommand...");
        super.onDestroy();
    }
}
