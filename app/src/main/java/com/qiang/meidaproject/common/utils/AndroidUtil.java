package com.qiang.meidaproject.common.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by Qiang on 2016/3/24.
 *
 * android 工具类
 */
public class AndroidUtil {

    private static final String TAG = "AndroidUtil";
    public static final String OS_TYPE = "02";


    /**
     *
     * 取得应用的版本号
     *
     * @param context
     *            context对象
     * @return String 应用版本号
     * @exception PackageManager.NameNotFoundException
     * @since 1.0.0
     */
    public static String getAppVersion( Context context ) {

        if( context == null ) {
            return null;
        } else {
            PackageManager packageManager = context.getPackageManager();
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo( context.getPackageName(), 0 );
                if( packageInfo != null ) {
                    return packageInfo.versionName;
                }
            } catch( PackageManager.NameNotFoundException e ) {
                LogUtil.e(TAG, e.getMessage(), e);
            }
            return null;
        }

    }


    /**
     *
     * 比较新版本是否比当前版本大
     *
     * @param newVersion
     * @param curVersion
     * @return
     * @permission boolean
     * @since 1.0.0
     */
    public static boolean compareVersion( String newVersion, String curVersion ) {

        if( !TextUtils.isEmpty(newVersion) && !TextUtils.isEmpty( curVersion ) ) {
            String[] newVersionArray = newVersion.split( "." );
            String[] curVersionArray = curVersion.split( "." );
            for( int i = 0; i < newVersionArray.length; i++ ) {
                // 若当前版本的长度没有新的长，则表示新版本比当前版本大
                if( i < curVersionArray.length ) {
                    try {
                        int newVersionTemp = Integer.parseInt( newVersionArray[ i ] );
                        int curVersionTemp = Integer.parseInt( curVersionArray[ i ] );

                        // 若两者相等，则继续比对，若不等，则大小已分
                        if( newVersionTemp != curVersionTemp ) {
                            return newVersionTemp > curVersionTemp;
                        }
                    } catch( Exception e ) {
                        LogUtil.e(TAG, e.getMessage(), e);
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     *
     * 获取手机序列号
     *
     * @param context
     *            context对象
     * @return String 手机序列号
     * @since 1.0.0
     */
    public static String getDeviceId( Context context ) {

        String deviceId = "";
        if( context != null ) {
            TelephonyManager tm = ( TelephonyManager )context.getSystemService( Context.TELEPHONY_SERVICE );
            if( !TextUtils.isEmpty( tm.getDeviceId() ) ) {
                deviceId = tm.getDeviceId();
            }
        }
        return deviceId;

    }


    /**
     *
     * 获取手机唯一标示
     *
     * @param context
     *            context对象
     * @return String 手机唯一标示
     * @since 1.0.0
     */
    public static String getUUID( Context context ) {

        if( context == null ) {
            return null;
        }

        String uuid = null;
        String SimSerialNumber = "";
        String androidId = "";
        String deviceId = getDeviceId( context );
        TelephonyManager tm = ( TelephonyManager )context.getSystemService( Context.TELEPHONY_SERVICE );
        androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        if( tm != null ) {// sim序列号
            SimSerialNumber = tm.getSimSerialNumber();
        }

        if( TextUtils.isEmpty( deviceId ) || TextUtils.isEmpty( androidId ) || TextUtils.isEmpty( SimSerialNumber ) ) {
            uuid = deviceId + androidId + SimSerialNumber + Utils.generateString(10);
        } else {
            uuid = deviceId + androidId + SimSerialNumber;
        }
        try {
            uuid = MD5.md5( uuid );
        } catch( Exception e ) {
            LogUtil.e(TAG, e.getMessage(), e);
        }

        if( TextUtils.isEmpty( uuid ) ) {
            uuid = Utils.generateString(10);
        }

        return uuid;

    }


    /**
     *
     * 判断是否已联网
     *
     * @param context
     *            context对象
     * @return boolean true 已联网 false 未联网
     * @since 1.0.0
     */
    public static boolean isNetworkConnected( Context context ) {

        if( context != null ) {
            ConnectivityManager manager = ( ConnectivityManager )context
                    .getSystemService( Context.CONNECTIVITY_SERVICE );
            NetworkInfo info = manager.getActiveNetworkInfo();
            if( info != null ) {
                return info.isAvailable();
            }
        }

        return false;
    }


    /**
     *
     * 获取网络类型
     *
     * @param context
     *            context对象
     * @return String 类型+信息
     * @since 1.0.0
     */
    public static String getNetworkTypeName( Context context ) {

        String netType = "";
        if( context != null ) {
            ConnectivityManager manager = ( ConnectivityManager )context
                    .getSystemService( Context.CONNECTIVITY_SERVICE );
            NetworkInfo info = manager.getActiveNetworkInfo();
            if( info != null && info.getExtraInfo() != null ) {
                netType = info.getTypeName() + "_" + info.getExtraInfo().replace( "\"", "" );
            }
        }
        return netType;
    }


    public static int getNetworkType( Context context ) {

        int netType = ConnectivityManager.TYPE_MOBILE;
        if( context != null ) {
            ConnectivityManager manager = ( ConnectivityManager )context
                    .getSystemService( Context.CONNECTIVITY_SERVICE );
            NetworkInfo info = manager.getActiveNetworkInfo();
            if( info != null ) {
                netType = info.getType();
            }
        }

        return netType;
    }


    /**
     *
     * 获取MAC地址
     *
     * @param context
     *            context对象
     * @return String MAC地址
     * @since 1.0.0
     */
    public static String getMAC( Context context ) {

        if( context == null ) {
            return null;
        }
        WifiManager wifi = ( WifiManager )context.getSystemService( Context.WIFI_SERVICE );
        WifiInfo info = wifi.getConnectionInfo();
        String macAddress = info.getMacAddress();// 4C:AA:16:24:F5:43
        if( TextUtils.isEmpty( macAddress ) )
            macAddress = "";
        return macAddress;
    }


    /**
     *
     * 获取手机厂商信息
     *
     * @return String 手机厂商信息
     * @since 1.0.0
     */
    public static String getDeviceBrand() {

        return android.os.Build.BRAND;

    }


    /**
     *
     * 获取手机设备名
     *
     * @return String 手机设备名
     * @since 1.0.0
     */
    public static String getDeviceName() {

        return android.os.Build.PRODUCT;

    }


    /**
     *
     * 获取设备型号信息
     *
     * @return String 设备型号信息
     * @since 1.0.0
     */
    public static String getDeviceModel() {

        return android.os.Build.MODEL;

    }


    /**
     *
     * 获取系统版本
     *
     * @return String 系统版本
     * @since 1.0.0
     */
    public static String getOsVersion() {

        return android.os.Build.VERSION.RELEASE;

    }


    /**
     *
     * 获取服务集标识（SSID）
     *
     * @param context
     *            context对象
     * @return String 服务集标识（SSID）
     * @since 1.0.0
     */
    public static String getSSID( Context context ) {

        if( context == null ) {
            return null;
        }
        WifiManager wifiManager = ( WifiManager )context.getSystemService( context.WIFI_SERVICE );// 获取ssid等数据
        WifiInfo info = wifiManager.getConnectionInfo();
        if( info != null && info.getSSID() != null ) {
            return info.getSSID().replace( "\"", "" );
        }
        return null;
    }


    /**
     *
     * 获取sim序列号
     *
     * @param context
     *            context对象
     * @return String sim序列号
     * @since 1.0.0
     */
    public static String getSimSerialNumber( Context context ) {

        if( context == null ) {
            return null;
        }
        String SimSerialNumber = "";
        TelephonyManager tm = ( TelephonyManager )context.getSystemService( Context.TELEPHONY_SERVICE );
        if( tm != null ) {// sim序列号
            SimSerialNumber = tm.getSimSerialNumber();
        }
        return SimSerialNumber;
    }


    /**
     *
     * 获取屏幕信息对象
     *
     * @param act
     *            Activity对象
     * @return DisplayMetrics 屏幕信息类
     * @since 1.0.0
     */
    public static DisplayMetrics getDisplayMetrics( Activity act ) {

        if( act == null ) {
            return null;
        } else {
            Display display = act.getWindowManager().getDefaultDisplay();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            display.getMetrics( displayMetrics );

            return displayMetrics;
        }
    }


    /**
     *
     * 获取屏幕信息对象
     *
     * @param context
     *            context对象
     * @return DisplayMetrics 屏幕信息类
     * @since 1.0.0
     */
    public static DisplayMetrics getDisplayMetrics( Context context ) {

        if( context != null ) {
            return context.getResources().getDisplayMetrics();
        }
        return null;
    }


    /**
     *
     * 获取位置区位码
     *
     * @param context
     *            context对象
     * @return int 位置区位码
     * @since 1.0.0
     */
    public static int getLac( Context context ) {

        int lac = 0;
        if( context != null ) {
            TelephonyManager tm = ( TelephonyManager )context.getSystemService( Context.TELEPHONY_SERVICE );
            if( tm != null ) {
//				GsmCellLocation location = ( GsmCellLocation )tm.getCellLocation();
                CellLocation celllocation = tm.getCellLocation();
                if (celllocation instanceof GsmCellLocation) {
                    GsmCellLocation location = ( GsmCellLocation )celllocation;
                    if( location != null ) {
                        lac = location.getLac();
                    }
                }else if(celllocation instanceof CdmaCellLocation){
                    CdmaCellLocation location = ( CdmaCellLocation )celllocation;
                    if( location != null ) {
                        lac = location.getNetworkId();
                    }
                }
            }
        }
        return lac;
    }


    /**
     *
     * 获取基站编号
     *
     * @param context
     *            context对象
     * @return int 基站编号
     * @since 1.0.0
     */
    public static int getCid( Context context ) {

        int cid = 0;
        if( context != null ) {
            TelephonyManager tm = ( TelephonyManager )context.getSystemService( Context.TELEPHONY_SERVICE );
            if( tm != null ) {
//				GsmCellLocation location = ( GsmCellLocation )tm.getCellLocation();
                CellLocation celllocation = tm.getCellLocation();
                if (celllocation instanceof GsmCellLocation) {
                    GsmCellLocation location = ( GsmCellLocation )celllocation;
                    if( location != null ) {
                        cid = location.getCid();
                    }
                }else if(celllocation instanceof CdmaCellLocation ){
                    CdmaCellLocation location = ( CdmaCellLocation )celllocation;
                    if( location != null ) {
                        cid = location.getBaseStationId();
                    }
                }
            }
        }
        return cid;
    }


    /**
     *
     * sendMsg 发送短信
     *
     * @param act
     *            Activity对象
     * @param msg
     *            短信内容
     * @param number
     *            发送电话号
     * @since 1.0.0
     */
    public static void sendMsg( Activity act, String msg, String number ) {

        if( act == null || TextUtils.isEmpty( number ) ) {
            return;
        }

        Uri uri = Uri.parse( "smsto:" + number );
        Intent intent = new Intent( Intent.ACTION_SENDTO, uri );
        intent.putExtra( "sms_body", msg );
        try {
            act.startActivity( intent );
        } catch( ActivityNotFoundException e ) {
            LogUtil.e(TAG, e.getMessage(), e.getCause());
        }
    }


    /**
     *
     * 检测是否具有SD卡
     *
     * @return boolean true 是 false 否
     * @since 1.0.0
     */
    public static boolean checkSDCardAvailable() {

        return android.os.Environment.getExternalStorageState().equals( android.os.Environment.MEDIA_MOUNTED );
    }


    /**
     *
     * 获取存储根目录路径
     *
     * @return String 如果有sd卡，则返回sd卡的目录 如果没有sd卡，则返回存储目录
     * @since 1.0.0
     */
    public static String getSDPath() {

        if( checkSDCardAvailable() ) {
            return Environment.getExternalStorageDirectory().getPath();
        } else {
            return Environment.getDownloadCacheDirectory().getPath();
        }
    }


    /**
     *
     * 获取application的meta-data
     *
     * @param context
     *            应用context
     * @param key
     *            待获取的meta-data的key
     * @return String meta-data的value值，若无该key值，则返回null
     * @since 1.0.0
     */
    public static String getApplicationMetaData( Context context, String key ) {

        ApplicationInfo appInfo;
        try {
            appInfo = context.getPackageManager().getApplicationInfo( context.getPackageName(),
                    PackageManager.GET_META_DATA );
            return appInfo.metaData.getString( key );
        } catch( PackageManager.NameNotFoundException e ) {
            LogUtil.e(TAG, e.getMessage(), e);
        }

        return null;
    }


    public static String getIp( Context context ) {

        // switch( getNetworkType( context ) ) {
        // case ConnectivityManager.TYPE_WIFI: {
        // WifiManager wifiManager = ( WifiManager )context.getSystemService(
        // Context.WIFI_SERVICE );
        // WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        // int ipAddress = wifiInfo.getIpAddress();
        // return intToIp( ipAddress );
        // }
        // case ConnectivityManager.TYPE_MOBILE: {
        // try {
        // for( Enumeration<NetworkInterface> en =
        // NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
        // NetworkInterface intf = en.nextElement();
        // for( Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
        // enumIpAddr.hasMoreElements(); ) {
        // InetAddress inetAddress = enumIpAddr.nextElement();
        // if( !inetAddress.isLoopbackAddress() && inetAddress instanceof
        // Inet4Address ) {
        // return inetAddress.getHostAddress();
        // }
        // }
        // }
        // } catch( SocketException e ) {
        // LogUtil.e( TAG, e.getMessage(), e );
        // }
        // break;
        // }
        // default:
        // break;
        // }
        try {
            for( Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for( Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if( !inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch( SocketException e ) {
            LogUtil.e(TAG, e.getMessage(), e);
        }
        return "127.0.0.1";
    }


    // private static String intToIp( int i ) {
    //
    // return ( i & 0xFF ) + "." + ( ( i >> 8 ) & 0xFF ) + "." + ( ( i >> 16 ) &
    // 0xFF ) + "." + ( ( i >> 24 ) & 0xFF );
    // }

    public static String getOperateName( Context context ) {

        TelephonyManager tm = ( TelephonyManager )context.getSystemService( Context.TELEPHONY_SERVICE );
        String operator = tm.getSimOperator();
        if( operator != null ) {
            if( "46000".equals( operator ) || "46002".equals( operator ) ) {
                return "中国移动";
            } else if( "46001".equals( operator ) ) {
                return "中国联通";
            } else if( "46003".equals( operator ) ) {
                return "中国电信";
            } else {
                return tm.getSimOperatorName();
            }
        }
        return null;
    }


    /**
     *
     * 获取设备屏幕宽度
     *
     * @param act
     * @return int
     * @since 1.0.0
     */
    public static final int getMobileWidth( Activity act ) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        act.getWindowManager().getDefaultDisplay().getMetrics( displayMetrics );
        return displayMetrics.widthPixels;
    }


    /**
     *
     * 获取设备屏幕宽度
     *
     * @param act
     * @return int
     * @since 1.0.0
     */
    public static final int getMobileHeight( Activity act ) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        act.getWindowManager().getDefaultDisplay().getMetrics( displayMetrics );
        return displayMetrics.heightPixels;
    }


    /**
     * isWifi(判断用户是否 在wifi 网络环境)
     *
     * @param context
     * @permission boolean
     * @since 1.1.0
     */
    public static boolean isWifi( Context context ) {

        if( context == null ) {
            return false;
        }

        ConnectivityManager cm = ( ConnectivityManager )context.getSystemService( Context.CONNECTIVITY_SERVICE );
        NetworkInfo info = cm.getNetworkInfo( ConnectivityManager.TYPE_WIFI );
        if( info != null ) {
            return info.isAvailable();
        }
        return false;
    }

}
