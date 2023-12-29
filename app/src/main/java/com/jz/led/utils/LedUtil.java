package com.jz.led.utils;

import com.jz.led.light.Light;

import java.util.ArrayList;

public class LedUtil {

    public enum LightMode {
        //正常、闪烁、流水、呼吸、单个开
        NORMAL, BLINK, STREAM, BREATHE, SINGLE,MUSIC
    }

    public ArrayList<String> getColors(String curHexColor,String curMode){
        ArrayList<String> colors = new ArrayList<>();
        if(Contrants.isCycle){  //是否打开循环
            colors.add(curHexColor);
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
            case Contrants.MODE_GRADIENT:
                return LightMode.SINGLE;
            case Contrants.MODE_MUSIC:
                return LightMode.MUSIC;
            default:
                return LightMode.NORMAL;
        }
    }

    public void turnOnForMode(LightMode mode,ArrayList<String> hexRgbs) {
        switch (mode) {
            case BLINK:
                blink(500,500,hexRgbs);
                break;
            case STREAM:
                stream(100,hexRgbs);
                break;
            case BREATHE:
                breathe(500,hexRgbs);
                break;
            case SINGLE:
                turnOnEach(hexRgbs);
                break;
            case NORMAL:
            default:
                turnOn(hexRgbs);
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
