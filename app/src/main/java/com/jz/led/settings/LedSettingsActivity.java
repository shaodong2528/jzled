package com.jz.led.settings;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.jz.led.activity.BasicActivity;
import com.jz.led.colorpick.ActionMode;
import com.jz.led.colorpick.ColorEnvelope;
import com.jz.led.colorpick.ColorPickerView;
import com.jz.led.colorpick.listeners.ColorEnvelopeListener;
import com.jz.led.colorpick.preference.ColorPickerPreferenceManager;
import com.jz.led.utils.Contrants;
import com.jz.led.utils.LedUtil;
import com.jz.led.utils.SystemUtils;
import com.jz.led.widget.AlphaSlideBar;
import com.jz.led.widget.BrightnessSlideBar;


public class LedSettingsActivity extends BasicActivity implements View.OnClickListener {
    private ColorPickerView colorPickerView;
    private BrightnessSlideBar brightnessSlideBar;
    private ImageView vSwitchImg,vCircleImg,vAdd,vMinus,vGradientPan;
    private LinearLayout vModeList;
    private LinearLayout vModeLay,vModeSing,vModeGradient,vModeBreadh,vModeWater,vModeMusic;
    private FrameLayout vLedSwitchLay,vCircleSiwtchLay;
    private ImageView vLedSwitchLeftPoint,vLedSwitchRightPoint,vCircleSwitchLeftPoint,vCircleSwitchRightPoint;
    private boolean mLedSwitchStatue,mCircleSwitchStatue;
    private TextView vModeName;
    private ImageView vGradientIconColor1,vGradientIconColor2,vGradientIconColor3,vGradientIconColor4,vGradientIconColor5,vGradientIconColor6;
    private ImageView vMusicBg;
    private boolean isGradientMode;  //是否渐变模式
    private boolean isMusicMode;  //是否音乐模式
    private ImageView vBottomSelectLine; //底部推荐颜色线


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
        vCircleSiwtchLay = findViewById(R.id.fl_circle_switch_lay);
        vCircleSiwtchLay.setOnClickListener(this);
        vCircleImg = findViewById(R.id.iv_cycle_switch);
        vCircleSwitchLeftPoint = findViewById(R.id.iv_cycle_point_left);
        vCircleSwitchRightPoint = findViewById(R.id.iv_cycle_point_right);

