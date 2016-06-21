package com.qiang.meidaproject.config;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Qiang on 2016/3/24.
 *
 *
 */
public abstract class Config {

    private SharedPreferences mPref = null;


    /**
     * 创建一个新的实例 Config.
     *
     * @param context
     *            上下文
     * @param configFileName
     *            配置文件的名字
     */
    protected Config( Context context, String configFileName ) {

        mPref = context.getApplicationContext().getSharedPreferences( configFileName, 0 );
    }


    /**
     * 创建一个新的实例 Config.
     *
     */
    protected Config() {

        // TODO Auto-generated constructor stub
    }


    /**
     * 销毁pref并释放资源 void
     *
     * @since 1.0.0
     */
    public void destroy() {

        mPref = null;
    }


    /**
     * 从pref中获取值
     *
     * @param key
     * @param defaultValue
     *            默认值
     * @return String
     * @since 1.0.0
     */
    public String getString( String key, String defaultValue ) {

        return mPref.getString( key, defaultValue );
    }


    /**
     * 保存值，需要调用apply才能真正的提交
     *
     * @param key
     * @param value
     *            void
     * @since 1.0.0
     */
    public void putString( String key, String value ) {

        mPref.edit().putString( key, value ).apply();
    }


    /**
     * 保存值并提交
     *
     * @param key
     * @param value
     *            void
     * @since 1.0.0
     */
    public void putStringAndCommit( String key, String value ) {

        mPref.edit().putString( key, value ).apply();
    }


    /**
     * 从pref中获取值
     *
     * @param key
     * @param defaultValue
     *            默认值
     * @return String
     * @since 1.0.0
     */
    public int getInt( String key, int defaultValue ) {

        return mPref.getInt( key, defaultValue );
    }


    /**
     * 保存值，需要调用commit才能真正的提交
     *
     * @param key
     * @param value
     *            void
     * @since 1.0.0
     */
    public void putInt( String key, int value ) {

        mPref.edit().putInt( key, value ).apply();
    }


    /**
     * 保存值并提交
     *
     * @param key
     * @param value
     *            void
     * @since 1.0.0
     */
    public void putIntAndCommit( String key, int value ) {

        mPref.edit().putInt( key, value ).apply();
    }


    /**
     * 从pref中获取值
     *
     * @param key
     * @param defaultValue
     *            默认值
     * @return String
     * @since 1.0.0
     */
    public Boolean getBoolean( String key, boolean defaultValue ) {

        return mPref.getBoolean( key, defaultValue );
    }


    /**
     * 保存值，需要调用commit才能真正的提交
     *
     * @param key
     * @param value
     *            void
     * @since 1.0.0
     */
    public void putBoolean( String key, boolean value ) {

        mPref.edit().putBoolean( key, value ).apply();
    }


    /**
     * 保存值并提交
     *
     * @param key
     * @param value
     *            void
     * @since 1.0.0
     */
    public void putBooleanAndCommit( String key, boolean value ) {

        mPref.edit().putBoolean( key, value ).apply();
    }


    public void putFloat( String key, float value ) {

        mPref.edit().putFloat( key, value ).apply();
    }


    public float getFloat( String key, float defaultValue ) {

        return mPref.getFloat( key, defaultValue );
    }


    /**
     * 提交
     *
     * @since 1.0.0
     */
    public void apply() {

        mPref.edit().apply();
    }

}
