package com.jz.led.activity;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;


import com.jz.led.message.CommonParams;
import com.jz.led.message.MsgBaseHandler;
import com.jz.led.message.MsgHandlerCenter;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C)
 * FileName: BasicActivity
 * Author: JZ
 * Date: 2022/5/7 15:20
 * Description: BasicActivity
 */
public abstract class BasicActivity extends FragmentActivity {

    private final String TAG = BasicActivity.class.getSimpleName();

    protected int mCurrentFocusViewIndex = -1; //当前拥有焦点的视图索引

    protected boolean mHasWindowFocus;

    protected boolean mIsAutoScroll;

    protected List<View> mFocusViewList = new ArrayList<>(); //需要拥有焦点的视图集合

    protected int mCurrentDynamicViewPosition = -1; //动态添加的View的当前拥有焦点的视图索引

    private MsgBaseHandler mMainActivityHandler = new MsgMainActivityHandler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
        initData();
        initFocusView();
        MsgHandlerCenter.registerMessageHandler(mMainActivityHandler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MsgHandlerCenter.unRegisterMessageHandler(mMainActivityHandler);
    }

    private class MsgMainActivityHandler extends MsgBaseHandler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "handleMessage " + msg.what + " " + msg.arg1);
            if (msg.what == CommonParams.MSG_KEY_EVENT_TYPE && hasWindowFocus()) {
                switch (msg.arg1) {
                    case CommonParams.EventType.DPAD_UP:
                        onKnobUp();
                        break;
                    case CommonParams.EventType.DPAD_DOWN:
                        onKnobDown();
                        break;
                    case CommonParams.EventType.DPAD_LEFT:
                        onKnobLeft();
                        break;
                    case CommonParams.EventType.DPAD_RIGHT:
                        onKnobRight();
                        break;
                    case CommonParams.EventType.DPAD_CENTER:
                        break;
                    case CommonParams.EventType.DPAD_CENTER_LONG_CLICK:
                        onKnobLongClick();
                        break;
                }
            } else if (msg.what == CommonParams.MSG_KEY_EVENT_DOWN) {
            }
        }

        @Override
        public void careAbout() {
            addMsg(CommonParams.MSG_KEY_EVENT_TYPE);
            addMsg(CommonParams.MSG_KEY_EVENT_DOWN);
        }
    }

    /**
     * 获取焦点
     *
     * @param view
     */
    public static void requestFocus(View view) {
        if (view != null && view.getVisibility() == View.VISIBLE && !view.isFocused()) {
            view.requestFocus();
            view.requestFocusFromTouch();
        }
    }

    /**
     * RecyclerView请求视图焦点
     */
    public void requestFocusForRV(RecyclerView rv, int currFocusIndex) {
        int maxPosition = rv.getAdapter().getItemCount() - 1;
        if (currFocusIndex > maxPosition) {
            currFocusIndex = maxPosition;
        }
        if (!isRequestFocus(rv, currFocusIndex)) {
            RecyclerView.ViewHolder viewHolderForAdapterPosition = rv.findViewHolderForAdapterPosition(currFocusIndex);
            if (viewHolderForAdapterPosition == null) {
                return;
            }
            View itemView = viewHolderForAdapterPosition.itemView;
            Log.d(TAG, "requestFocusForRV itemView " + itemView.toString());
            requestFocus(itemView);
        }
    }

    /**
     * RecyclerView是否需要滚动
     */
    public boolean isRequestFocus(RecyclerView rv, int currFocusIndex) {
        if (currFocusIndex < 0 || currFocusIndex > rv.getAdapter().getItemCount() - 1) {
            return false;
        } else {
            int firstItem = rv.getChildLayoutPosition(rv.getChildAt(0));
            int lastItem = rv.getChildLayoutPosition(rv.getChildAt(rv.getChildCount() - 1));
            if (currFocusIndex < firstItem) {
                mIsAutoScroll = true;
                rv.smoothScrollToPosition(currFocusIndex);
                return true;
            } else if (currFocusIndex <= lastItem) {
                return false;
            } else {
                mIsAutoScroll = true;
                rv.smoothScrollToPosition(currFocusIndex);
                return true;
            }
        }
    }

    protected abstract void initUI();

    protected abstract void initData();

    protected abstract void initFocusView();

    protected abstract void onKnobUp();

    protected abstract void onKnobDown();

    protected abstract void onKnobLeft();

    protected abstract void onKnobRight();

    protected void onKnobLongClick(){};
}
