package com.jz.led.utils;

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
    //渐变颜色
    private static final ArrayList<String> gradientColor1 = new ArrayList<>(Arrays.asList("DF07FF","A012E2","701CCA","2D2297","192485","7888B0"));
    private static final ArrayList<String> gradientColor2 = new ArrayList<>(Arrays.asList("F005FF","C331FF","9063FF","6F83FF","34BCFF","7BEBFF"));
    private static final ArrayList<String> gradientColor3 = new ArrayList<>(Arrays.asList("02A365","00A651","009E7D","009A92","0097A6","71C3D5"));
    private static final ArrayList<String> gradientColor4 = new ArrayList<>(Arrays.asList("FF9903","FF7517","FF502C","FF3B39","FF174F","FF78A0"));
    private static final ArrayList<String> gradientColor5 = new ArrayList<>(Arrays.asList("FFAB00","FF8700","FF7700","FF5700","FF4500","FF9271"));
    private static final ArrayList<String> gradientColor6 = new ArrayList<>(Arrays.asList("EFA301","CFA000","A09D00","769C00","549B00","99C976"));
    public static TreeMap<Integer,ArrayList<String>> colorsMap = new TreeMap<Integer,ArrayList<String>>(){{
        put(0,gradientColor1);
        put(1,gradientColor2);
        put(2,gradientColor3);
        put(3,gradientColor4);
        put(4,gradientColor5);
        put(5,gradientColor6);
    }};
    //单色推荐颜色
    private static final ArrayList<String> recmdColors = new ArrayList<>(Arrays.asList("F50802","F26103","E39507","2DA71A","1592A3","4307F1"));
    public enum LightMode {
        //正常、闪烁、流水、呼吸、单个开
        NORMAL, BLINK, STREAM, BREATHE, SINGLE,MUSIC
    }

    public ArrayList<String> getColors(String curHexColor,String curMode){
        ArrayList<String> colors = new ArrayList<>();
        if(Contrants.isCycle){  //是否打开循环
            colors.add(curHexColor);
        }else{
            if(curMode.equals(Contrants.MODE_GRADIENT)){  //渐变模式设置6种颜色
                colors.addAll(colorsMap.get(Contrants.mColorBtnIndex));
                Log.d("===zzzddd","colors="+colors+",index="+Contrants.mColorBtnIndex);
            }else{
                for (int i = 0 ; i < 6;i++){
                    colors.add(curHexColor);
                }
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
            case Contrants.MODE_GRADIENT:
                return LightMode.SINGLE;
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
                stream(100,hexRgbs);
                break;
            case BREATHE:
                breathe(1000,hexRgbs);
                break;
            case SINGLE:
                turnOn(hexRgbs);
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
}
