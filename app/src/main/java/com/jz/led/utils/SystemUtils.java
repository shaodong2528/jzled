package com.jz.led.utils;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;

import com.jz.led.MainApplication;
import com.jz.led.settings.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName SystemUtils
 * @Author xuexb
 * @Date 2022/5/6 10:37
 */
public class SystemUtils {

    private static final String TAG = "SystemUtils";

    public static int getScreenWidth() {
        return Integer.parseInt(SystemUtil.getProp("pandora.viewarea_width", "0"));
    }

    public static int getScreenHeight() {
        return Integer.parseInt(SystemUtil.getProp("pandora.viewarea_height", "0"));
    }

    public static int getCellLayoutBottomPadding() {//xxb1
        float ratio = getScreenWidth()/1.0f/getScreenHeight()/1.0f;
        String layout_prefix = "";
        int bottom = MainApplication.getContext().getResources().getDimensionPixelOffset(R.dimen.y192);
        if(ratio > 1.94 && ratio < 2.3){
            bottom = MainApplication.getContext().getResources().getDimensionPixelOffset(R.dimen.yy176);
        }else if(ratio > 2.3){
            bottom = MainApplication.getContext().getResources().getDimensionPixelOffset(R.dimen.y192);
        }
        return bottom;
    }

    public static int getRatio() {
        float ratio = getScreenWidth()/1.0f/getScreenHeight()/1.0f;
        String layout_prefix = "";
        int mRatio = 0;
        if(ratio > 1.94 && ratio < 2.3){
            //mRatio = 1;      //1366
            mRatio = 3;        //id8 新分辨率1920x960暂时同1920x720
        }else if(ratio > 2.6){
            mRatio = 2;         //1920x720
        }else if(ratio > 2.3){
            mRatio = 3;         //ID8 1920x812
        }
        return mRatio;
    }

    public static int getResourcesId(String name, String type) {
        float ratio = getScreenWidth()/1.0f/getScreenHeight()/1.0f;
        String layout_prefix = "";
        if(ratio > 1.94 && ratio < 2.3){
            //layout_prefix = "land_1366_640_";
            layout_prefix = "land_1920_812_";  //id8 新分辨率1920x960暂时同1920x720
        }else if (ratio > 2.6) {
            layout_prefix = "land_1920_720_";
        } else if(ratio > 2.3){
            layout_prefix = "land_1920_812_";
        }else{
            layout_prefix = "land_";
        }

        return getResourcesId(layout_prefix, name, type);
    }

    public static int getResourcesId(String resources_id_prefix, String name, String type) {
        Resources resources = MainApplication.getContext().getResources();
        String packageName = MainApplication.getContext().getPackageName();
        int layout = resources.getIdentifier(resources_id_prefix + name, type, packageName);

        if (layout == 0) {
            layout = resources.getIdentifier(name, type, packageName);
        }

        if (layout == 0) {
            layout = resources.getIdentifier("land_" + name, type, packageName);
        }

        if (layout == 0 && !TextUtils.isEmpty(resources_id_prefix)) {
           // layout = resources.getIdentifier(name, type+"-nodpi", packageName);
        }

        if (layout == 0 && !TextUtils.isEmpty(resources_id_prefix)) {
           // layout = resources.getIdentifier(name, type+"-anydpi", packageName);
        }

        return layout;
    }

    public static Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    public static void launchAppDetail(String appPkg, String marketPkg,Context mContext) {
        try {
            if (TextUtils.isEmpty(appPkg)) return;
            Uri uri = Uri.parse("market://details?id=" + appPkg);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (!TextUtils.isEmpty(marketPkg)) {
                intent.setPackage(marketPkg);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void RunApp(Context context, String packageNam,String className) {
        Intent intent = null;
        try {
            intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName(packageNam,className);
            intent.setComponent(cn);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.i(TAG, "RunApp with packageName error: " + e.toString());
        }
    }

    public static Intent RunApp1(Context context, String packageName) {
        Intent intent = null;
        try {
            if (packageName != null) {
                PackageManager pm = context.getPackageManager();
                intent = pm.getLaunchIntentForPackage(packageName);
            }
        } catch (Exception e) {
            Log.i(TAG, "RunApp with packageName error: " + e.toString());
        }
        return intent;
    }


    public static void setOnClickListener(View view,Intent intent,Context mContext) {
        if(view == null){
            return;
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mContext.startActivity(intent);
                }catch (Exception e){
                }
            }
        });
    }

