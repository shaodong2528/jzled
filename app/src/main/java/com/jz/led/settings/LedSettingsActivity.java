package com.jz.led.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.jz.led.LedService;
import com.jz.led.activity.BasicActivity;
import com.jz.led.activity.LightTestActivity;
import com.jz.led.colorpick.ActionMode;
import com.jz.led.colorpick.ColorEnvelope;
import com.jz.led.colorpick.ColorPickerView;
import com.jz.led.colorpick.listeners.ColorEnvelopeListener;
import com.jz.led.colorpick.preference.ColorPickerPreferenceManager;
import com.jz.led.light.Light;
import com.jz.led.utils.Contrants;
import com.jz.led.utils.LedUtil;
import com.jz.led.utils.SystemUtil;
import com.jz.led.utils.SystemUtils;
import com.jz.led.widget.BrightnessSlideBar;

import java.util.ArrayList;


public class LedSettingsActivity extends BasicActivity implements View.OnClickListener {
    private ColorPickerView colorPickerView;
    private BrightnessSlideBar brightnessSlideBar;
    private ImageView vSwitchImg,vCircleImg,vAdd,vMinus,vGradientPan;
    private LinearLayout vModeList;
    private LinearLayout vModeLay,vModeSing,vModeGradient,vModeBreadh,vModeWater,vModeMusic;
    private FrameLayout vLedSwitchLay,vCircleSiwtch;
    private ImageView vLedSwitchLeftPoint,vLedSwitchRightPoint,vCircleSwitchLeftPoint,vCircleSwitchRightPoint;
    private boolean mLedSwitchStatue,mCircleSwitchStatue;
    private TextView vModeName;
    private ImageView vGradientIconColor1,vGradientIconColor2,vGradientIconColor3,vGradientIconColor4,vGradientIconColor5,vGradientIconColor6;
    private ImageView vMusicBg;
    private boolean isGradientMode;  //是否渐变模式
    private boolean isMusicMode;  //是否音乐模式
    private ImageView vBottomSelectLine; //底部推荐颜色线
    private int lastPointX = 0;
    private LinearLayout vCycleSwitchLay; //循环开关布局
    private String mCurHexColor = "0000FF",mCurLedMode = Contrants.MODE_SING;
    private LedUtil mService;
    private String TAG = "==zxd"+LedSettingsActivity.class.getSimpleName();

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(SystemUtils.getResourcesId("led_settings_activity", "layout"));
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initUI() {
        findViewById(R.id.left_home).setOnClickListener(this);
        //led开关
        vLedSwitchLay = findViewById(R.id.fl_led_switch_lay);
        vLedSwitchLay.setOnClickListener(this);
        vSwitchImg = findViewById(R.id.iv_led_switch);
        vLedSwitchLeftPoint = findViewById(R.id.iv_switch_point_left);
        vLedSwitchRightPoint = findViewById(R.id.iv_switch_point_right);
        //循环开关
        vCycleSwitchLay = findViewById(R.id.ll_right_switch);
        vCircleSiwtch = findViewById(R.id.fl_circle_switch_lay);
        vCircleSiwtch.setOnClickListener(this);
        vCircleImg = findViewById(R.id.iv_cycle_switch);
        vCircleSwitchLeftPoint = findViewById(R.id.iv_cycle_point_left);
        vCircleSwitchRightPoint = findViewById(R.id.iv_cycle_point_right);

        colorPickerView = findViewById(R.id.colorPickerView);
        colorPickerView.setActionMode(ActionMode.LAST); //手指释放回调监听
        ColorPickerPreferenceManager manager = ColorPickerPreferenceManager.getInstance(this);
        int color = manager.getColor("MyColorPicker",0);
        colorPickerView.setInitialColor(color);
        colorPickerView.setColorListener(new ColorEnvelopeListener() {
            @Override
            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                if(fromUser){
                    int color = envelope.getColor();
                    String hexColor = envelope.getHexCode();  //ex:FF215D3D
                    int argb[] = envelope.getArgb();
                    mCurHexColor = hexColor.substring(2); //去除前面两位透明度
                    SystemUtils.setProp("persist.save.led.hexcolor",mCurHexColor);
                    //vModeName.setTextColor(color);
                    ArrayList<String> rgbs = mService.getColors(mCurHexColor,mCurLedMode);
                    if(mCurLedMode.equals(Contrants.MODE_GRADIENT)){  //渐变颜色是固定的，所以需要处理下亮度
                        //转成当前对应亮度的颜色值
                        for (int i = 0 ; i < rgbs.size();i++){
                            String newHexColor = SystemUtil.colorBrightConvert(rgbs.get(i),brightnessSlideBar.getBrightValue());
                            rgbs.set(i,newHexColor);
                        }
                    }
                    mService.turnOnForMode(mService.getMode(mCurLedMode),rgbs);
                    Log.d(TAG,"brightValue="+brightnessSlideBar.getBrightValue()+",color="+color+",hexColor="+hexColor+","+mCurHexColor+",rbgColor="+argb[0]+","+argb[1]+","+argb[2]+","+argb[3]);
                }
            }
        });
        brightnessSlideBar = findViewById(R.id.brightnessSlide);
        colorPickerView.attachBrightnessSlider(brightnessSlideBar);

