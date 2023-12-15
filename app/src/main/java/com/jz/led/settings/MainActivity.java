package com.jz.led.settings;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.media.audiofx.Visualizer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.jz.led.colorpick.ActionMode;
import com.jz.led.colorpick.ColorEnvelope;
import com.jz.led.colorpick.ColorPickerView;
import com.jz.led.colorpick.listeners.ColorEnvelopeListener;
import com.jz.led.colorpick.preference.ColorPickerPreferenceManager;
import com.jz.led.widget.AlphaSlideBar;
import com.jz.led.widget.BrightnessSlideBar;


public class MainActivity extends AppCompatActivity {
    private ColorPickerView colorPickerView;
    private BrightnessSlideBar brightnessSlideBar;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tv1 = findViewById(R.id.test1);
        TextView tv2 = findViewById(R.id.test2);
        TextView tv3 = findViewById(R.id.test3);
        colorPickerView = findViewById(R.id.colorPickerView);
        colorPickerView.setActionMode(ActionMode.LAST); //手指释放回调监听
        ColorPickerPreferenceManager manager = ColorPickerPreferenceManager.getInstance(this);
        int color = manager.getColor("MyColorPicker",0);
        Point point = manager.getSelectorPosition("savepoint",new Point(100,100));
        int bright = manager.getBrightnessSliderPosition("savebright",0);
        Log.d("===zzz","color="+color+",px="+point.x+",py="+point.y+",bright="+bright);
        //colorPickerView.setSelectorPoint(point.x,point.y);
        colorPickerView.setInitialColor(color);
        colorPickerView.setColorListener(new ColorEnvelopeListener() {
            @Override
            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                int color = envelope.getColor();
                String hexColor = envelope.getHexCode();
                int argb[] = envelope.getArgb();

                Log.d("===zzz","fromUser="+fromUser+",color="+color+",hexColor="+hexColor+",rbgColor="+argb[0]+","+argb[1]+","+argb[2]+","+argb[3]);

                tv1.setBackgroundColor(color);
                tv2.setBackgroundColor(Color.parseColor("#"+hexColor));
                tv3.setBackgroundColor(Color.rgb(argb[1],argb[2],argb[3]));
            }
        });


        AlphaSlideBar alphaSlideBar = findViewById(R.id.alphaSlideBar);
        colorPickerView.attachAlphaSlider(alphaSlideBar);

        //亮度调节
        // attach brightnessSlideBar
        brightnessSlideBar = findViewById(R.id.brightnessSlide);
        colorPickerView.attachBrightnessSlider(brightnessSlideBar);
    }

    @Override
    protected void onPause() {
        ColorPickerPreferenceManager manager = ColorPickerPreferenceManager.getInstance(this);
        manager.setColor("MyColorPicker", colorPickerView.getColor()); // manipulates the saved color data.
        //manager.setSelectorPosition("savepoint",colorPickerView.getSelectedPoint());
        //manager.setBrightnessSliderPosition("savebright",brightnessSlideBar.getColor());
        super.onPause();
    }

}