/**
 *
 *
 */
package com.jz.led.utils;

import android.annotation.SuppressLint;
import android.os.Looper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolUtils {

    public static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }
    public static final int FixedThread = 0;
    public static final int CachedThread = 1;
    public static final int SingleThread = 2;
    public volatile static ThreadPoolUtils mThreadPoolUtils;
    private Map<Integer, ExecutorService> mThreadCacheMap;

    public static ThreadPoolUtils newInstance() {
        if (mThreadPoolUtils == null) {
            synchronized (ThreadPoolUtils.class) {
                if (mThreadPoolUtils == null) {
                    mThreadPoolUtils = new ThreadPoolUtils();
                }
            }
        }
        return mThreadPoolUtils;
    }

    @SuppressLint("UseSparseArrays")
    private ThreadPoolUtils() {
        mThreadCacheMap = new HashMap<Integer, ExecutorService>();
    }

    public Map<Integer, ExecutorService> getThreadCacheMap() {
        return mThreadCacheMap;
    }

    public static ExecutorService getFixedThreadPool(int corePoolSize) {
        ExecutorService exec = newInstance().getThreadCacheMap().get(FixedThread);
        if (exec == null) {
            exec = Executors.newFixedThreadPool(corePoolSize);
            newInstance().getThreadCacheMap().put(FixedThread, exec);
        }
        return exec;

    }

    public static ExecutorService getSingleThreadExecutor() {
        ExecutorService exec = newInstance().getThreadCacheMap().get(SingleThread);
        if (exec == null) {
            exec = Executors.newSingleThreadExecutor();
            newInstance().getThreadCacheMap().put(SingleThread, exec);
        }
        return exec;

    }

    public static ExecutorService getCachedThreadPool() {
        ExecutorService exec = newInstance().getThreadCacheMap().get(CachedThread);
        if (exec == null) {
            exec = Executors.newCachedThreadPool();
            newInstance().getThreadCacheMap().put(CachedThread, exec);
        }
        return exec;

    }

    public static void shutDown(int type) {
        ExecutorService exec = newInstance().getThreadCacheMap().get(type);
        if (exec != null && !exec.isShutdown()) {
            exec.shutdown();
            newInstance().getThreadCacheMap().remove(type);
        }
    }

    public static boolean isShutDown(int type) {
        ExecutorService exec = newInstance().getThreadCacheMap().get(type);
        if (exec != null) {
            return exec.isShutdown();
        }
        return false;
    }
}
