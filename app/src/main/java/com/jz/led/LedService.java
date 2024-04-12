package com.jz.led;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.jz.led.utils.Contrants;
import com.jz.led.utils.LedUtil;
import com.jz.led.utils.SystemUtils;

import java.util.ArrayList;

public class LedService extends Service {

    private String TAG = "==zxd"+LedService.class.getSimpleName();
    private LedUtil ledUtil;

    @Override
    public void onCreate() {
        Log.d(TAG, "LedService onCreate");
        if(ledUtil == null){
            ledUtil = new LedUtil();
        }
        initLed();
        super.onCreate();
    }

    private void initLed(){
        //是否打开Led灯
        String ledEnable = SystemUtils.getProp("persist.led.switch","OFF");
        //是否首次运行
        boolean first = SystemUtils.getProp("persist.led.first","true").equals("true");
        if(ledEnable.equals("ON")||first){
            //是否开启循环
            Contrants.isCycle = SystemUtils.getProp("persist.circle.switch","OFF").equals("ON");
            String colors = SystemUtils.getProp("persist.led.colors","[0000FF,0000FF,0000FF,0000FF,0000FF,0000FF]");
            colors = colors.replace("[","").replace("]","").replace(" ","");
            String cols[] = colors.split(",");
            Log.d("===zxd","colors="+colors+",size="+cols.length);
            ArrayList<String> rgbs = new ArrayList<>();
            for (int i = 0 ; i < cols.length;i++){
                rgbs.add(cols[i]);
                Log.d("===zxd","colors"+i+"==="+cols[i]+",size="+cols[i].length());
            }
            String ledMode =  SystemUtils.getProp("persist.current.led.mode", Contrants.MODE_SING);
            ledUtil.turnOnForMode(ledUtil.getMode(ledMode),rgbs);
        }
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