    public static void setOnClickListener(View view,String name,String className,Context mContext) {
        if(view == null){
            return;
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RunApp(mContext, name,className);
            }
        });
    }

    public static void setOnClickListener2(View view,String action,Context mContext) {
        if(view == null){
            return;
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent();
                    intent.setAction(action);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }catch (Exception e){

                }
            }
        });
    }

    public static synchronized void sendKeyCode(final int keyCode) {
        new Thread() {
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(keyCode);
                } catch (Exception e) {
                }
            }
        }.start();
    }

    public static String getLauncherMode(Context mContext){
        String mode = Settings.Secure.getString(mContext.getContentResolver(), "launcher_mode");
        if(TextUtils.isEmpty(mode)){
            mode = "0";
        }
        return mode;
    }

    //读取文件内容
    public static String readData(String path) {
        createFile(path);
        StringBuilder res = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(path));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                res.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                closeAll(bufferedReader);
            }
        }
        return res.toString();
    }

    //往文件写入内容
    public static void writeData(String path, String value) {
        createFile(path);
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(path));
            bufferedWriter.write(value);
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                closeAll(bufferedWriter);
            }
        }
        sync();
    }

    public static File createFile(String path){
        File file = new File(path);
        File fileParent = file.getParentFile();
        if(!fileParent.exists()){
            fileParent.mkdirs();
        }
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        sync();
        return file;
    }

    public static void sync(){
        try {
            Runtime.getRuntime().exec("sync");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //关闭流
    public static void closeAll(Closeable...ables){
        for(Closeable c : ables){
            if(c != null){
                try {
                    c.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //获取系统属性
    public static String getProp(String key) {
        String value = (String) Reflex.invokeStaticMethod("android.os.SystemProperties", "get",
                new Class[]{String.class, String.class}, new String[]{key, ""});
        return value;
    }

    //获取系统属性
    public static String getProp(String key, String def) {
        String value = (String) Reflex.invokeStaticMethod("android.os.SystemProperties", "get",
                new Class[]{String.class, String.class}, new String[]{key, def});
        return value;
    }

    //设置系统属性
    public static void setProp(String key ,String value) {
        Reflex.invokeStaticMethod("android.os.SystemProperties","set",
                new Class[]{String.class,String.class},new String[]{key,value});
    }

    public static int getImgResources(String name,String type) {
        float ratio = getScreenWidth()/1.0f/getScreenHeight()/1.0f;
        String layout_prefix = "";
        if(ratio > 1.94 && ratio < 2.3){
            layout_prefix = "_1024";
        }else if(ratio > 2.3){
            layout_prefix = "_1280";
        }
        Resources resources = MainApplication.getContext().getResources();
        String packageName = MainApplication.getContext().getPackageName();
        int layout = resources.getIdentifier(name+layout_prefix, type, packageName);
        if (layout == 0) {
            layout = resources.getIdentifier(name, type, packageName);
        }
        return layout;
    }



    public static boolean hasSimCard(Context context) {
        TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telMgr.getSimState();
        boolean result = true;
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
            case TelephonyManager.SIM_STATE_UNKNOWN:
                result = false;
                break;
        }
        return result;
    }

    //移动开关
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void setMobileDataEnabled(boolean enabled,Context mContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getTelephonyManager(mContext).setDataEnabled(enabled);
        }
    }


    //获取热点名称
    public static String getWifiApName(Context context){
        String ssid = "";
        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            Method method = manager.getClass().getDeclaredMethod("getWifiApConfiguration");
            WifiConfiguration configuration = (WifiConfiguration) method.invoke(manager);
            ssid = configuration.SSID;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ssid;
    }

    //获取热点密码
    public static String getValidPassword(Context context){
        try {
            WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
            WifiConfiguration configuration = (WifiConfiguration)method.invoke(mWifiManager);
            return configuration.preSharedKey;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @VisibleForTesting
    public static TelephonyManager getTelephonyManager(Context mContext) {
        int subscriptionId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
        if (!SubscriptionManager.isValidSubscriptionId(subscriptionId)) {
            subscriptionId = SubscriptionManager.getDefaultDataSubscriptionId();
        }
        if (!SubscriptionManager.isValidSubscriptionId(subscriptionId)) {
            SubscriptionManager subscriptionManager = SubscriptionManager.from(mContext);
            int[] activeSubIds = (int[]) Reflex.invokeInstanceMethod(subscriptionManager,
                    "getActiveSubscriptionIdList");
            if (!isEmpty(activeSubIds)) {
                subscriptionId = activeSubIds[0];
            }
        }
        TelephonyManager mTelephonyManager = (TelephonyManager) Reflex.invokeStaticMethod(TelephonyManager.class,
                "from",new Class[]{Context.class},new Object[]{mContext});
        return mTelephonyManager.createForSubscriptionId(subscriptionId);
    }

    public static boolean isEmpty(@androidx.annotation.Nullable int[] array) {
        return array == null || array.length == 0;
    }




    //遍历一个目录下的所有子目录
    public static void getAllFiles(File root,List<String> mList){
        File files[] = root.listFiles();
        if(files != null){
            for (File f : files){
                if(f.isDirectory()){
                    getAllFiles(f,mList);
                }else{
                    boolean isVideo = f.isFile() && isVideoFile(f.getName().toUpperCase());
                    if(isVideo) {
                        mList.add(f.getAbsolutePath());
                    }
                }
            }
        }
    }

    public static boolean isVideoFile(String type){
        return type.endsWith(".MP4")||
                type.endsWith(".M4V")||
                type.endsWith(".3GPP")||
                type.endsWith(".3GPP2")||
                type.endsWith(".WMV")||
                type.endsWith(".MKV")||
                type.endsWith(".ASF")||
                type.endsWith(".MP2TS")||
                type.endsWith(".AVI")||
                type.endsWith(".MOV")||
                type.endsWith(".WEBM");
    }

    //key MD5将加密
    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    //byte转化16进制
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    //获取视频截图1
    public static Bitmap getVideoThumbnail1(String videoPath) {
        Bitmap bitmap = getVideoThumbnail(videoPath,100,100,5);
        return bitmap;
    }

    //获取视频截图
    public static Bitmap getVideoThumbnail(String filePath,int w1, int h1 ,int time) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(time*1000*1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        if (bitmap == null) {
            bitmap = ThumbnailUtils.extractThumbnail(ThumbnailUtils.createVideoThumbnail(filePath,
                            android.provider.MediaStore.Video.Thumbnails.MINI_KIND),
                    w1, h1, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }

        return bitmap;
    }

    public static boolean copyFile(final File fromFile, final File targetFile) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(fromFile);
            out = new FileOutputStream(targetFile);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        } catch (Exception e) {
            Log.w(TAG + ":copy", "error occur while copy", e);
            return false;
        } finally {
            closeAll(in,out);
        }
        return true;
    }

    //执行adb shell操作
    public static void execShellCmd(String str_cmd) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(str_cmd);
            process.waitFor();
            int i = process.exitValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
