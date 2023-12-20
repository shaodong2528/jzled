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

        vGradientPan = findViewById(R.id.iv_gradient);
        vModeList = findViewById(R.id.ll_mode_list);

        findViewById(R.id.iv_color1).setOnClickListener(this);
        findViewById(R.id.iv_color2).setOnClickListener(this);
        findViewById(R.id.iv_color3).setOnClickListener(this);
        findViewById(R.id.iv_color4).setOnClickListener(this);
        findViewById(R.id.iv_color5).setOnClickListener(this);
        findViewById(R.id.iv_color6).setOnClickListener(this);

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
        findViewById(R.id.iv_line).setVisibility(mLedSwitchStatue ? View.VISIBLE :View.INVISIBLE);
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
        mFocusViewList.add(findViewById(R.id.iv_color1));
        mFocusViewList.add(findViewById(R.id.iv_color2));
        mFocusViewList.add(findViewById(R.id.iv_color3));
        mFocusViewList.add(findViewById(R.id.iv_color4));
        mFocusViewList.add(findViewById(R.id.iv_color5));
        mFocusViewList.add(findViewById(R.id.iv_color6));
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
                colorPickerView.setPureColor(Color.parseColor("#FC2E28")); //红色
                colorPickerView.notifyColorChanged();
                break;
            case R.id.iv_color2:
                colorPickerView.setPureColor(Color.parseColor("#F66202")); //橙色
                colorPickerView.notifyColorChanged();
                break;
            case R.id.iv_color3:
                colorPickerView.setPureColor(Color.parseColor("#F7A002")); //黄色
                colorPickerView.notifyColorChanged();
                break;
            case R.id.iv_color4:
                colorPickerView.setPureColor(Color.parseColor("#2DAE18"));//绿色
                colorPickerView.notifyColorChanged();
                break;
            case R.id.iv_color5:
                colorPickerView.setPureColor(Color.parseColor("#12A3B5"));//蓝色
                colorPickerView.notifyColorChanged();
                break;
            case R.id.iv_color6:
                colorPickerView.setPureColor(Color.parseColor("#3E02F8"));//紫色
                colorPickerView.notifyColorChanged();
                break;
            case R.id.ll_mode_sing:
                SystemUtils.setProp("persist.current.led.mode",Contrants.MODE_SING);
                vModeName.setText(getResources().getString(R.string.app_led_mode_monochrome));
                onAllModeNoSel(vModeSing);
                break;
            case R.id.ll_mode_gradient:
                SystemUtils.setProp("persist.current.led.mode",Contrants.MODE_GRADIENT);
                vModeName.setText(getResources().getString(R.string.app_led_mode_gradient));
                onAllModeNoSel(vModeGradient);
                break;
            case R.id.ll_mode_breath:
                SystemUtils.setProp("persist.current.led.mode",Contrants.MODE_BREATH);
                vModeName.setText(getResources().getString(R.string.app_led_mode_breathing));
                onAllModeNoSel(vModeBreadh);
                break;
            case R.id.ll_mode_water:
                SystemUtils.setProp("persist.current.led.mode",Contrants.MODE_WATER);
                vModeName.setText(getResources().getString(R.string.app_led_mode_pipeline));
                onAllModeNoSel(vModeWater);
                break;
            case R.id.ll_mode_music:
                SystemUtils.setProp("persist.current.led.mode",Contrants.MODE_MUSIC);
                vModeName.setText(getResources().getString(R.string.app_led_mode_musical));
                onAllModeNoSel(vModeMusic);
                break;
        }
    }

    private void onAllModeNoSel(View curView){
        vModeSing.setSelected(false);
        vModeGradient.setSelected(false);
        vModeBreadh.setSelected(false);
        vModeWater.setSelected(false);
        vModeMusic.setSelected(false);
        curView.setSelected(true);
        vModeList.setVisibility(View.GONE);
        initFocusView();
        vModeLay.requestFocus();
    }

    @Override
    protected void onPause() {
        ColorPickerPreferenceManager manager = ColorPickerPreferenceManager.getInstance(this);
        manager.setColor("MyColorPicker", colorPickerView.getColor()); // manipulates the saved color data.
        super.onPause();
    }
}