        vBottomSelectLine = findViewById(R.id.iv_line);

        vModeName = findViewById(R.id.tv_mode_name);
        vModeLay = findViewById(R.id.ll_mode);
        vModeLay.setOnClickListener(this);

        vAdd = findViewById(R.id.iv_add);
        vAdd.setOnClickListener(this);
        vMinus = findViewById(R.id.iv_minus);
        vMinus.setOnClickListener(this);

        vMusicBg = findViewById(R.id.iv_music_bg);
        vGradientPan = findViewById(R.id.iv_gradient);
        vModeList = findViewById(R.id.ll_mode_list);

        vGradientIconColor1 = findViewById(R.id.iv_color1);
        vGradientIconColor2 = findViewById(R.id.iv_color2);
        vGradientIconColor3 = findViewById(R.id.iv_color3);
        vGradientIconColor4 = findViewById(R.id.iv_color4);
        vGradientIconColor5 = findViewById(R.id.iv_color5);
        vGradientIconColor6 = findViewById(R.id.iv_color6);
        vGradientIconColor1.setOnClickListener(this);
        vGradientIconColor2.setOnClickListener(this);
        vGradientIconColor3.setOnClickListener(this);
        vGradientIconColor4.setOnClickListener(this);
        vGradientIconColor5.setOnClickListener(this);
        vGradientIconColor6.setOnClickListener(this);

