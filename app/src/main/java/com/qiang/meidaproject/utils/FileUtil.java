package com.qiang.meidaproject.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

/**
 * Created by Qiang on 2016/3/24.
 *
 * 文件工具类
 */
public class FileUtil {

    private static final String TAG = "FileUtil";


    /**
     *
     * 读取assets文件
     *
     * @param context
     *            context对象
     * @param name
     *            读取文件名
     * @return String 读取信息
     */
    public static String readAssetsFile( Context context, String name ) {

        if( context == null || TextUtils.isEmpty(name) ) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        InputStream in;
        try {
            in = context.getResources().getAssets().open( name );
            BufferedReader br = new BufferedReader( new InputStreamReader( in ) );
            String tempStr;
            while( ( tempStr = br.readLine() ) != null ) {// 一行一行的读取
                sb.append( tempStr ).append( "\n" );
            }
            return sb.toString();
        } catch( IOException e ) {
            LogUtil.e(TAG, e.getMessage(), e);
        }
        return null;
    }


    /**
     *
     * 将bitmap图片保存到本地
     *
     * @param ctx
     *            context对象
     * @param bitmap
     *            图片对象
     * @return String 操作信息状态
     * @exception
     * @since 1.0.0

    public static String saveToAlbum( Context ctx, Bitmap bitmap ) {

        ResourceManager mResourceManager = ResourceManager.getInstance( ctx );
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMdd_hhmmss" );
        ContentResolver cr = ctx.getContentResolver();
        String fileName = "IMG_" + sdf.format( new Date() ) + ".png";
        if( android.os.Environment.getExternalStorageDirectory() == null ) {
            return ctx.getResources().getString( mResourceManager.getStringId( "fw_fileutil_error_msg1" ) );
        }
        String url = MediaStore.Images.Media.insertImage( cr, bitmap, fileName, "" );

        if( url != null ) {
            MediaScannerConnection.scanFile(ctx,// 部分机器缓存更新不及时问题，该代码待测试，缩略图可能有此问题
                    new String[]{Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                            .getPath() + "/" + fileName}, null, null);
            return ctx.getResources().getString( mResourceManager.getStringId( "fw_fileutil_succ_msg" ) );
        } else {
            return ctx.getResources().getString( mResourceManager.getStringId( "fw_fileutil_error_msg2" ) );
        }
    }
    */

    /**
     *
     * 将指定文件转换为byte数组
     *
     * @param file
     *            要转换的文件
     * @return byte[] 转换后byte数组
     * @throws IOException
     * @permission
     * @since 1.0.0
     */
    @SuppressWarnings( "resource" )
    public static byte[] getBytesFromFile( File file ) throws IOException {

        InputStream is = new FileInputStream( file );// 获取文件大小
        long length = file.length();
        if( length > Integer.MAX_VALUE ) {
            // 文件太大，无法读取
            throw new IOException( "File is to large " + file.getName() );
        }// 创建一个数据来保存文件数据
        byte[] bytes = new byte[ ( int )length ];
        // 读取数据到byte数组中
        int offset = 0;
        int numRead = 0;
        while( offset < bytes.length && ( numRead = is.read( bytes, offset, bytes.length - offset ) ) >= 0 ) {
            offset += numRead;
        }
        // 确保所有数据均被读取
        if( offset < bytes.length ) {
            throw new IOException( "Could not completely read file " + file.getName() );
        }
        is.close();
        return bytes;
    }


    /**
     *
     * 用于判断目标文件是否存在
     *
     * @param path
     *            文件全路径
     * @return ture 存在 false 不存在
     * @permission
     * @exception Exception
     * @since 1.0.0
     */
    public static boolean FileIsExists( String path ) {

        try {
            File f = new File( path );
            if( !f.exists() ) {
                return false;
            }
        } catch( Exception e ) {
            e.printStackTrace();
            return false;
        }
        return true;

    }


