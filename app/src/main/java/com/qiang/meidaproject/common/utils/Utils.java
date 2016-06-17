package com.qiang.meidaproject.common.utils;

import android.app.Activity;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;

/**
 * Created by Qiang on 2016/3/24.
 */
public class Utils {

    private static final String TAG = "Utils";
    public static final String allChar = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String letterChar = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";


    /**
     * dip转换为px
     *
     * @return px单位的数值
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * px转换为dip
     *
     * @return dip单位的数值
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    /**
     *
     * 返回一个定长的随机字符串(只包含大小写字母、数字)
     *
     * @param length
     *            随机字符串长度
     * @return String 随机字符串
     * @since 1.0.0
     */
    public static String generateString( int length ) {

        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for( int i = 0; i < length; i++ ) {
            sb.append( allChar.charAt( random.nextInt( allChar.length() ) ) );
        }
        return sb.toString();
    }


    /**
     *
     * 返回一个定长的随机纯字母字符串(只包含大小写字母)
     *
     * @param length
     *            随机字符串长度
     * @return String 随机字符串
     * @since 1.0.0
     */
    public static String generateMixString( int length ) {

        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for( int i = 0; i < length; i++ ) {
            sb.append( allChar.charAt( random.nextInt( letterChar.length() ) ) );
        }
        return sb.toString();
    }


    /**
     *
     * 返回一个定长的随机纯大写字母字符串(只包含大写字母)
     *
     * @param length
     *            随机字符串长度
     * @return String 随机字符串
     * @since 1.0.0
     */
    public static String generateLowerString( int length ) {

        return generateMixString( length ).toLowerCase();
    }


    /**
     *
     * 通用的打开activity方法，catch了ActivityNotFoundException
     *
     * @param ctx
     *            调用者的context句柄，必须是activity或者service句柄，否则不会被调用
     * @param intent
     *            指定的intent
     * @since 1.0.0
     */
    public static void startActivity( Context ctx, Intent intent ) {

        if( ctx == null || intent == null || !( ctx instanceof Activity || ctx instanceof Service) ) {
            return;
        } else {
            try {
                ctx.startActivity( intent );
            } catch( ActivityNotFoundException e ) {
                LogUtil.e(TAG, e.getMessage(), e);
            }
        }
    }


    public static void startActivityForResult( Activity ctx, Intent intent, int requestCode ) {

        if( ctx == null || intent == null ) {
            return;
        } else {
            try {
                ctx.startActivityForResult( intent, requestCode );
            } catch( ActivityNotFoundException e ) {
                LogUtil.e(TAG, e.getMessage(), e);
            }
        }
    }


    /**
     *
     * 获取接近参数的2的整数次幂
     *
     * @param inSampleSize
     *            目标参数
     * @return int 整数次幂
     * @exception
     * @permission
     * @since 1.0.0
     */
    public static int computeSampleSize( int inSampleSize ) {

        // 每次除2的商
        int result = inSampleSize;
        // 每次除以2的余数
        int remainder = 0;
        // 计数器
        int i = 0;
        // 参数是否就是2的整数次幂
        boolean flag = true;
        // 循环除以2 直到商为1时
        while( result != 1 ) {
            remainder = inSampleSize % 2;
            if( remainder != 0 ) {
                flag = false;
            }
            result = result / 2;
            i++;
        }
        if( flag ) {// 所有余数都为0 则参数即是2的整数次幂
            return inSampleSize;
        }
        // 取最接近的2的整数次幂
        int outSampleSize = ( int )( Math.abs( Math.pow( 2, i ) - inSampleSize ) > Math.abs( Math.pow( 2, i + 1 )
                - inSampleSize ) ? Math.pow( 2, i + 1 ) : Math.pow( 2, i ) );
        return outSampleSize;
    }


    /**
     *
     * 判断目标应用程序是否安装
     *
     * @param context
     *            context对象
     * @param packagename
     *            目标应用包名
     * @return true 已经安装 false 没有安装
     * @permission
     * @exception PackageManager.NameNotFoundException
     * @since 1.0.0
     */
    public static boolean isAppInstalled( Context context, String packagename ) {

        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo( packagename, 0 );
        } catch( PackageManager.NameNotFoundException e ) {
            packageInfo = null;
            LogUtil.e(TAG, e.getMessage(), e);
        }
        if( packageInfo == null ) {
            return false;
        } else {
            return true;
        }
    }


    public static Class reflectClass( String className ) {

        if( !TextUtils.isEmpty(className) ) {
            try {
                return Class.forName( className );
            } catch( ClassNotFoundException e ) {
                LogUtil.e(TAG, e.getMessage(), e);
            }
        }

        return null;
    }


    public static String encodeStrToUtf8( String str ) {

        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch( UnsupportedEncodingException e ) {
            LogUtil.e(TAG, e.getMessage(), e);
        }

        return str;
    }














}
