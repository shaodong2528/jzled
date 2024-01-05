package com.jz.led.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

import com.jz.led.colorpick.ColorUtils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright (C)
 * FileName: SystemUtils
 * Author: JZ
 * Date: 2022/5/23 14:51
 * Description: SystemUtils
 */
public class SystemUtil {

    public static String getTopName(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = null;
        if (activityManager != null) {
            list = activityManager.getRunningTasks(100);
        }
        if (list == null || list.size() <= 0) {
            return "";
        }
        if (list.get(0) != null && list.get(0).topActivity != null) {
            return list.get(0).topActivity.getPackageName();
        }
        return "";
    }

    public static void setProp(String key, String value) {
        Reflex.invokeStaticMethod("android.os.SystemProperties", "set",
                new Class[]{String.class, String.class}, new String[]{key, value});
    }

    public static String getProp(String key) {
        String value = (String) Reflex.invokeStaticMethod("android.os.SystemProperties", "get",
                new Class[]{String.class, String.class}, new String[]{key, ""});
        return value;
    }

    public static String getProp(String key, String def) {
        String value = (String) Reflex.invokeStaticMethod("android.os.SystemProperties", "get",
                new Class[]{String.class, String.class}, new String[]{key, def});
        return value;
    }

    public static String getLename() {
        return SystemUtil.getProp("persist.bluetooth.lename", "");
    }

    public static String getDeviceName() {
        return getProp("persist.pandora.device_name", "Zeus");
    }

    public static String getSerialNo() {
        String serialNo = getProp("ro.serialno");
        if (!TextUtils.isEmpty(serialNo) && serialNo.length() >= 4) {
            return serialNo.substring(serialNo.length() - 4);
        }
        return getRandomString(4);
    }

