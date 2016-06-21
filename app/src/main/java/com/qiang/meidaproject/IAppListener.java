package com.qiang.meidaproject;

/**
 * Created by Qiang on 2016/3/24.
 *
 */
public interface IAppListener {

    /**
     *
     * app退出时，清除数据及状态使用
     *
     * @since 1.0.0
     */
    void destroy();
}
