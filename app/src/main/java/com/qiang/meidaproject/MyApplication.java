package com.qiang.meidaproject;

import android.app.ActivityManager;
import android.app.Application;
import android.text.TextUtils;

import com.qiang.meidaproject.common.AppManager;

import java.util.Iterator;

/**
 * Created by Qiang on 2016/3/24.
 *
 *
 */
public class MyApplication extends Application {

    private static MyApplication mApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;

        AppManager.getInstance(this).initApp(getPackageName());

        // 防止重启两次,非相同名字的则返回
        if (!isSameAppName()) {
            return;
        }
    }


    /**
     * app退出时监听回调，可清理缓存等

    private IAppListener appListener=new IAppListener() {
        @Override
        public void destroy() {
            // TODO: 2016/3/24
        }
    };*/


    /**
     * 获取application对象
     *
     * @return JApplication
     */
    public static MyApplication get() {
        return mApplication;
    }

    /**
     * 判断是否为相同app名
     *
     * @return boolean
     */
    private boolean isSameAppName() {
        int pid = android.os.Process.myPid();
        String processAppName = getProcessAppName(pid);
        if (TextUtils.isEmpty(processAppName) || !processAppName.equalsIgnoreCase(getPackageName())) {
            return false;
        }
        return true;
    }

    /**
     * 获取processAppName
     *
     * @param pid pid
     * @return String
     */
    private String getProcessAppName(int pid) {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        Iterator<ActivityManager.RunningAppProcessInfo> iterator = activityManager.getRunningAppProcesses().iterator();
        while (iterator.hasNext()) {
            ActivityManager.RunningAppProcessInfo runningAppProcessInfo = (ActivityManager.RunningAppProcessInfo) (iterator
                    .next());
            try {
                if (runningAppProcessInfo.pid == pid) {
                    return runningAppProcessInfo.processName;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

}
