package com.jz.led.light;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.jz.led.utils.Contrants;

import java.lang.Exception;
import java.io.FileOutputStream;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Light implements ILight {
    private static final String LED_CTRL_NODE = "/proc/led_ctrl_byte";

    private static final String TAG = "juwei-light";

    private static final int NUMBER_OF_LIGHT = 6;
    private static final int STREAM_COUNT = (NUMBER_OF_LIGHT / 3) * 2;

    private static final int MODE_NORMAL = 0;
    private static final int MODE_BLINK = 1;
    private static final int MODE_STREAM = 2;
    private static final int MODE_BREATHE = 3;
    private static final int MODE_MUSIC = 4;

    private boolean mBlinkOn = false;
    private boolean mBreatheIn = true;

    private int mStreamLoopCount = 0;
    private int mBlinkLoopCount = 0;
    private double[] mBreathHSV = new double[3];

    private volatile int mMode = MODE_NORMAL;

    private static final int MSG_TURN_OFF = 0;
    private static final int MSG_TURN_ON = 1;
    private static final int MSG_BLINK = 2;
    private static final int MSG_STREAM = 3;
    private static final int MSG_BREATHE = 4;
    private static final int MSG_MUSIC = 7;

    private static final int MSG_TURN_OFF_ONE = 5;
    private static final int MSG_TURN_ON_ONE = 6;

    private final HandlerThread mHandlerThread;
    private final Handler mHandler;

    private final byte[] mCmd = new byte[NUMBER_OF_LIGHT * 3];

    private static final Light mLight = new Light();
    private int cycleCount = 0;
    //单色推荐颜色
    private final ArrayList<String> recmdColors = new ArrayList<>(Arrays.asList("F50802","F26103","E39507","2DA71A","1592A3","4307F1"));

    private Light() {
        mHandlerThread = new HandlerThread("light-service");
        mHandlerThread.start();

        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_TURN_OFF:
                        handleTurnOff();
                        break;
                    case MSG_TURN_ON:
                        handleTurnOn((int[]) msg.obj);
                        break;
                    case MSG_BLINK:
                        mHandler.removeMessages(MSG_BLINK);//删除之前的
                        handleBlink((int[]) msg.obj, msg.arg1, msg.arg2);
                        break;
                    case MSG_STREAM:
                        mHandler.removeMessages(MSG_STREAM); //删除之前的
                        handleStream((int[]) msg.obj, msg.arg1, msg.arg2);
                        break;
                    case MSG_BREATHE:
                        mHandler.removeMessages(MSG_BREATHE);  //删除之前的
                        handleBreathe((int[]) msg.obj, msg.arg1, msg.arg2);
                        break;
                    case MSG_MUSIC:
                        handMusicSound((int[]) msg.obj, msg.arg1);
                        break;
                    case MSG_TURN_OFF_ONE:
                        handleTurnOffOne(msg.arg1);
                        break;
                    case MSG_TURN_ON_ONE:
                        handleTurnOnOne(msg.arg1, msg.arg2);
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    public static Light getInstance() {
        return mLight;
    }

    private int rgbR(int rgb) {
        return (rgb & 0xff0000) >> 16;
    }

    private int rgbG(int rgb) {
        return (rgb & 0xff00) >> 8;
    }

    private int rgbB(int rgb) {
        return rgb & 0xff;
    }

    private static double[] rgbToHSV(int rgb) {

        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;

        double rr = red / 255.0;
        double gg = green / 255.0;
        double bb = blue / 255.0;

        double maxVal = Math.max(Math.max(rr, gg), bb);
        double minVal = Math.min(Math.min(rr, gg), bb);

        double hue, saturation, value;

        value = maxVal;

        if (maxVal > 0.0) {
            saturation = (maxVal - minVal) / maxVal;
        } else {
            saturation = 0.0;
        }
        hue = 0.0;
        if (maxVal == minVal) {
            hue = 0.0;
        } else {
            double delta = maxVal - minVal;
            if (maxVal == rr) {
                hue = 60.0 * ((gg - bb) / delta + ((gg < bb) ? 6 : 0));
            } else if (maxVal == gg) {
                hue = 60.0 * ((bb - rr) / delta + 2);
            } else if (maxVal == bb) {
                hue = 60.0 * ((rr - gg) / delta + 4);
            }

            if (hue >= 360.0) {
                hue -= 360.0;
            }
        }

        return new double[]{hue, saturation, value};
    }

    private static int hsvToRgb(double hue, double saturation, double value) {
        int hi = (int) (hue / 60) % 6;
        double f = hue / 60 - hi;
        double p = value * (1 - saturation);
        double q = value * (1 - f * saturation);
        double t = value * (1 - (1 - f) * saturation);

        double red, green, blue;
        switch (hi) {
            case 0:
                red = value;
                green = t;
                blue = p;
                break;
            case 1:
                red = q;
                green = value;
                blue = p;
                break;
            case 2:
                red = p;
                green = value;
                blue = t;
                break;
            case 3:
                red = p;
                green = q;
                blue = value;
                break;
            case 4:
                red = t;
                green = p;
                blue = value;
                break;
            case 5:
                red = value;
                green = p;
                blue = q;
                break;
            default:
                red = green = blue = 0;
                break;
        }

        int r = (int) (red * 255);
        int g = (int) (green * 255);
        int b = (int) (blue * 255);

        return (r << 16) | (g << 8) | b;
    }

    private void writeCmd(byte[] cmd) {
        Log.d(TAG, "writeRGB, cmd: " + encodeHexString(cmd));

        try (FileOutputStream out = new FileOutputStream(LED_CTRL_NODE)) {
            out.write(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String encodeHexString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private void lightOff() {
        Log.d("===zxd","lightOff,mCmd="+mCmd);
        Arrays.fill(mCmd, 0, mCmd.length, (byte) 0);
        writeCmd(mCmd);
    }

    private void lightOn(int rgb) {
        Log.d("===zxd","lightOn,rgb="+rgb);
        for (int i = 0; i < NUMBER_OF_LIGHT; i++) {
            mCmd[i * 3] = (byte) rgbR(rgb);
            mCmd[i * 3 + 1] = (byte) rgbG(rgb);
            mCmd[i * 3 + 2] = (byte) rgbB(rgb);
        }
        writeCmd(mCmd);
    }

    private void updateCmd(int rgb, int index) {
        Log.d("===zxd","updateCmd,rgb="+rgb);
        if (index >= mCmd.length) return;
        mCmd[index * 3] = (byte) rgbR(rgb);
        mCmd[index * 3 + 1] = (byte) rgbG(rgb);
        mCmd[index * 3 + 2] = (byte) rgbB(rgb);
    }

    private void handleTurnOff() {
        Log.d("===zxd","handleTurnOff,"+mMode);
        if (mMode != MODE_NORMAL) return;
        lightOff();
    }

    private void handleTurnOn(int[] rgbs) {
        Log.d("===zxd","handleTurnOn,"+mMode);
        if (mMode != MODE_NORMAL) return;
        for (int i = 0; i < rgbs.length; i++) {
            updateCmd(rgbs[i], i);
        }
        writeCmd(mCmd);
        if(Contrants.isCycle){
            ++cycleCount;
            for (int i = 0 ;i < recmdColors.size();i++){
                int cycles[] = getRGBS(recmdColors);
                rgbs[i] = cycles[cycleCount % NUMBER_OF_LIGHT];
            }
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TURN_ON, -1, -1, rgbs),1000);
        }
    }

    private void handleBlink(int[] rgbs, int openDuration, int closeDuration) {
        Log.d("===zxdddddd","handleBlink111111111,"+mMode);
        if (mMode != MODE_BLINK) return;
        mBlinkOn = !mBlinkOn;
        if (mBlinkOn) {
            if (mBlinkLoopCount > rgbs.length - 1) {
                mBlinkLoopCount = 0;
            }
            lightOn(rgbs[mBlinkLoopCount]);
            mBlinkLoopCount++;
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_BLINK, openDuration, closeDuration, rgbs), openDuration);
        } else {
            lightOff();
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_BLINK, openDuration, closeDuration, rgbs), closeDuration);
        }
    }

    private void handleStream(int[] rgbs, int speed, int index) {
        Log.d("===zxdddddd","handleStream22222222222222,"+mMode);
        if (mMode != MODE_STREAM) return;

        if (index < 0) {
            mStreamLoopCount = 0;
        }

        if (mStreamLoopCount > rgbs.length - 1) {
            mStreamLoopCount = 0;
        }
        int rgb = rgbs[mStreamLoopCount];

        for (int i = 0; i < NUMBER_OF_LIGHT; i++) {
            if (i > index - STREAM_COUNT && i <= index) {
                updateCmd(rgb, i);
            } else {
                updateCmd(0x00, i);
            }
        }
        writeCmd(mCmd);

        int next = index + 1;
        if (next - STREAM_COUNT > NUMBER_OF_LIGHT) {
            next = 0;
            mStreamLoopCount++;
            //开启循环
            if(Contrants.isCycle){
                int cycles[] = getRGBS(recmdColors);
                for (int i = 0 ; i < NUMBER_OF_LIGHT;i++){
                    rgbs[i] = cycles[mStreamLoopCount % NUMBER_OF_LIGHT];
                }
                Log.d("===zzzddd","streamCount="+mStreamLoopCount+",cycles="+cycles+",rgbs="+rgbs);
            }
        }
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_STREAM, speed, next, rgbs), speed);
    }

    private void handleBreathe(int[] rgbs, int interval, int loopCount) {
        Log.d("===zxdddddd","handleBreathe33333333333,"+mMode);
        if (mMode != MODE_BREATHE) return;

        Log.d(TAG, "handleBreathe loop count " + loopCount);
        int delay = interval / 99;

        if (loopCount < 0) {
            mBreathHSV = rgbToHSV(rgbs[0]);
            double v = mBreathHSV[2];
            mBreatheIn = !(v > 0.5);
            lightOn(rgbs[0]);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_BREATHE, interval, 0, rgbs), delay);
            return;
        }

        double v = mBreathHSV[2];
        if (mBreatheIn) {
            v += 0.01;
        } else {
            v -= 0.01;
        }

        if (v > 1.0) {
            mBreathHSV[2] = 1.0;
            mBreatheIn = false;
        } else if (v < 0.01) {
            mBreatheIn = true;
            loopCount++;
            if (loopCount > rgbs.length - 1) {
                loopCount = 0;
            }
            mBreathHSV = rgbToHSV(rgbs[loopCount]);
            //循环颜色
            if(Contrants.isCycle){
                int cycles[] = getRGBS(recmdColors);
                mBreathHSV = rgbToHSV(cycles[++cycleCount % NUMBER_OF_LIGHT]);
                Log.d("===zzzddd","recmd cycle "+cycleCount);
            }
            mBreathHSV[2] = 0.01;
        } else {
            mBreathHSV[2] = v;
        }
        lightOn(hsvToRgb(mBreathHSV[0], mBreathHSV[1], mBreathHSV[2]));
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_BREATHE, interval, loopCount, rgbs), delay);
    }

    public void handMusicSound(int[] rgbs,int interval){
        if (mMode != MODE_MUSIC){
            return;
        }
        for (int i = 0; i < NUMBER_OF_LIGHT; i++) {
            //随机关灯
            Random tunOn = new Random();
            if(tunOn.nextInt(3) == 1){
                mCmd[i * 3] = (byte) rgbR(rgbs[0]);
                mCmd[i * 3 + 1] = (byte) rgbG(rgbs[0]);
                mCmd[i * 3 + 2] = (byte) rgbB(rgbs[0]);
            }else{
                mCmd[i * 3] = (byte) rgbR(0x00);
                mCmd[i * 3 + 1] = (byte) rgbG(0x00);
                mCmd[i * 3 + 2] = (byte) rgbB(0x00);
            }
        }
        writeCmd(mCmd);
        Log.d("===zzxd","music "+interval+",color="+rgbs[0]);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_MUSIC,interval,-1,rgbs),interval);
    }

    private void handleTurnOnOne(int rgb, int index) {
        Log.d("===zxd","handleTurnOnOne,"+mMode);
        if (mMode != MODE_NORMAL) return;
        updateCmd(rgb, index);
        writeCmd(mCmd);
    }

    private void handleTurnOffOne(int index) {
        Log.d("===zxd","handleTurnOffOne,"+mMode);
        if (mMode != MODE_NORMAL) return;
        updateCmd(0x0, index);
        writeCmd(mCmd);
    }

    @Override
    public void turnOff() {
        mMode = MODE_NORMAL;
        mHandler.sendEmptyMessage(MSG_TURN_OFF);
    }

    @Override
    public void turnOn(int[] rgb) {
        mMode = MODE_NORMAL;
        mHandler.removeMessages(MSG_TURN_ON);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_TURN_ON, -1, -1, rgb));
    }

    @Override
    public void turnOffOne(int index) {
        mMode = MODE_NORMAL;
        mHandler.sendMessage(mHandler.obtainMessage(MSG_TURN_OFF_ONE, index, -1));
    }

    @Override
    public void turnOnOne(int rgb, int index) {
        mMode = MODE_NORMAL;
        mHandler.sendMessage(mHandler.obtainMessage(MSG_TURN_ON_ONE, rgb, index));
    }

    @Override
    public void blink(int[] rgb, int openDuration, int closeDuration) {
        mMode = MODE_BLINK;
        mHandler.sendMessage(mHandler.obtainMessage(MSG_BLINK, openDuration, closeDuration, rgb));
    }

    @Override
    public void stream(int[] rgb, int speed) {
        mMode = MODE_STREAM;
        mHandler.sendMessage(mHandler.obtainMessage(MSG_STREAM, speed, -1, rgb));
    }

    @Override
    public void breathe(int[] rgb, int interval) {
        mMode = MODE_BREATHE;
        mHandler.sendMessage(mHandler.obtainMessage(MSG_BREATHE, interval, -1, rgb));
    }

    @Override
    public void musicSound(int[] rgb, int interval) {
        mMode = MODE_MUSIC;
        mHandler.removeMessages(MSG_MUSIC);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_MUSIC, interval, -1, rgb));
    }

    /**
     * 16进制字符串值转成int
     * @return
     */
    public int[] getRGBS(ArrayList<String> hexRgbs) {
        ArrayList<Integer> rgbList = new ArrayList<>();
        for (int i = 0 ; i < hexRgbs.size();i++){
            rgbList.add(Integer.parseInt(hexRgbs.get(i), 16));
        }
        int[] rgbs = new int[rgbList.size()];
        for (int i = 0; i < rgbs.length; i++) {
            rgbs[i] = rgbList.get(i);
        }
        return rgbs;
    }
}