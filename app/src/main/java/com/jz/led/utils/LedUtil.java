package com.jz.led.utils;

import android.view.View;
import android.widget.EditText;

import com.jz.led.activity.LightTestActivity;
import com.jz.led.light.Light;
import com.jz.led.settings.R;

import java.util.ArrayList;

public class LedUtil {

    private enum LightMode {
        //正常、闪烁、流水、呼吸、单个开
        NORMAL, BLINK, STREAM, BREATHE, SINGLE
    }

    private void turnOnForMode(LightMode mode) {
        switch (mode) {
            case BLINK:
                blink(500,500);
                break;
            case STREAM:
                stream(100);
                break;
            case BREATHE:
                breathe(500);
                break;
            case SINGLE:
                turnOnEach();
                break;
            case NORMAL:
            default:
                turnOn();
                break;
        }
    }

    /**
     * 呼吸效果
     * @param openduration  亮灯周期(毫秒)
     * @param closeduration 关灯周期(毫秒)
     */
    private void blink(int openduration,int closeduration) {
        Light.getInstance().blink(getRGBS(), openduration, closeduration);
    }

    /**
     * 流水速度(毫秒)
     */
    private void stream(int streamSpeed) {
        Light.getInstance().stream(getRGBS(), streamSpeed);
    }

    /**
     * 呼吸时长(毫秒)
     * @param breathDuration
     */
    private void breathe(int breathDuration) {
        Light.getInstance().breathe(getRGBS(), breathDuration);
    }

    /**
     * 开灯
     */
    private void turnOn() {
        Light.getInstance().turnOn(getRGBS());
    }

    /**
     * 单色灯显示
     */
    private void turnOnEach() {
        int[] rgbs = getRGBS();
        for (int i = 0; i < rgbs.length; i++) {
            Light.getInstance().turnOnOne(rgbs[i], i);
        }
    }
    /**
     * 关闭所有灯
     */
    private void turnOff() {
        Light.getInstance().turnOff();
    }

    /**
     * 关闭指定的灯
     * @param index
     */
    private void turnOffForNumber(int index) {
        Light.getInstance().turnOffOne(index);
    }

    /**
     *
     * @param hexRgbs
     * @return
     */
    private int[] getRGBS(String... hexRgbs) {
        ArrayList<Integer> rgbList = new ArrayList<>();
        if(hexRgbs.length == 1){
            for (int i = 0 ; i < 6;i++){  //单色
                rgbList.add(Integer.parseInt(hexRgbs[0], 16));
            }
        }else{
            for (int i = 0 ; i < hexRgbs.length;i++){
                rgbList.add(Integer.parseInt(hexRgbs[i], 16));
            }
        }
        int[] rgbs = new int[rgbList.size()];
        for (int i = 0; i < rgbs.length; i++) {
            rgbs[i] = rgbList.get(i);
        }
        return rgbs;
    }
}
