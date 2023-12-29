package com.jz.led.activity;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;


import com.jz.led.light.ILight;
import com.jz.led.light.Light;
import com.jz.led.settings.R;

import java.util.ArrayList;

public class LightTestActivity extends Activity {

    private ILight mLight;

    private Button mBtn;
    private Button mBtnOff4Number;
    private boolean isOpen = false;

    private enum LightMode {
        NORMAL, BLINK, STREAM, BREATHE, SINGLE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.light_test);

        mBtn = (Button) findViewById(R.id.btn);
        Button exit = (Button) findViewById(R.id.close);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mBtn.setOnClickListener(view -> {
            if (mLight == null) {
                return;
            }

            isOpen = !isOpen;

            if (!isOpen) {
                turnOff();
                mBtn.setText(R.string.btn_open);
                return;
            }

            mBtn.setText(R.string.btn_close);
            LightMode mode = getMode();
            turnOnForMode(mode);
        });

        ((RadioGroup) findViewById(R.id.radio_mode)).setOnCheckedChangeListener((radioGroup, checkedId) -> {
            if (!isOpen) return;
            LightMode mode = getMode(checkedId);
            turnOnForMode(mode);
        });

        mBtnOff4Number = (Button) findViewById(R.id.btn_turn_off);
        mBtnOff4Number.setOnClickListener(view -> {
            int index = getTurnoffNumber();
            turnOffForNumber(index);
        });

        mLight = Light.getInstance();
        isOpen = false;
        turnOff();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        turnOff();
    }

    private int[] getRGBS() {
        String rgbHex = ((EditText) findViewById(R.id.rgb)).getText().toString();
        String rgb1Hex = ((EditText) findViewById(R.id.rgb1)).getText().toString();
        String rgb2Hex = ((EditText) findViewById(R.id.rgb2)).getText().toString();
        String rgb3Hex = ((EditText) findViewById(R.id.rgb3)).getText().toString();
        String rgb4Hex = ((EditText) findViewById(R.id.rgb4)).getText().toString();
        String rgb5Hex = ((EditText) findViewById(R.id.rgb5)).getText().toString();

        ArrayList<Integer> rgbList = new ArrayList<>();

        if (!rgbHex.isEmpty()) {
            rgbList.add(Integer.parseInt(rgbHex, 16));
        }

        if (!rgb1Hex.isEmpty()) {
            rgbList.add(Integer.parseInt(rgb1Hex, 16));
        }

        if (!rgb2Hex.isEmpty()) {
            rgbList.add(Integer.parseInt(rgb2Hex, 16));
        }

        if (!rgb3Hex.isEmpty()) {
            rgbList.add(Integer.parseInt(rgb3Hex, 16));
        }

        if (!rgb4Hex.isEmpty()) {
            rgbList.add(Integer.parseInt(rgb4Hex, 16));
        }

        if (!rgb5Hex.isEmpty()) {
            rgbList.add(Integer.parseInt(rgb5Hex, 16));
        }

        int[] rgbs = new int[rgbList.size()];
        for (int i = 0; i < rgbs.length; i++) {
            rgbs[i] = rgbList.get(i);
        }
        return rgbs;
    }

    private int getBlinkOpenDuration() {
        String duration = ((EditText) findViewById(R.id.blink_open)).getText().toString();
        return Integer.parseInt(duration);
    }

    private int getBlinkCloseDuration() {
        String duration = ((EditText) findViewById(R.id.blink_close)).getText().toString();
        return Integer.parseInt(duration);
    }

    private int getStreamSpeed() {
        String duration = ((EditText) findViewById(R.id.stream_speed)).getText().toString();
        return Integer.parseInt(duration);
    }

    private int getBreatheDuration() {
        String duration = ((EditText) findViewById(R.id.breathe_duration)).getText().toString();
        return Integer.parseInt(duration);
    }

    private LightMode getMode() {
        int checkedId = ((RadioGroup) findViewById(R.id.radio_mode)).getCheckedRadioButtonId();
        return getMode(checkedId);
    }

    public LightMode getMode(int radioBtnId) {
        switch (radioBtnId) {
            case R.id.radio_mode_blink:
                return LightMode.BLINK;
            case R.id.radio_mode_stream:
                return LightMode.STREAM;
            case R.id.radio_mode_breathe:
                return LightMode.BREATHE;
            case R.id.radio_mode_single:
                return LightMode.SINGLE;
            case R.id.radio_mode_normal:
            default:
                return LightMode.NORMAL;
        }
    }

    private int getTurnoffNumber() {
        String number = ((EditText) findViewById(R.id.turn_off_index)).getText().toString();
        return Integer.parseInt(number);
    }

    private void turnOnForMode(LightMode mode) {
        findViewById(R.id.control_special).setVisibility(View.INVISIBLE);
        switch (mode) {
            case BLINK:
                blink();
                break;
            case STREAM:
                stream();
                break;
            case BREATHE:
                breathe();
                break;
            case SINGLE:
                turnOnEach();
                findViewById(R.id.control_special).setVisibility(View.VISIBLE);
                break;
            case NORMAL:
            default:
                turnOn();
                break;
        }
    }

    private void blink() {
        if (mLight == null) return;
        int openDuration = getBlinkOpenDuration();
        int closeDuration = getBlinkCloseDuration();
        mLight.blink(getRGBS(), openDuration, closeDuration);
    }

    private void stream() {
        if (mLight == null) return;
        int speed = getStreamSpeed();
        mLight.stream(getRGBS(), speed);
    }

    private void breathe() {
        if (mLight == null) return;
        int duration = getBreatheDuration();
        mLight.breathe(getRGBS(), duration);
    }

    private void turnOn() {
        if (mLight == null) return;
        mLight.turnOn(getRGBS());
    }

    private void turnOff() {
        if (mLight == null) return;
        mLight.turnOff();
    }

    private void turnOnEach() {
        if (mLight == null) return;

        int[] rgbs = getRGBS();
        for (int i = 0; i < rgbs.length; i++) {
            mLight.turnOnOne(rgbs[i], i);
        }
    }

    private void turnOffForNumber(int index) {
        if (mLight == null) return;
        mLight.turnOffOne(index);
    }
}