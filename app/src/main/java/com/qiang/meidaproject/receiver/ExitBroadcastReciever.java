package com.qiang.meidaproject.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.qiang.meidaproject.AppManager;
import com.qiang.meidaproject.utils.LogUtil;

/**
 * Created by Qiang on 2016/3/24.
 *
 * 退出APP广播接受器
 */
public class ExitBroadcastReciever extends BroadcastReceiver {

    private static final String tag = "ExitBroadcastReciever";

    @Override
    public void onReceive( Context context, Intent intent ) {

        LogUtil.d(tag, "onReceive exit app!");

        AppManager.getInstance(context).exitApp();
    }

}