    private static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    //获取可用的RAM
    public static String getFreeRam() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/meminfo"));
            Pattern p = Pattern.compile("MemAvailable" + "\\s*:\\s*(.*)");
            String line;
            while ((line = br.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    String sRam = m.group(1).replace("kB", "").trim();
                    int anInt = Integer.parseInt(sRam);
                    float v = anInt / 1024 / 1_024.0f;
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    String strPrice = decimalFormat.format(v);
                    return strPrice;
                }
            }
        } catch (Exception e) {
        } finally {
            closeAll(br);
        }
        return null;
    }

    //获取可用ROM
    public static String getFreeRom() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        double v = availableBlocks * blockSize / 1024.0 / 1024.0 / 1024.0;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        String strPrice = decimalFormat.format(v);
        return strPrice;
    }

    //获取总RAM
    public static String getTotalRam() {
        String path = "/proc/meminfo";
        String firstLine = null;
        int totalRam = 0;
        try {
            FileReader fileReader = new FileReader(path);
            BufferedReader br = new BufferedReader(fileReader, 8192);
            firstLine = br.readLine().split("\\s+")[1];
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (firstLine != null) {
            totalRam = (int) Math.ceil((new Float(Float.valueOf(firstLine) / (1024 * 1024)).doubleValue()));
        }
        return totalRam + "";
    }

    //获取总ROM
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static String getTotalRom(Context mContext) {
        String strTotal = "";
        StorageManager mStorageManager = mContext.getSystemService(StorageManager.class);
        //VolumeInfo = Object
        List<Object> volumes = (List<Object>) Reflex.invokeInstanceMethod(mStorageManager, "getVolumes");
        final List<Object> privateVolumes = new ArrayList<>(volumes.size());
        // Find mounted volumes
        for (final Object vol : volumes) {
            int getType = (int) Reflex.invokeInstanceMethod(vol, "getType");
            if (getType == 1) {
                privateVolumes.add(vol);
            }
        }
        //VolumeInfo = Object
        for (final Object volumeInfo : privateVolumes) {
            boolean isMountedReadable = (boolean) Reflex.invokeInstanceMethod(volumeInfo, "isMountedReadable");
            if (isMountedReadable) {
                strTotal = getSizeString(volumeInfo, mContext);
            }
        }

        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        double mTotal = totalBlocks * blockSize / 1024.0 / 1024.0 / 1024.0 / 8.0;
        if (TextUtils.isEmpty(strTotal)) {
            strTotal = (int) Math.ceil(mTotal) * 8 + "";
        }
        return strTotal;
    }

    private static String getSizeString(Object vol, Context mContext) {
        final File path = (File) Reflex.invokeInstanceMethod(vol, "getPath");
        boolean isMountedReadable = (boolean) Reflex.invokeInstanceMethod(vol, "isMountedReadable");
        if (isMountedReadable && path != null) {
            String string = gettwo(getTotalSize(vol, path.getTotalSpace()), mContext);
            return string;
        } else {
            return "";
        }
    }

    public static long getTotalSize(Object info, long sTotalInternalStorage) {
        int getType = (int) Reflex.invokeInstanceMethod(info, "getType");
        String getFsUuid = (String) Reflex.invokeInstanceMethod(info, "getFsUuid");
        File path = (File) Reflex.invokeInstanceMethod(info, "getPath");
        if (getType == 1 && Objects.equals(getFsUuid, null) && sTotalInternalStorage > 0) {
            if (path.getPath().equals("/data")) {
                String f = getProp("persist.sys.ddr");//设置ddr的倍数
                if (TextUtils.isEmpty(f)) {
                    f = "1";
                }
                double g = Double.valueOf(f);
                long l = (long) (sTotalInternalStorage * g);
                return l;
            }
            return sTotalInternalStorage;
        } else {
            if (path == null) {
                return 0;
            }
            if (path.getPath().equals("/data")) {
                String f = getProp("persist.sys.ddr");//设置ddr的倍数
                if (TextUtils.isEmpty(f)) {
                    f = "1";
                }
                double g = Double.valueOf(f);
                return (long) (path.getTotalSpace() * g);
            }
            return path.getTotalSpace();
        }
    }

    public static String gettwo(long storage, Context context) {
        long definitionStorage = 0l;
        long storage1 = storage;
        long sto = 0l;//单位GB
        Log.i("wzf2017", "storage==" + storage);
        storage = storage / 1024 / 1024;
        String tol;
        if (0 < storage && storage < 4096) {
            definitionStorage = 4096l * 1024 * 1024;
            tol = "4GB";
            sto = 4l;
        } else if (storage < 8192) {
            definitionStorage = 8192l * 1024 * 1024;
            tol = "8GB";
            sto = 8l;
        } else if (storage < 16384) {
            definitionStorage = 16384l * 1024 * 1024;
            tol = "16GB";
            sto = 16l;
        } else if (storage < 32768) {
            definitionStorage = 32768l * 1024 * 1024;
            tol = "32GB";
            sto = 32l;
        } else if (storage < 65536) {
            definitionStorage = 65536l * 1024 * 1024;
            tol = "64GB";
            sto = 64l;
        } else if (storage < 131072) {
            definitionStorage = 131072l * 1024 * 1024;
            tol = "128GB";
            sto = 128l;
        } else if (storage < 262144) {
            definitionStorage = 262144 * 1024 * 1024;
            tol = "256GB";
            sto = 256l;
        } else if (storage < 524288) {
            definitionStorage = 524288 * 1024 * 1024;
            tol = "512GB";
            sto = 512l;
        } else {
            definitionStorage = storage1;
            tol = formatSize(context/*getContext()*/, storage1);
            sto = storage1 / 1000 / 1000 / 1000l;
        }
        Log.i("wzf2017", "definitionStorage==" + definitionStorage);
        tol = formatSize(context/*getContext()*/, sto * 1000 * 1000 * 1000);
        return tol.replace("GB", "");
    }

    public static String formatSize(Context context, long size) {
        return (size == -1)
                ? "Mengira..." : Formatter.formatShortFileSize(context, size);
    }

    //关闭流
    public static void closeAll(Closeable... ables) {
        for (Closeable c : ables) {
            if (c != null) {
                try {
                    c.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private static void sync() {
        try {
            Runtime.getRuntime().exec("sync");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface ExecShellListener {
        void onComplete(boolean isok);
    }

    public static void execShellCmd3(Context context, String str_cmd1, ExecShellListener callback) {
        boolean isok = false;
        String[] str_cmd2 = {"/bin/sh", "-c", str_cmd1};
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        try {
            process = Runtime.getRuntime().exec(str_cmd2);
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            String s1;
            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }
            while ((s1 = errorResult.readLine()) != null) {
                errorMsg.append(s1);
            }
            if (TextUtils.isEmpty(successMsg.toString())) {
                isok = false;
            } else {
                isok = true;
            }
        } catch (Exception e) {
            isok = false;
        } finally {
            closeAll(successResult, errorResult);
            if (process != null) {
                process.destroy();
            }
        }
        sync();
        if (callback != null)
            callback.onComplete(isok);
    }

    public static void RunApp(Context context, String packageName) {
        Intent intent = null;
        try {
            if (packageName != null) {
                PackageManager pm = context.getPackageManager();
                intent = pm.getLaunchIntentForPackage(packageName);
                if (intent != null) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } else {
                    //Toast.makeText(context,"app not installed",Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
        }
    }

    public static void RunApp(Context context, String packageName, String classname) {
        if (classname != null) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName cn = new ComponentName(packageName, classname);
            intent.setComponent(cn);
            if (context.getPackageManager().resolveActivity(intent, 0) != null) {
                context.startActivity(intent);
            }
        } else {
            PackageInfo pi;
            try {
                pi = context.getPackageManager().getPackageInfo(packageName, 0);
                Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
                resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                resolveIntent.setPackage(pi.packageName);
                PackageManager pManager = context.getPackageManager();
                List<ResolveInfo> apps = pManager.queryIntentActivities(resolveIntent, 0);
                if (apps.iterator().hasNext()) {
                    ResolveInfo ri = apps.iterator().next();
                    if (ri != null) {
                        packageName = ri.activityInfo.packageName;
                        String className = ri.activityInfo.name;

                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        ComponentName cn = new ComponentName(packageName, className);
                        intent.setComponent(cn);
                        if (context.getPackageManager().resolveActivity(intent, 0) != null) {
                            context.startActivity(intent);
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    public static long lastClickTime1 = 0;

    public static boolean isFastDoubleClick1() {
        boolean isDouble;
        long time = System.currentTimeMillis();
        if (time - lastClickTime1 < 120) {
            isDouble = true;
        } else {
            lastClickTime1 = time;
            isDouble = false;
        }
        return isDouble;
    }

    //获取版本号
    public static String getSystemVersion() {
        String version = getProp("ro.pandora.version.incremental");
        if (TextUtils.isEmpty(version)) {
            version = "null";
        }
        return version;
    }

    public static String getPackageVersionCode(Context context,String packageName) {
        String code = "";
        List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
        for(int i = 0;i < packages.size();i++) {
            PackageInfo packageInfo = packages.get(i);
            if(packageInfo.packageName.equals(packageName)){
                code = packageInfo.versionName;
                break;
            }
        }
        return code;
    }

    public static boolean isShowNaivWidget(){
        String prop = SystemUtils.getProp("persist.isShowNaivWidget", "0");
        return prop.equals("1");
    }

    /**
     *
     * @param hexColor   0-1
     * @param brightValue 00ff00
     * @return
     */
    public static String colorBrightConvert(String hexColor,float brightValue){
        float[] hsv = new float[3];
        if(!TextUtils.isEmpty(hexColor)){
            if(hexColor.startsWith("#")){
                Color.colorToHSV(Color.parseColor(hexColor), hsv);
                hsv[2] = brightValue;
                return colorToHexColor(Color.HSVToColor(hsv));
            }else{
                Color.colorToHSV(Color.parseColor("#"+hexColor), hsv);
                hsv[2] = brightValue;
                return colorToHexColor(Color.HSVToColor(hsv));
            }
        }else{
            //默认颜色
            Color.colorToHSV(Color.parseColor("#0000FF"), hsv);
            hsv[2] = brightValue;
            return colorToHexColor(Color.HSVToColor(hsv));
        }
    }

    public static String colorToHexColor(int color){
        return ColorUtils.getHexCode(color).substring(2);
    }

}
