package com.jz.led.utils;

import android.media.audiofx.Visualizer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.jz.led.MainApplication;
import com.jz.led.light.Light;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

public class LedUtil extends Binder {

    private Visualizer visualizer;
    private int currentVolume;
    private float currentFrequency;
    //渐变颜色
    private static final ArrayList<String> gradientColor1 = new ArrayList<>(Arrays.asList("E245FD","A010E2","721CCB","491FAD","2D2297","0D2476"));
    private static final ArrayList<String> gradientColor2 = new ArrayList<>(Arrays.asList("F00DFF","B242FF","8370FF","668BFF","44AEFF","1BD6FF"));
    private static final ArrayList<String> gradientColor3 = new ArrayList<>(Arrays.asList("1BD6FF","00A557","00A075","009C8A","009A90","0095AC"));
    private static final ArrayList<String> gradientColor4 = new ArrayList<>(Arrays.asList("FF9903","FF7517","FF502C","FF3B39","FF333E","FF1252"));
    private static final ArrayList<String> gradientColor5 = new ArrayList<>(Arrays.asList("FFAB00","FF8700","FF7700","FF5700","FF5700","FF3B00"));
    private static final ArrayList<String> gradientColor6 = new ArrayList<>(Arrays.asList("EFA301","CFA000","A09D00","769C00","749C00","449900"));
    public static TreeMap<Integer,ArrayList<String>> colorsMap = new TreeMap<Integer,ArrayList<String>>(){{
        put(0,gradientColor1);
        put(1,gradientColor2);
        put(2,gradientColor3);
        put(3,gradientColor4);
        put(4,gradientColor5);
        put(5,gradientColor6);
    }};

    public enum LightMode {
        //正常、闪烁、流水、呼吸、单色、音乐、渐变
        NORMAL, BLINK, STREAM, BREATHE, SINGLE,MUSIC,GRADIENT
    }

    public ArrayList<String> getColors(String curHexColor,String curMode){
        ArrayList<String> colors = new ArrayList<>();
        if(curMode.equals(Contrants.MODE_GRADIENT)){  //渐变模式设置6种颜色
            colors.addAll(colorsMap.get(Contrants.mColorBtnIndex));
            Log.d("===zzzddd","colors="+colors+",index="+Contrants.mColorBtnIndex);
        }else{
            for (int i = 0 ; i < 6;i++){
                colors.add(curHexColor);
            }
        }
        return colors;
    }
    public LightMode getMode(String mode) {
        switch (mode) {
            case Contrants.MODE_STREAM:
                return LightMode.STREAM;
            case Contrants.MODE_BREATH:
                return LightMode.BREATHE;
            case Contrants.MODE_SING:
                return LightMode.SINGLE;
            case Contrants.MODE_GRADIENT:
                return LightMode.GRADIENT;
            case Contrants.MODE_MUSIC:
                return LightMode.MUSIC;
            default:
                return LightMode.NORMAL;
        }
    }

    public void turnOnForMode(LightMode mode,ArrayList<String> hexRgbs) {
        SystemUtils.setProp("persist.led.colors",hexRgbs.toString());
        Log.d("===colors","="+hexRgbs);
        switch (mode) {
            case BLINK:
                blink(1000,1000,hexRgbs);
                break;
            case STREAM:
                stream(115,hexRgbs);
                break;
            case BREATHE:
                breathe(1600,hexRgbs);
                break;
            case SINGLE:
                turnOn(hexRgbs);
                break;
            case MUSIC:
                musicSound(150,hexRgbs);
                break;
            case GRADIENT:
                gradient(hexRgbs);
                break;
            case NORMAL:
            default:
                turnOnEach(hexRgbs);
                break;
        }
    }

    /**
     * 呼吸效果
     * @param openduration  亮灯周期(毫秒)
     * @param closeduration 关灯周期(毫秒)
     */
    public void blink(int openduration,int closeduration,ArrayList<String> hexRgbs) {
        Light.getInstance().blink(getRGBS(hexRgbs), openduration, closeduration);
    }

    /**
     * 流水速度(毫秒)
     */
    public void stream(int streamSpeed,ArrayList<String> hexRgbs) {
        Light.getInstance().stream(getRGBS(hexRgbs), streamSpeed);
    }

    /**
     * 呼吸时长(毫秒)
     * @param breathDuration
     */
    public void breathe(int breathDuration,ArrayList<String> hexRgbs) {
        Light.getInstance().breathe(getRGBS(hexRgbs), breathDuration);
    }

    /**
     * 音谱
     * @param delay 谱率(毫秒)
     * @param hexRgbs
     */
    public void musicSound(int delay,ArrayList<String> hexRgbs){
        Light.getInstance().musicSound(hexRgbs, delay);
    }
    /**
     * 渐变 同单色
     */
    public void gradient(ArrayList<String> hexRgbs){
        Light.getInstance().gradient(getRGBS(hexRgbs));
    }

    /**
     * 开灯
     */
    public void turnOn(ArrayList<String> hexRgbs) {
        Light.getInstance().turnOn(getRGBS(hexRgbs));
    }

    /**
     * 单色灯显示
     */
    public void turnOnEach(ArrayList<String> hexRgbs) {
        int[] rgbs = getRGBS(hexRgbs);
        for (int i = 0; i < rgbs.length; i++) {
            Light.getInstance().turnOnOne(rgbs[i], i);
        }
    }
    /**
     * 关闭所有灯
     */
    public void turnOff() {
        Light.getInstance().turnOff();
    }

    /**
     * 关闭指定的灯
     * @param index
     */
    public void turnOffForNumber(int index) {
        Light.getInstance().turnOffOne(index);
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

    public void musicVisual(){
        ArrayList<String> hexRgbs = new ArrayList<>();
        int sid = Integer.valueOf(SystemUtil.getProp("persist.led.music.sid","160"));
        try {
            visualizer = new Visualizer(sid);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(MainApplication.getContext(),"music id error",0).show();
            return;
        }
        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                Log.i("xiaozhu===", "waveform" + waveform.length+",rate="+samplingRate+",currentVolume="+currentVolume);
                long v = 0;
                for (int i = 0; i < waveform.length; i++) {
                    v += Math.pow(waveform[i], 2);
                }
                double volume = 10 * Math.log10(v / (double) waveform.length);
                currentVolume = (int) volume;
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                float[] magnitudes = new float[fft.length / 2];
                int max = 0;
                for (int i = 0; i < magnitudes.length; i++) {
                    magnitudes[i] = (float) Math.hypot(fft[2 * i], fft[2 * i + 1]);
                    if (magnitudes[max] < magnitudes[i]) {
                        max = i;
                    }
                }
                currentFrequency = max * samplingRate / fft.length;
                hexRgbs.clear();
                for (int i = 0 ; i < 6;i++){
                    hexRgbs.add(SystemUtil.colorBrightConvert("0000FF",(float)(currentFrequency*1.0/1000000)));
                }
                Log.i("xiaozhu===", "currentFrequency=" + currentFrequency+",birght="+(float)(currentFrequency*1.0/1000000)+",rgbs="+hexRgbs);
                musicSound(150,hexRgbs);
            }
        }, Visualizer.getMaxCaptureRate() / 2, true, true);
        visualizer.setEnabled(true);
    }

}
