package com.jz.led.settings;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.jz.led.utils.ThreadPoolUtils;
import com.jz.led.widget.AlphaSlideBar;
import com.jz.led.widget.BrightnessSlideBar;

import java.io.DataOutputStream;


public class TestDemoActivity extends BasicActivity implements View.OnClickListener {

    private boolean canClick = true;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initUI() {
        setContentView(R.layout.activity_test_demo);
        findViewById(R.id.left_home).setOnClickListener(this);
        findViewById(R.id.close).setOnClickListener(this);
        findViewById(R.id.red).setOnClickListener(this);
        findViewById(R.id.green).setOnClickListener(this);
        findViewById(R.id.blue).setOnClickListener(this);
        findViewById(R.id.cycle).setOnClickListener(this);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initFocusView() {
        mFocusViewList.add(findViewById(R.id.left_home));
        mFocusViewList.add(findViewById(R.id.close));
        mFocusViewList.add(findViewById(R.id.red));
        mFocusViewList.add(findViewById(R.id.green));
        mFocusViewList.add(findViewById(R.id.blue));
        mFocusViewList.add(findViewById(R.id.cycle));
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
            case R.id.left_home:
                finish();
                break;
            case R.id.close:
                //exec( new String[]{"/bin/sh","-c","echo hello >> ouput.txt"});
                command("echo \"\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\\0\" > /proc/led_ctrl_byte");
                break;
            case R.id.red:
                command("echo \"z\\0\\0z\\0\\0z\\0\\0z\\0\\0z\\0\\0z\\0\\0\" > /proc/led_ctrl_byte");
                break;
            case R.id.green:
                command("echo \"\\0z\\0\\0z\\0\\0z\\0\\0z\\0\\0z\\0\\0z\\0\" > /proc/led_ctrl_byte");
                break;
            case R.id.blue:
                command("echo \"\\0\\0z\\0\\0z\\0\\0z\\0\\0z\\0\\0z\\0\\0z\" > /proc/led_ctrl_byte");
                break;
            case R.id.cycle:
                command("echo \"z\\0\\0\\0z\\0\\0\\0zz\\0\\0\\0z\\0\\0\\0z\" > /proc/led_ctrl_byte");
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
                    process.destroy();
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