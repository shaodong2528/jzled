package com.jz.led.settings;

import android.content.Intent;
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
import com.jz.led.activity.LightTestActivity;
import com.jz.led.colorpick.ActionMode;
import com.jz.led.colorpick.ColorEnvelope;
import com.jz.led.colorpick.ColorPickerView;
import com.jz.led.colorpick.listeners.ColorEnvelopeListener;
import com.jz.led.colorpick.preference.ColorPickerPreferenceManager;
import com.jz.led.utils.Contrants;
import com.jz.led.utils.SystemUtils;
import com.jz.led.utils.ThreadPoolUtils;
import com.jz.led.widget.AlphaSlideBar;
import com.jz.led.widget.BrightnessSlideBar;

import java.io.DataOutputStream;


public class TestDemoActivity extends BasicActivity implements View.OnClickListener {

    private ColorPickerView colorPickerView;
    private BrightnessSlideBar brightnessSlideBar;
    private ImageView vSwitchImg,vCircleImg,vAdd,vMinus,vGradientPan;
    private LinearLayout vModeList;
    private LinearLayout vModeLay,vModeSing,vModeGradient,vModeBreadh,vModeWater,vModeMusic;
    private FrameLayout vLedSwitchLay,vCircleSiwtchLay;
    private ImageView vLedSwitchLeftPoint,vLedSwitchRightPoint,vCircleSwitchLeftPoint,vCircleSwitchRightPoint;
    private boolean mLedSwitchStatue,mCircleSwitchStatue;
    private TextView vModeName;
    private boolean canClick = true;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_test_demo);
        //setContentView(SystemUtils.getResourcesId("activity_test_demo", "layout"));
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
                    //vModeName.setTextColor(color);
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
        mLedSwitchStatue = SystemUtils.getProp("persist.led.switch","ON").equals("ON");
        vModeLay.setVisibility(mLedSwitchStatue ? View.VISIBLE :View.INVISIBLE);
        findViewById(R.id.tv_led_bright_txt).setVisibility(mLedSwitchStatue ? View.VISIBLE :View.INVISIBLE);
        findViewById(R.id.ll_led_bright_lay).setVisibility(mLedSwitchStatue ? View.VISIBLE :View.INVISIBLE);
        findViewById(R.id.tv_recmd_txt).setVisibility(mLedSwitchStatue ? View.VISIBLE :View.INVISIBLE);
        findViewById(R.id.ll_recmd_color_lay).setVisibility(mLedSwitchStatue ? View.VISIBLE :View.INVISIBLE);
        findViewById(R.id.iv_line).setVisibility(/*mLedSwitchStatue ? View.VISIBLE :*/View.INVISIBLE);
        findViewById(R.id.fl_right_lay).setVisibility(/*mLedSwitchStatue ? View.VISIBLE :*/View.INVISIBLE);
        vSwitchImg.setBackgroundResource(mLedSwitchStatue ? R.mipmap.switch_on_bg_1920:R.mipmap.switch_off_bg_1920);
        vLedSwitchLeftPoint.setVisibility(mLedSwitchStatue ? View.VISIBLE:View.INVISIBLE);
        vLedSwitchRightPoint.setVisibility(mLedSwitchStatue ? View.INVISIBLE:View.VISIBLE);
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
    protected void onKnobUp() {   }

    @Override
    protected void onKnobDown() {   }

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
            case R.id.left_home:
                finish();
                break;
            case R.id.iv_color1:
                command("echo \"z\\0\\0z\\0\\0z\\0\\0z\\0\\0z\\0\\0z\\0\\0\" > /proc/led_ctrl_byte");  //红
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
                startActivity(new Intent(this, LightTestActivity.class));
                break;
            case R.id.iv_color4:
                command("echo \"\\0z\\0\\0z\\0\\0z\\0\\0z\\0\\0z\\0\\0z\\0\" > /proc/led_ctrl_byte");
                colorPickerView.setPureColor(Color.parseColor("#2DAE18"));//绿色
                colorPickerView.notifyColorChanged();
                break;
            case R.id.iv_color5:
                colorPickerView.setPureColor(Color.parseColor("#12A3B5"));//青色
                colorPickerView.notifyColorChanged();
                break;
            case R.id.iv_color6:
                command("echo \"\\0\\0z\\0\\0z\\0\\0z\\0\\0z\\0\\0z\\0\\0z\" > /proc/led_ctrl_byte");
                colorPickerView.setPureColor(Color.parseColor("#3E02F8"));//蓝色
                colorPickerView.notifyColorChanged();
                break;
        }
    }

    private void command(String cmd) {
        if(!canClick){
            Toast.makeText(this, "正在写入，请稍候", Toast.LENGTH_SHORT).show();
            return;
        }
        ThreadPoolUtils.getCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    canClick = false;
                    Log.d("===zxd","开始写入");
                    Process process = null;
                    DataOutputStream dos = null;
                    process = Runtime.getRuntime().exec("sh");
                    dos = new DataOutputStream(process.getOutputStream());
                    dos.writeBytes(cmd);
                    dos.flush();
                    //process.destroy();
                    dos.close();
                    Log.d("===zxd","写入完成");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("===zxd","写入命令异常:"+e.getMessage());
                }finally {
                    canClick = true;
                }
            }
        });
    }
}