        colorPickerView = findViewById(R.id.colorPickerView);
        colorPickerView.setActionMode(ActionMode.LAST); //手指释放回调监听
        ColorPickerPreferenceManager manager = ColorPickerPreferenceManager.getInstance(this);
        int color = manager.getColor("MyColorPicker",0);
        Point point = manager.getSelectorPosition("savepoint",new Point(100,100));
        int bright = manager.getBrightnessSliderPosition("savebright",0);
        Log.d("===zzz","color="+color+",px="+point.x+",py="+point.y+",bright="+bright);
        colorPickerView.setInitialColor(color);
        colorPickerView.setColorListener(new ColorEnvelopeListener() {
            @Override
            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                if(fromUser){
                    int color = envelope.getColor();
                    String hexColor = envelope.getHexCode();
                    int argb[] = envelope.getArgb();
                    Log.d("===zzz","fromUser="+fromUser+",color="+color+",hexColor="+hexColor+",rbgColor="+argb[0]+","+argb[1]+","+argb[2]+","+argb[3]);
                    vModeName.setTextColor(color);
                }
            }
        });
        brightnessSlideBar = findViewById(R.id.brightnessSlide);
        colorPickerView.attachBrightnessSlider(brightnessSlideBar);


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
        initLedSwitchStatue();
        initCircleStatue();
        String mode = SystemUtils.getProp("persist.current.led.mode","");
        if(mode.equals(Contrants.MODE_SING)){
            vModeSing.setSelected(true);
            vModeName.setText(getResources().getString(R.string.app_led_mode_monochrome));
        }else if(mode.equals(Contrants.MODE_GRADIENT)){
            vModeGradient.setSelected(true);
            vModeName.setText(getResources().getString(R.string.app_led_mode_gradient));
        }else if(mode.equals(Contrants.MODE_BREATH)){
            vModeBreadh.setSelected(true);
            vModeName.setText(getResources().getString(R.string.app_led_mode_breathing));
        }else if(mode.equals(Contrants.MODE_WATER)){
            vModeWater.setSelected(true);
            vModeName.setText(getResources().getString(R.string.app_led_mode_pipeline));
        }else if(mode.equals(Contrants.MODE_MUSIC)){
            vModeMusic.setSelected(true);
            vModeName.setText(getResources().getString(R.string.app_led_mode_musical));
        }
    }

    private void initLedSwitchStatue(){
        mLedSwitchStatue = SystemUtils.getProp("persist.led.switch","OFF").equals("ON");
        vModeLay.setVisibility(mLedSwitchStatue ? View.VISIBLE :View.INVISIBLE);
        findViewById(R.id.tv_led_bright_txt).setVisibility(mLedSwitchStatue ? View.VISIBLE :View.INVISIBLE);
        findViewById(R.id.ll_led_bright_lay).setVisibility(mLedSwitchStatue ? View.VISIBLE :View.INVISIBLE);
        findViewById(R.id.tv_recmd_txt).setVisibility(mLedSwitchStatue ? View.VISIBLE :View.INVISIBLE);
        findViewById(R.id.ll_recmd_color_lay).setVisibility(mLedSwitchStatue ? View.VISIBLE :View.INVISIBLE);
        vBottomSelectLine = findViewById(R.id.iv_line);
        vBottomSelectLine.setVisibility(mLedSwitchStatue ? View.VISIBLE :View.INVISIBLE);
        findViewById(R.id.fl_right_lay).setVisibility(mLedSwitchStatue ? View.VISIBLE :View.INVISIBLE);
        vSwitchImg.setBackgroundResource(mLedSwitchStatue ? R.mipmap.switch_on_bg_1920:R.mipmap.switch_off_bg_1920);
        vLedSwitchLeftPoint.setVisibility(mLedSwitchStatue ? View.VISIBLE:View.INVISIBLE);
        vLedSwitchRightPoint.setVisibility(mLedSwitchStatue ? View.INVISIBLE:View.VISIBLE);
    }

    private void initCircleStatue(){
        mCircleSwitchStatue = SystemUtils.getProp("persist.circle.switch","OFF").equals("ON");
        vCircleImg.setBackgroundResource(mCircleSwitchStatue ? R.mipmap.switch_on_bg_1920:R.mipmap.switch_off_bg_1920);
        vCircleSwitchLeftPoint.setVisibility(mCircleSwitchStatue ? View.VISIBLE:View.INVISIBLE);
        vCircleSwitchRightPoint.setVisibility(mCircleSwitchStatue ? View.INVISIBLE:View.VISIBLE);
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

                break;
            case R.id.iv_minus:

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
                SystemUtils.setProp("persist.current.led.mode",Contrants.MODE_WATER);
                vModeName.setText(getResources().getString(R.string.app_led_mode_pipeline));
                onAllModeNoSel(vModeWater,Contrants.MODE_WATER);
                break;
            case R.id.ll_mode_music:  //音谱模式
                SystemUtils.setProp("persist.current.led.mode",Contrants.MODE_MUSIC);
                vModeName.setText(getResources().getString(R.string.app_led_mode_musical));
                onAllModeNoSel(vModeMusic,Contrants.MODE_MUSIC);
                break;
        }
    }

    private void switchRecmdColor(int idnex){
        //底部线条
        int lineMarStart = getResources().getDimensionPixelOffset(R.dimen.bottom_line_mar_left1);
        switch (idnex){
            case 1:
                if(isGradientMode){
                    vGradientPan.setImageResource(R.mipmap.gradient_sepan1);
                }else{
                    colorPickerView.setPureColor(Color.parseColor("#FC2E28"));
                    colorPickerView.notifyColorChanged();
                }
                break;
            case 2:
                lineMarStart = getResources().getDimensionPixelOffset(R.dimen.bottom_line_mar_left2);
                if(isGradientMode){
                    vGradientPan.setImageResource(R.mipmap.gradient_sepan2);
                }else{
                    colorPickerView.setPureColor(Color.parseColor("#F66202"));
                    colorPickerView.notifyColorChanged();
                }
                break;
            case 3:
                lineMarStart = getResources().getDimensionPixelOffset(R.dimen.bottom_line_mar_left3);
                if(isGradientMode){
                    vGradientPan.setImageResource(R.mipmap.gradient_sepan3);
                }else{
                    colorPickerView.setPureColor(Color.parseColor("#F7A002"));
                    colorPickerView.notifyColorChanged();
                }
                break;
            case 4:
                lineMarStart = getResources().getDimensionPixelOffset(R.dimen.bottom_line_mar_left4);
                if(isGradientMode){
                    vGradientPan.setImageResource(R.mipmap.gradient_sepan4);
                }else{
                    colorPickerView.setPureColor(Color.parseColor("#2DAE18"));
                    colorPickerView.notifyColorChanged();
                }
                break;
            case 5:
                lineMarStart = getResources().getDimensionPixelOffset(R.dimen.bottom_line_mar_left5);
                if(isGradientMode){
                    vGradientPan.setImageResource(R.mipmap.gradient_sepan5);
                }else{
                    colorPickerView.setPureColor(Color.parseColor("#12A3B5"));
                    colorPickerView.notifyColorChanged();
                }
                break;
            case 6:
                lineMarStart = getResources().getDimensionPixelOffset(R.dimen.bottom_line_mar_left6);
                if(isGradientMode){
                    vGradientPan.setImageResource(R.mipmap.gradient_sepan6);
                }else{
                    colorPickerView.setPureColor(Color.parseColor("#3E02F8"));
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
        }else if(Contrants.MODE_MUSIC.equals(mode)){  //music
            isMusicMode = true;
            colorPickerView.setVisibility(View.INVISIBLE);
            vGradientPan.setVisibility(View.INVISIBLE);
            vMusicBg.setVisibility(View.VISIBLE);
        }else {
            colorPickerView.setVisibility(View.VISIBLE);
            vGradientPan.setVisibility(View.INVISIBLE);
            vMusicBg.setVisibility(View.INVISIBLE);
            vGradientIconColor1.setImageResource(R.mipmap.icon_color_red_1);
            vGradientIconColor2.setImageResource(R.mipmap.icon_color_orange_2);
            vGradientIconColor3.setImageResource(R.mipmap.icon_color_yellow_3);
            vGradientIconColor4.setImageResource(R.mipmap.icon_color_green_4);
            vGradientIconColor5.setImageResource(R.mipmap.icon_color_cyan_5);
            vGradientIconColor6.setImageResource(R.mipmap.icon_color_purple_6);
        }
        //音乐模式时隐藏推荐颜色相关
        findViewById(R.id.tv_recmd_txt).setVisibility(isMusicMode ? View.INVISIBLE :View.VISIBLE);
        findViewById(R.id.ll_recmd_color_lay).setVisibility(isMusicMode ? View.INVISIBLE :View.VISIBLE);
        vBottomSelectLine.setVisibility(isMusicMode ? View.INVISIBLE :View.VISIBLE);
    }

    @Override
    protected void onPause() {
        ColorPickerPreferenceManager manager = ColorPickerPreferenceManager.getInstance(this);
        manager.setColor("MyColorPicker", colorPickerView.getColor()); // manipulates the saved color data.
        super.onPause();
    }
}