package com.qiang.meidaproject.config;

import android.content.Context;

/**
 * Created by Qiang on 2016/3/24.
 *
 *
 */
public class GlobalConfig extends Config {

    private static final String CONFIG_FILE_NAME = "global_config";

    private static volatile GlobalConfig mInstance;


    private GlobalConfig( Context context, String configFileName ) {

        super( context, configFileName );
    }


    private GlobalConfig() {

        super();
    }


    /**
     * 获取单例对象
     *
     * @param context
     * @return GlobalConfig
     * @since 1.0.0
     */
    public static GlobalConfig getInstance( Context context ) {

        if( mInstance == null ) {
            synchronized( GlobalConfig.class ) {
                if( mInstance == null ) {
                    mInstance = new GlobalConfig( context, CONFIG_FILE_NAME );
                }
            }
        }

        return mInstance;
    }


    /*
     * 销毁单例，建议在应用退出的时候调用
     */
    @Override
    public void destroy() {

        super.destroy();
        mInstance = null;
    }


}
