package com.jz.led.light;

public interface ILight {
    /**
     *  关闭所有灯
     */
    public void turnOff();

    /**
     * 使用rgb数组中的颜色打开相应索引对应的灯. 如果相应的颜色值为0, 关闭该灯. 如果rgb数组数据小于灯的数目,
     * 维持相应的灯的状态不变.
     * @param rgb 颜色数组. 数组的索引值对应灯的序号.
     */
    public void turnOn(int[] rgb);

    /**
     * 关闭index号灯. 不影响其他灯的状态
     * @param index
     */
    public void turnOffOne(int index);

    /**
     * 打开或更新index号灯的颜色. 不影响其它灯的状态
     * @param rgb
     * @param index
     */
    public void turnOnOne(int rgb, int index);

    /**
     * 闪烁模式.
     * @param rgb 灯每次打开的颜色. 第一次使用rgb[0], 第二次使用rgb[1], 以此类推. 如果rgb数组只有一个颜色,
     *            意味着灯只有一种颜色闪烁.
     * @param openDuration 灯亮的时间, 单位毫秒
     * @param closeDuration 灯灭的时间, 单位毫秒
     */
    public void blink(int[] rgb, int openDuration, int closeDuration);

    /**
     * 流水灯
     * @param rgb 一次流水使用rgb数组中的一种颜色. 第一次使用rgb[0], 第二次使用rgb[1], 以此类推.
     *            如果rgb数组只有一个颜色, 意味始终使用一种颜色跑流水灯.
     * @param speed 灯切换的速度, 单位毫秒
     */
    public void stream(int[] rgb, int speed);

    /**
     * 呼吸灯
     * @param rgb 一次呼吸使用rgb数组中的一种颜色. 第一次使用rgb[0], 第二次使用rgb[1], 以此类推.
     *            如果rgb数组只有一个颜色, 意味始终使用一种颜色呼吸.
     * @param interval 一次呼或者一次吸的时间间隔, 单位毫秒
     */
    public void breathe(int[] rgb, int interval);
}