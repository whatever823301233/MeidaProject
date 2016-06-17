package com.qiang.meidaproject.common;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.qiang.meidaproject.common.utils.FileUtil;

import java.io.File;

/**
 * Created by Qiang on 2016/3/24.
 *
 *
 */
public class Constants {


    private static final String TAG = Constants.class.getSimpleName();

    private static volatile Constants sInstance;
    private Context mContext;
    private String mRootPath;

    private static final String PATH_PREFIX = "/qiang/";

    public static String sHostUrl;


    private Constants( Context context ) {

        mContext = context.getApplicationContext();
    }


    public static Constants getInstance( Context context ) {

        if( context == null ) {
            return null;
        }
        if( sInstance == null ) {
            synchronized( Constants.class ) {
                if( sInstance == null ) {
                    sInstance = new Constants( context );
                }
            }
        }
        return sInstance;
    }


    public String getRootPath() {

        if( TextUtils.isEmpty(mRootPath) ) {
            if( FileUtil.isSdcardExist() ) {
                mRootPath = Environment.getExternalStorageDirectory().getPath() + PATH_PREFIX + mContext.getPackageName();
                File file = new File( mRootPath );
                if( !file.exists() ) {
                    file.mkdirs();
                }
            } else {
                mRootPath = mContext.getFilesDir().getAbsolutePath();
            }
        }

        return mRootPath;

    }


    public static String getAppCachePath( Context context ) {

        if( context != null ) {
            return context.getFilesDir().getAbsolutePath() + "/cache";
        }

        return null;
    }

}