        vModeSing = findViewById(R.id.ll_mode_sing);
        vModeGradient = findViewById(R.id.ll_mode_gradient);
        vModeBreadh = findViewById(R.id.ll_mode_breath);
        vModeWater = findViewById(R.id.ll_mode_water);
        vModeMusic = findViewById(R.id.ll_mode_music);
        vModeSing.setOnClickListener(this);
        vModeGradient.setOnClickListener(this);
        vModeBreadh.setOnClickListener(this);
        vModeWater.setOnClickListener(this);
        vModeMusic.setOnClickListener(this);

    }

    @Override
    protected void initData() {
        if (mService == null) {
            Intent intentService = new Intent(this, LedService.class);
            if (bindService(intentService, connection, Context.BIND_AUTO_CREATE)) {
                Log.d(TAG, "Bind LedService Success!");
            } else {
                Log.d(TAG, "Bind LedService Failed!");
            }
        }
        //获取上次保存点的位置
        initLedSwitchStatue();
        initCircleStatue();
        String mode = SystemUtils.getProp("persist.current.led.mode","");
        if(mode.equals(Contrants.MODE_SING)){
            vModeSing.setSelected(true);
            vModeName.setText(getResources().getString(R.string.app_led_mode_monochrome));
            mCurLedMode = Contrants.MODE_SING;
        }else if(mode.equals(Contrants.MODE_GRADIENT)){
            vModeGradient.setSelected(true);
            vModeName.setText(getResources().getString(R.string.app_led_mode_gradient));
            mCurLedMode = Contrants.MODE_GRADIENT;
            //隐藏色盘与音乐图
            colorPickerView.setVisibility(View.INVISIBLE);
            vGradientPan.setVisibility(View.VISIBLE);
            vMusicBg.setVisibility(View.INVISIBLE);
            findViewById(R.id.iv_cycle_line).setVisibility(View.INVISIBLE);
            vCycleSwitchLay.setVisibility(View.INVISIBLE);
        }else if(mode.equals(Contrants.MODE_BREATH)){
            vModeBreadh.setSelected(true);
            vModeName.setText(getResources().getString(R.string.app_led_mode_breathing));
            mCurLedMode = Contrants.MODE_BREATH;
            brightnessSlideBar.setEnabled(false);  //该模式不支持亮度调节
            brightnessSlideBar.setAlpha(0.5f);
        }else if(mode.equals(Contrants.MODE_STREAM)){
            vModeWater.setSelected(true);
            vModeName.setText(getResources().getString(R.string.app_led_mode_pipeline));
            mCurLedMode = Contrants.MODE_STREAM;
        }else if(mode.equals(Contrants.MODE_MUSIC)){
            vModeMusic.setSelected(true);
            vModeName.setText(getResources().getString(R.string.app_led_mode_musical));
            mCurLedMode = Contrants.MODE_MUSIC;
            colorPickerView.setVisibility(View.INVISIBLE);
            vGradientPan.setVisibility(View.INVISIBLE);
            vMusicBg.setVisibility(View.VISIBLE);
            findViewById(R.id.tv_recmd_txt).setVisibility(View.INVISIBLE);
            findViewById(R.id.ll_recmd_color_lay).setVisibility(View.INVISIBLE);
            vBottomSelectLine.setVisibility(View.INVISIBLE);
            //隐藏循环
            findViewById(R.id.iv_cycle_line).setVisibility(View.INVISIBLE);
            vCycleSwitchLay.setVisibility(View.INVISIBLE);
        }
        brightnessSlideBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                lastPointX = ColorPickerPreferenceManager.getInstance(LedSettingsActivity.this)
                        .getBrightnessSliderPosition("bright",colorPickerView.getSelectedPoint().x);
                Log.d("----==run","size="+lastPointX);
                brightnessSlideBar.updateSelectorX(lastPointX);
            }
        }, 200);
    }

    private void initLedSwitchStatue(){
        mLedSwitchStatue = SystemUtils.getProp("persist.led.switch","OFF").equals("ON");
        vModeLay.setVisibility(mLedSwitchStatue ? View.VISIBLE :View.INVISIBLE);
        findViewById(R.id.tv_led_bright_txt).setVisibility(mLedSwitchStatue ? View.VISIBLE :View.INVISIBLE);
        findViewById(R.id.ll_led_bright_lay).setVisibility(mLedSwitchStatue ? View.VISIBLE :View.INVISIBLE);
        findViewById(R.id.tv_recmd_txt).setVisibility(mLedSwitchStatue ? View.VISIBLE :View.INVISIBLE);
        findViewById(R.id.ll_recmd_color_lay).setVisibility(mLedSwitchStatue ? View.VISIBLE :View.INVISIBLE);

        vBottomSelectLine.setVisibility(mLedSwitchStatue ? View.VISIBLE :View.INVISIBLE);
        findViewById(R.id.fl_right_lay).setVisibility(mLedSwitchStatue ? View.VISIBLE :View.INVISIBLE);
        vSwitchImg.setBackgroundResource(mLedSwitchStatue ? R.mipmap.switch_on_bg_1920:R.mipmap.switch_off_bg_1920);
        vLedSwitchLeftPoint.setVisibility(mLedSwitchStatue ? View.VISIBLE:View.INVISIBLE);
        vLedSwitchRightPoint.setVisibility(mLedSwitchStatue ? View.INVISIBLE:View.VISIBLE);
        if(mLedSwitchStatue){

        }else{
            Light.getInstance().turnOff(); //关闭所有灯
        }
    }

    private void initCircleStatue(){
        mCircleSwitchStatue = SystemUtils.getProp("persist.circle.switch","OFF").equals("ON");
        vCircleImg.setBackgroundResource(mCircleSwitchStatue ? R.mipmap.switch_on_bg_1920:R.mipmap.switch_off_bg_1920);
        vCircleSwitchLeftPoint.setVisibility(mCircleSwitchStatue ? View.VISIBLE:View.INVISIBLE);
        vCircleSwitchRightPoint.setVisibility(mCircleSwitchStatue ? View.INVISIBLE:View.VISIBLE);
        Contrants.isCycle = mCircleSwitchStatue;
        if(Contrants.isCycle){  //该模式下禁用色盘
            colorPickerView.setEnabled(false);
            colorPickerView.setAlpha(0.5f);
            brightnessSlideBar.setAlpha(0.5f);
            setRecmdColorAlpha(0.5f);
        }else{
            colorPickerView.setEnabled(true);
            colorPickerView.setAlpha(1f);
            brightnessSlideBar.setAlpha(1f);
            setRecmdColorAlpha(1f);
        }
    }

    public void setRecmdColorAlpha(float alpha){
        vGradientIconColor1.setAlpha(alpha);
        vGradientIconColor2.setAlpha(alpha);
        vGradientIconColor3.setAlpha(alpha);
        vGradientIconColor4.setAlpha(alpha);
        vGradientIconColor5.setAlpha(alpha);
        vGradientIconColor6.setAlpha(alpha);
    }

    @Override
    protected void initFocusView() {
        mFocusViewList.clear();
        mFocusViewList.add(findViewById(R.id.left_home));
        mFocusViewList.add(vLedSwitchLay);
        mFocusViewList.add(vModeLay);
        if(vModeList.getVisibility() == View.VISIBLE){
            mFocusViewList.add(vModeSing);
            mFocusViewList.add(vModeGradient);
            mFocusViewList.add(vModeBreadh);
            mFocusViewList.add(vModeWater);
            mFocusViewList.add(vModeMusic);
        }
        mFocusViewList.add(findViewById(R.id.iv_minus));
        mFocusViewList.add(findViewById(R.id.iv_add));
        mFocusViewList.add(vGradientIconColor1);
        mFocusViewList.add(vGradientIconColor2);
        mFocusViewList.add(vGradientIconColor3);
        mFocusViewList.add(vGradientIconColor4);
        mFocusViewList.add(vGradientIconColor5);
        mFocusViewList.add(vGradientIconColor6);
    }

    @Override
    protected void onKnobUp() {

    }

    @Override
    protected void onKnobDown() {

    }

    @Override
    protected void onKnobLeft() {
        if(mFocusViewList.size() == 0){
            return;
        }
        mCurrentFocusViewIndex--;
        if (mCurrentFocusViewIndex < 0) {
            mCurrentFocusViewIndex = 0;
        }
        View viewLeft = mFocusViewList.get(mCurrentFocusViewIndex);
        if(viewLeft != null){
            viewLeft.requestFocus();
            viewLeft.requestFocusFromTouch();
        }
    }

    @Override
    protected void onKnobRight() {
        if(mFocusViewList.size() == 0){
            return;
        }
        mCurrentFocusViewIndex++;
        if (mCurrentFocusViewIndex > mFocusViewList.size() - 1) {
            mCurrentFocusViewIndex = mFocusViewList.size() - 1;
        }
        View viewRight = mFocusViewList.get(mCurrentFocusViewIndex);
        if(viewRight != null){
            viewRight.requestFocus();
            viewRight.requestFocusFromTouch();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fl_led_switch_lay:  //led开关
                SystemUtils.setProp("persist.led.switch",mLedSwitchStatue ? "OFF":"ON");
                initLedSwitchStatue();
                break;
            case R.id.fl_circle_switch_lay: //循环开关
                SystemUtils.setProp("persist.circle.switch",mCircleSwitchStatue ? "OFF":"ON");
                initCircleStatue();
                mService.turnOnForMode(mService.getMode(mCurLedMode),mService.getColors(mCurHexColor,mCurLedMode));
                //startActivity(new Intent(this, LightTestActivity.class));
                break;
            case R.id.left_home:
                finish();
                break;
            case R.id.ll_mode:  //mode
                if(vModeList.getVisibility() == View.VISIBLE){
                    vModeList.setVisibility(View.GONE);
                }else{
                    vModeList.setVisibility(View.VISIBLE);
                }
                initFocusView();
                break;
            case R.id.iv_add:
                if(!mCurLedMode.equals(Contrants.MODE_BREATH) && !Contrants.isCycle){
                    lastPointX = lastPointX + 20;
                    brightnessSlideBar.updateSelectorX(lastPointX);
                    colorPickerView.notifyColorChanged();
                }
                break;
            case R.id.iv_minus:
                if(!mCurLedMode.equals(Contrants.MODE_BREATH) && !Contrants.isCycle){
                    lastPointX = lastPointX - 20;
                    brightnessSlideBar.updateSelectorX(lastPointX);
                    colorPickerView.notifyColorChanged();
                }
                break;
            case R.id.iv_color1:
                switchRecmdColor(1);
                break;
            case R.id.iv_color2:
                switchRecmdColor(2);
                break;
            case R.id.iv_color3:
                switchRecmdColor(3);
                break;
            case R.id.iv_color4:
                switchRecmdColor(4);
                break;
            case R.id.iv_color5:
                switchRecmdColor(5);
                break;
            case R.id.iv_color6:
                switchRecmdColor(6);
                break;
            case R.id.ll_mode_sing:    //单色模式
                SystemUtils.setProp("persist.current.led.mode",Contrants.MODE_SING);
                vModeName.setText(getResources().getString(R.string.app_led_mode_monochrome));
                onAllModeNoSel(vModeSing, Contrants.MODE_SING);
                break;
            case R.id.ll_mode_gradient: //渐变模式
                SystemUtils.setProp("persist.current.led.mode",Contrants.MODE_GRADIENT);
                vModeName.setText(getResources().getString(R.string.app_led_mode_gradient));
                onAllModeNoSel(vModeGradient,Contrants.MODE_GRADIENT);
                break;
            case R.id.ll_mode_breath:   //呼吸模式
                SystemUtils.setProp("persist.current.led.mode",Contrants.MODE_BREATH);
                vModeName.setText(getResources().getString(R.string.app_led_mode_breathing));
                onAllModeNoSel(vModeBreadh,Contrants.MODE_BREATH);
                break;
            case R.id.ll_mode_water:   //流水模式
                SystemUtils.setProp("persist.current.led.mode",Contrants.MODE_STREAM);
                vModeName.setText(getResources().getString(R.string.app_led_mode_pipeline));
                onAllModeNoSel(vModeWater,Contrants.MODE_STREAM);
                break;
            case R.id.ll_mode_music:  //音谱模式
                SystemUtils.setProp("persist.current.led.mode",Contrants.MODE_MUSIC);
                vModeName.setText(getResources().getString(R.string.app_led_mode_musical));
                onAllModeNoSel(vModeMusic,Contrants.MODE_MUSIC);
                break;
        }
    }

    private void switchRecmdColor(int idnex){
        if(Contrants.isCycle && !mCurLedMode.equals(Contrants.MODE_GRADIENT)){
            return;
        }
        //底部线条
        Contrants.mColorBtnIndex = idnex-1;
        int lineMarStart = getResources().getDimensionPixelOffset(R.dimen.bottom_line_mar_left1);
        switch (idnex){
            case 1:
                if(isGradientMode){
                    vGradientPan.setImageResource(R.mipmap.gradient_sepan1);
                    mService.turnOnForMode(mService.getMode(mCurLedMode),mService.colorsMap.get(0));
                }else{
                    colorPickerView.setPureColor(Color.parseColor("#FF0000"));
                    colorPickerView.notifyColorChanged();
                }
                break;
            case 2:
                lineMarStart = getResources().getDimensionPixelOffset(R.dimen.bottom_line_mar_left2);
                if(isGradientMode){
                    vGradientPan.setImageResource(R.mipmap.gradient_sepan2);
                    mService.turnOnForMode(mService.getMode(mCurLedMode),mService.colorsMap.get(1));
                }else{
                    colorPickerView.setPureColor(Color.parseColor("#FF7D00"));
                    colorPickerView.notifyColorChanged();
                }
                break;
            case 3:
                lineMarStart = getResources().getDimensionPixelOffset(R.dimen.bottom_line_mar_left3);
                if(isGradientMode){
                    vGradientPan.setImageResource(R.mipmap.gradient_sepan3);
                    mService.turnOnForMode(mService.getMode(mCurLedMode),mService.colorsMap.get(2));
                }else{
                    colorPickerView.setPureColor(Color.parseColor("#F7A002"));
                    colorPickerView.notifyColorChanged();
                }
                break;
            case 4:
                lineMarStart = getResources().getDimensionPixelOffset(R.dimen.bottom_line_mar_left4);
                if(isGradientMode){
                    vGradientPan.setImageResource(R.mipmap.gradient_sepan4);
                    mService.turnOnForMode(mService.getMode(mCurLedMode),mService.colorsMap.get(3));
                }else{
                    colorPickerView.setPureColor(Color.parseColor("#2DAE18"));
                    colorPickerView.notifyColorChanged();
                }
                break;
            case 5:
                lineMarStart = getResources().getDimensionPixelOffset(R.dimen.bottom_line_mar_left5);
                if(isGradientMode){
                    vGradientPan.setImageResource(R.mipmap.gradient_sepan5);
                    mService.turnOnForMode(mService.getMode(mCurLedMode),mService.colorsMap.get(4));
                }else{
                    colorPickerView.setPureColor(Color.parseColor("#12A3B5"));
                    colorPickerView.notifyColorChanged();
                }
                break;
            case 6:
                lineMarStart = getResources().getDimensionPixelOffset(R.dimen.bottom_line_mar_left6);
                if(isGradientMode){
                    vGradientPan.setImageResource(R.mipmap.gradient_sepan6);
                    mService.turnOnForMode(mService.getMode(mCurLedMode),mService.colorsMap.get(5));
                }else{
                    colorPickerView.setPureColor(Color.parseColor("#3115C3"));
                    colorPickerView.notifyColorChanged();
                }
                break;
        }
        //更新线条位置
        RelativeLayout.LayoutParams bottomLine = (RelativeLayout.LayoutParams) vBottomSelectLine.getLayoutParams();
        bottomLine.setMarginStart(lineMarStart);
        vBottomSelectLine.setLayoutParams(bottomLine);
    }

    private void onAllModeNoSel(View curView, String mode){
        mCurLedMode = mode;
        vModeSing.setSelected(false);
        vModeGradient.setSelected(false);
        vModeBreadh.setSelected(false);
        vModeWater.setSelected(false);
        vModeMusic.setSelected(false);
        curView.setSelected(true);
        vModeList.setVisibility(View.GONE);
        initFocusView();
        vModeLay.requestFocus();
        switchRecomdIcon(mode);
    }

    private void switchRecomdIcon(String mode){
        isGradientMode = false;
        isMusicMode = false;
        //循环开关布局
        findViewById(R.id.iv_cycle_line).setVisibility(View.INVISIBLE);
        vCycleSwitchLay.setVisibility(View.INVISIBLE);
        brightnessSlideBar.setEnabled(true);
        brightnessSlideBar.setAlpha(1f);
        if(Contrants.MODE_GRADIENT.equals(mode)){  //渐变
            isGradientMode = true;
            colorPickerView.setVisibility(View.INVISIBLE);
            vGradientPan.setVisibility(View.VISIBLE);
            vMusicBg.setVisibility(View.INVISIBLE);
            vGradientIconColor1.setImageResource(R.mipmap.gradient_icon_color1);
            vGradientIconColor2.setImageResource(R.mipmap.gradient_icon_color2);
            vGradientIconColor3.setImageResource(R.mipmap.gradient_icon_color3);
            vGradientIconColor4.setImageResource(R.mipmap.gradient_icon_color4);
            vGradientIconColor5.setImageResource(R.mipmap.gradient_icon_color5);
            vGradientIconColor6.setImageResource(R.mipmap.gradient_icon_color6);
            setRecmdColorAlpha(1f);
            //Contrants.isCycle = false;  //关闭循环
            vGradientIconColor1.performClick();
        }else if(Contrants.MODE_MUSIC.equals(mode)){  //music
            isMusicMode = true;
            colorPickerView.setVisibility(View.INVISIBLE);
            vGradientPan.setVisibility(View.INVISIBLE);
            vMusicBg.setVisibility(View.VISIBLE);
            mService.turnOnForMode(mService.getMode(mCurLedMode),mService.getColors(mCurHexColor,mCurLedMode));
        }else {
            //循环开关布局
            findViewById(R.id.iv_cycle_line).setVisibility(View.VISIBLE);
            vCycleSwitchLay.setVisibility(View.VISIBLE);
            colorPickerView.setVisibility(View.VISIBLE);
            vGradientPan.setVisibility(View.INVISIBLE);
            vMusicBg.setVisibility(View.INVISIBLE);
            vGradientIconColor1.setImageResource(R.mipmap.icon_color_red_1);
            vGradientIconColor2.setImageResource(R.mipmap.icon_color_orange_2);
            vGradientIconColor3.setImageResource(R.mipmap.icon_color_yellow_3);
            vGradientIconColor4.setImageResource(R.mipmap.icon_color_green_4);
            vGradientIconColor5.setImageResource(R.mipmap.icon_color_cyan_5);
            vGradientIconColor6.setImageResource(R.mipmap.icon_color_purple_6);
            mService.turnOnForMode(mService.getMode(mCurLedMode),mService.getColors(mCurHexColor,mCurLedMode));
            if(Contrants.MODE_BREATH.equals(mode)){
                brightnessSlideBar.setEnabled(false);  //该模式不支持亮度调节
                brightnessSlideBar.setAlpha(0.5f);
            }
        }
        //音乐模式时隐藏推荐颜色相关
        findViewById(R.id.tv_recmd_txt).setVisibility(isMusicMode ? View.INVISIBLE :View.VISIBLE);
        findViewById(R.id.ll_recmd_color_lay).setVisibility(isMusicMode ? View.INVISIBLE :View.VISIBLE);
        vBottomSelectLine.setVisibility(isMusicMode ? View.INVISIBLE :View.VISIBLE);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            if (service != null) {
                mService = (LedUtil) service;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            mService = null;
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        ColorPickerPreferenceManager manager = ColorPickerPreferenceManager.getInstance(this);
        manager.setColor("MyColorPicker", colorPickerView.getColor());   //保存颜色
        manager.setBrightnessSliderPosition("bright", lastPointX);  //保存亮度
        if(mService != null){
            unbindService(connection);
        }
    }
}