    public static void copyFile( BufferedInputStream inbuff, OutputStream out ) {

        BufferedOutputStream outbuff = null;
        try {
            // 新建文件输出流并对它进行缓冲
            outbuff = new BufferedOutputStream( out );

            // 缓冲数组
            byte[] b = new byte[ 1024 ];
            int len = 0;
            while( ( len = inbuff.read( b ) ) != -1 ) {
                outbuff.write( b, 0, len );
            }
            // 刷新此缓冲的输出流
            outbuff.flush();

            // 关闭流
            inbuff.close();
            outbuff.close();
            out.close();
        } catch( FileNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if( inbuff != null ) {
                try {
                    inbuff.close();
                } catch( IOException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if( outbuff != null ) {
                try {
                    outbuff.close();
                } catch( IOException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if( out != null ) {
                try {
                    out.close();
                } catch( IOException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }


    public static void copyFile( BufferedInputStream inbuff, File targetFile ) {

        BufferedOutputStream outbuff = null;
        FileOutputStream out = null;
        try {
            // 新建文件输出流并对它进行缓冲
            out = new FileOutputStream( targetFile );
            outbuff = new BufferedOutputStream( out );

            // 缓冲数组
            byte[] b = new byte[ 1024 ];
            int len = 0;
            while( ( len = inbuff.read( b ) ) != -1 ) {
                outbuff.write( b, 0, len );
            }
            // 刷新此缓冲的输出流
            outbuff.flush();

            // 关闭流
            inbuff.close();
            outbuff.close();
            out.close();
        } catch( FileNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if( inbuff != null ) {
                try {
                    inbuff.close();
                } catch( IOException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if( outbuff != null ) {
                try {
                    outbuff.close();
                } catch( IOException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if( out != null ) {
                try {
                    out.close();
                } catch( IOException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }


    public static void copyFile( File s, File t ) {

        FileInputStream fi = null;
        FileOutputStream fo = null;
        FileChannel in = null;
        FileChannel out = null;
        try {
            fi = new FileInputStream( s );
            fo = new FileOutputStream( t );
            in = fi.getChannel();// 得到对应的文件通道
            out = fo.getChannel();// 得到对应的文件通道

            in.transferTo( 0, in.size(), out );// 连接两个通道，并且从in通道读取，然后写入out通道
        } catch( IOException e ) {
            e.printStackTrace();
        } finally {
            try {
                fi.close();
                in.close();
                fo.close();
                out.close();
            } catch( IOException e ) {
                e.printStackTrace();
            }
        }
    }


    public static void copyFile( String oldPath, OutputStream outputStream ) {

        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File( oldPath );
            if( oldfile.exists() ) { // 文件存在时
                InputStream inStream = new FileInputStream( oldPath ); // 读入原文件
                byte[] buffer = new byte[ 1444 ];
                while( ( byteread = inStream.read( buffer ) ) != -1 ) {
                    bytesum += byteread; // 字节数 文件大小
                    outputStream.write( buffer, 0, byteread );
                }
                inStream.close();
                outputStream.close();
            }
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }


    public static void deleteFile( File file ) {

        if( file != null ) {
            if( file.isDirectory() ) {
                for( File child : file.listFiles() ) {
                    deleteFile( child );
                }
            }
            if( file.exists() ) {
                file.delete();
            }
        }
    }


    public static void deleteFile( String path ) {

        if( path != null ) {
            File file = new File( path );
            if( file.exists() ) {
                deleteFile( file );
            }
        }
    }


    public static boolean isSdcardExist() {

        return Environment.getExternalStorageState().equals( Environment.MEDIA_MOUNTED );
    }


    /**
     *
     * 用URI获取真实路径
     *
     * @param context
     *            context对象
     * @param uri
     *            uri路径
     * @return String 文件真实路径
     * @permission
     * @exception
     * @since 1.0.0
     */
    public static String getRealFilePath( final Context context, final Uri uri ) {

        if( null == uri )
            return null;
        final String scheme = uri.getScheme();
        String data = null;
        if( scheme == null )
            data = uri.getPath();
        else if( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();
        } else if( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null,
                    null );
            if( null != cursor ) {
                if( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                    if( index > -1 ) {
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

}
