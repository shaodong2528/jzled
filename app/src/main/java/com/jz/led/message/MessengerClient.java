package com.jz.led.message;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * Copyright (C)
 * FileName: MessengerClient
 * Author: JZ
 * Date: 2022/5/9 17:03
 * Description: MessengerClient
 */
public class MessengerClient {

    private static final String TAG = "MessengerClient";

    private Context mContext = null;

    private Messenger mMessengerService = null;
    private Messenger mMessengerClient = new Messenger(new ClientHandler());

    private static MessengerClient mInstance = null;

    private MessengerClient() {
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");

            mMessengerService = new Messenger(service);
            Message obtain = Message.obtain(null, CommonParams.MSG_REC_REGISTER_CLIENT);
            Bundle bundle = new Bundle();
            bundle.putString("msg", "message from client packageName:" + mContext.getPackageName());
            obtain.setData(bundle);
            sendMsgToMessengerService(obtain);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            mMessengerService = null;
        }
    };

    public static MessengerClient getInstance() {
        if (null == mInstance) {
            synchronized (MessengerClient.class) {
                if (null == mInstance) {
                    mInstance = new MessengerClient();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        Log.d(TAG, "init");
        mContext = context;
        bindMessengerService();
    }

    public void uninit() {
        Log.d(TAG, "uninit");
        try {
            unbindMessengerService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bindMessengerService() {
        Log.d(TAG, "bind MessengerService");
        Intent bindIntent = new Intent();
        bindIntent.setAction("com.jz.server.MessengerService");
        bindIntent.setPackage("com.jz.server");
        mContext.bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindMessengerService() {
        Log.d(TAG, "unbind MessengerService");
        mContext.unbindService(mConnection);

        Message msg = Message.obtain(null, CommonParams.MSG_REC_UNREGISTER_CLIENT);
        sendMsgToMessengerService(msg);
    }

    public class ClientHandler extends Handler {

        @Override
        public void handleMessage(@NonNull Message msg) {
            Log.d(TAG, "ClientHandler handleMessage " + msg.what);
            switch (msg.what) {
                case CommonParams.MSG_REC_HELLO_SERVER:
                    Log.d(TAG, msg.getData().getString("msg"));
                    break;
                default:
                    MsgHandlerCenter.dispatchMessage(msg);
                    break;
            }
        }
    }

    public boolean sendMsgToMessengerService(Message msg) {
        if (mMessengerService == null) {
            Log.d(TAG, "mMessengerService is null");
            return false;
        }
        try {
            msg.replyTo = mMessengerClient;
            mMessengerService.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
