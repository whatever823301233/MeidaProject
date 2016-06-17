package com.qiang.meidaproject.common.utils.image;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.qiang.meidaproject.common.utils.AndroidUtil;
import com.qiang.meidaproject.common.utils.LogUtil;
import com.qiang.meidaproject.common.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Qiang on 2016/3/24.
 *
 *
 */
public class ImageUtil {

    private static final String TAG = "ImageUtil";


    /**
     *
     * 取得图片的旋转角度
     *
     * @param path
     *            图片路径
     * @return int 图片旋转角度
     * @exception IOException
     *                printStackTrace
     * @since 1.0.0
     */
    public static int readPictureDegree( String path ) {

        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface( path );
            int orientation = exifInterface.getAttributeInt( ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL );
            switch( orientation ) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch( IOException e ) {
            e.printStackTrace();
        }

        return degree;

    }


    /**
     *
     * 旋转图片
     *
     * @param angle
     *            旋转角度
     * @param bitmap
     *            待旋转图片对象
     * @return Bitmap 旋转后图片
     * @since 1.0.0
     */
    public static Bitmap rotaingImageView( int angle, Bitmap bitmap ) {

        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.setRotate( angle );
        System.out.println( "angle2=" + angle );
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap( bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true );
        return resizedBitmap;
    }


    /**
     *
     * 转换图片成圆形
     *
     * @param bitmap
     *            待转换图片对象
     * @return Bitmap 转换后图形
     * @since 1.0.0
     */
    public static Bitmap toRoundBitmap( Bitmap bitmap ) {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if( width <= height ) {
            roundPx = width / 2;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;
            float clip = ( width - height ) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }

        Bitmap output = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888 );
        Canvas canvas = new Canvas( output );

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect( ( int )left, ( int )top, ( int )right, ( int )bottom );
        final Rect dst = new Rect( ( int )dst_left, ( int )dst_top, ( int )dst_right, ( int )dst_bottom );
        final RectF rectF = new RectF( dst );

        paint.setAntiAlias( true );

        canvas.drawARGB( 0, 0, 0, 0 );
        paint.setColor( color );
        canvas.drawRoundRect( rectF, roundPx, roundPx, paint );

        paint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.SRC_IN ) );
        canvas.drawBitmap( bitmap, src, dst, paint );
        return output;
    }


    /**
     *
     * 将drawable转换为bitmap格式图片
     *
     * @param drawable
     *            drawable格式
     * @return Bitmap bitmap格式
     * @since 1.0.0
     */
    public static Bitmap drawableToBitmap( Drawable drawable ) {

        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap( w, h, config );
        Canvas canvas = new Canvas( bitmap );
        drawable.setBounds( 0, 0, w, h );
        drawable.draw( canvas );
        return bitmap;
    }


    /**
     *
     * 将bitmap图片缩放至指定大小
     *
     * @param bitmap
     *            待缩放图片
     * @param width
     *            宽
     * @param height
     *            高
     * @return Bitmap 缩放后图片
     * @since 1.0.0
     */
    public static Bitmap zoomBitmap( Bitmap bitmap, int width, int height ) {

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ( ( float )width / w );
        float scaleHeight = ( ( float )height / h );
        matrix.postScale( scaleWidth, scaleHeight );
        Bitmap newbmp = Bitmap.createBitmap( bitmap, 0, 0, w, h, matrix, true );
        return newbmp;
    }


    /**
     *
     * 将图片按照指定比例缩放
     *
     * @param bitmap
     *            待缩放图片
     * @param scaleX
     *            x轴缩放比率
     * @param scaleY
     *            y轴缩放比率
     * @return Bitmap 缩放后图片
     * @since 1.0.0
     */
    public static Bitmap zoomBitmap( Bitmap bitmap, float scaleX, float scaleY ) {

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale( scaleX, scaleY );
        return Bitmap.createBitmap( bitmap, 0, 0, w, h, matrix, true );
    }


    /**
     *
     * 将drawable图片缩放至指定大小
     *
     * @param drawable
     *            图片
     * @param width
     * @param height
     * @return Drawable
     * @since 1.0.0
     */
    public static Drawable zoomDrawable( Drawable drawable, int width, int height ) {

        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap oldbmp = drawableToBitmap( drawable );
        Matrix matrix = new Matrix();
        float sx = ( ( float )width / w );
        float sy = ( ( float )height / h );
        matrix.postScale( sx, sy );
        Bitmap newbmp = Bitmap.createBitmap( oldbmp, 0, 0, w, h, matrix, true );
        return new BitmapDrawable( newbmp );
    }


    /**
     *
     * 将指定目录下的指定图片转换成Bitmap格式图片
     *
     * @param path
     *            文件路径
     * @param photoName
     *            文件名
     * @return Bitmap Bitmap格式图片
     * @since 1.0.0
     */
    public static Bitmap getPhotoFromSDCard( String path, String photoName ) {

        Bitmap bitmap = BitmapFactory.decodeFile(path + "/" + photoName + ".png");
        if( bitmap == null ) {
            return null;
        } else {
            return bitmap;
        }
    }


    /**
     *
     * 将bitmap图片保存在SD卡的指定目录中
     *
     * @param bitmap
     *            Bitmap对象
     * @param path
     *            存储目录
     * @param photoName
     *            文件名
     * @return String 存储全路径
     * @since 1.0.0
     */
    public static String savePhotoToSDCard( Bitmap bitmap, String path, String photoName ) {

        String fullFileName = "";
        if( AndroidUtil.checkSDCardAvailable() ) {
            File dir = new File( path );
            if( !dir.exists() ) {
                dir.mkdirs();
            }
            fullFileName = AndroidUtil.getSDPath() + File.separator + photoName + ".png";
            File photoFile = new File( path, File.separator + photoName + ".png" );
            if( !photoFile.exists() ) {
                try {
                    photoFile.createNewFile();
                } catch( IOException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream( photoFile );
                if( bitmap != null ) {
                    if( bitmap.compress( Bitmap.CompressFormat.PNG, 100, fileOutputStream ) ) {
                        fileOutputStream.flush();
                    }
                }
            } catch( FileNotFoundException e ) {
                photoFile.delete();
                e.printStackTrace();
            } catch( IOException e ) {
                photoFile.delete();
                e.printStackTrace();
            } finally {
                try {
                    fileOutputStream.close();
                } catch( IOException e ) {
                    e.printStackTrace();
                }
            }
        }
        return fullFileName;
    }


    /**
     *
     * 如果照片大于sK则返回照片缩小至sK左右
     *
     * @param file
     *            操作文件
     * @param s
     *            要得到图片的文件大小
     * @return int 1代表不需要压缩 其他值则需要压缩
     * @since 1.0.0
     */
    public static int Zoom( File file, int s ) {

        int scale = 1;
        int length = s * 1024;
        Log.e("lenth1", file.length() / 1024 + "");
        if( file.length() > length ) {
            Log.e( "scale0", file.length() / length + "" );
            Log.e( "scale1", ( int )( file.length() / length ) + "" );
            scale = Utils.computeSampleSize((int) (file.length() / length));
            Log.e( "scale2", scale + "" );
        }
        return scale;

    }


    /**
     *
     * 将指定目录的文件转换为Bitmap图片
     *
     * @param filePath
     *            文件路径
     * @return Bitmap Bitmap图片
     * @since 1.0.0
     */
    public static Bitmap returnBitmap( String filePath ) {

        return BitmapFactory.decodeFile( filePath );
    }


    /**
     *
     * 压缩图片
     *
     * @param byteImage
     *            图片格式化后的二进制数组
     * @param reqWidth
     *            压缩后每行像素点个数
     * @param reqHeight
     *            压缩后每列像素点个数
     * @return Bitmap 压缩后图片
     * @since 1.0.0
     */
    public static Bitmap zoomPicture( byte[] byteImage, int reqWidth, int reqHeight ) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray( byteImage, 0, byteImage.length, options );
        options.inSampleSize = calculateInSampleSize( options, reqWidth, reqHeight );
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray( byteImage, 0, byteImage.length, options );
    }


    /**
     *
     * 计算压缩比例
     *
     * @param options
     *            bitmapfatory options类
     * @param reqWidth
     *            计算压缩比例
     * @param reqHeight
     *            压缩后每列像素点个数
     * @return int 比例
     * @since 1.0.0
     */
    public static int calculateInSampleSize( BitmapFactory.Options options, int reqWidth, int reqHeight ) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if( height > reqHeight || width > reqWidth ) {
            final int heightRatio = Math.round( ( float )height / ( float )reqHeight );
            final int widthRatio = Math.round( ( float )width / ( float )reqWidth );
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }


    /**
     *
     * 将指定bitmap图片转换为字符串的形式
     *
     * @param bitmap
     *            目标图片
     * @return String 字符串格式
     * @since 1.0.0
     */
    public static String bitmapToString( Bitmap bitmap ) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress( Bitmap.CompressFormat.JPEG, 100, baos );
        byte[] b = baos.toByteArray();
        return Base64.encodeToString( b, Base64.DEFAULT );
    }


    /**
     *
     * 将bitmap保存在指定文件中
     *
     * @param bitmap
     *            要保存的bitmap图片
     * @param filePath
     *            文件目录
     * @param fileName
     *            文件名
     * @since 1.0.0
     */
    public static void saveBitmapTofile( Bitmap bitmap, String filePath, String fileName ) {

        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        OutputStream os = null;
        File file = new File( filePath + fileName );
        try {
            os = new FileOutputStream( file );
        } catch( FileNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        bitmap.compress( format, 100, os );// 100为默认高质量压缩
        try {
            os.flush();
            os.close();
        } catch( IOException m ) {
            // TODO Auto-generated catch block
            m.printStackTrace();
        }
    }


    public static void saveBitmapTofile( Bitmap bitmap, String filePath ) {

        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        OutputStream os = null;
        File file = new File( filePath );
        try {
            os = new FileOutputStream( file );
        } catch( FileNotFoundException e ) {
            e.printStackTrace();
        }
        bitmap.compress( format, 80, os );// 100为默认高质量压缩,80为默认质量，否则会变大
        try {
            os.flush();
            os.close();
        } catch( IOException m ) {
            // TODO Auto-generated catch block
            m.printStackTrace();
        }
    }


    /**
     *
     * 对当前界面进行截屏
     *
     * @param curAct
     *            当前界面activity
     * @return String 图片保存路径
     * @since 1.0.0
     */
    public static String screenShot( Activity curAct ) {

        // 1.构建Bitmap
        WindowManager windowManager = curAct.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int w = display.getWidth();
        int h = display.getHeight();

        Bitmap Bmp = Bitmap.createBitmap( w, h, Bitmap.Config.ARGB_8888 );

        // 2.获取屏幕
        View decorview = curAct.getWindow().getDecorView();
        decorview.setDrawingCacheEnabled( true );
        Bmp = decorview.getDrawingCache();
        // 3.保存Bitmap
        try {
            String cacheString = "";
            boolean sdCardExist = Environment.getExternalStorageState().equals( android.os.Environment.MEDIA_MOUNTED ); // 判断sd卡是否存在
            if( sdCardExist ) {
                cacheString = curAct.getExternalCacheDir().getAbsolutePath() + "/";
            }
            File path = new File( cacheString );
            // 文件
            String filepath = cacheString + "/Screen_1.png";
            File file = new File( filepath );
            if( !path.exists() ) {
                path.mkdirs();
            }
            if( !file.exists() ) {
                file.createNewFile();
            }

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream( file );
                Bmp.compress( Bitmap.CompressFormat.PNG, 90, fos );
                fos.flush();
                return filepath;
            } catch( Exception e ) {
                LogUtil.e(TAG, e.getMessage(), e);
            } finally {
                if( null != fos ) {
                    fos.close();
                }
            }

        } catch( Exception e ) {
            LogUtil.e(TAG, e.getMessage(), e);
        }

        return null;
    }


    public static Bitmap scaleBitmap( String filePath, int width, int height ) {

        if( TextUtils.isEmpty(filePath) ) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile( filePath, options );
        options.inSampleSize = calculateInSampleSize( options, width, height );

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile( filePath, options );
    }


    public static Bitmap scaleBitmap( String filePath, float zoomRate ) {

        if( TextUtils.isEmpty( filePath ) ) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile( filePath, options );

        options.inSampleSize = ( int )( 100 / zoomRate );
        if( options.inSampleSize > 1 ) {
            options.inJustDecodeBounds = false;
            LogUtil.d(TAG, "inSampleSize is " + options.inSampleSize);

            return BitmapFactory.decodeFile( filePath, options );
        } else {
            return BitmapFactory.decodeFile( filePath );
        }

    }


    @SuppressLint( "NewApi" )
    public static String getPath( final Context context, final Uri uri ) {

        if( context == null || uri == null ) {
            return null;
        }
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if( isKitKat && DocumentsContract.isDocumentUri(context, uri) ) {
            // ExternalStorageProvider
            if( isExternalStorageDocument( uri ) ) {
                final String docId = DocumentsContract.getDocumentId( uri );
                final String[] split = docId.split( ":" );
                final String type = split[ 0 ];

                if( "primary".equalsIgnoreCase( type ) ) {
                    return Environment.getExternalStorageDirectory() + "/" + split[ 1 ];
                }

            }
            // DownloadsProvider
            else if( isDownloadsDocument( uri ) ) {

                final String id = DocumentsContract.getDocumentId( uri );
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn( context, contentUri, null, null );
            }
            // MediaProvider
            else if( isMediaDocument( uri ) ) {
                final String docId = DocumentsContract.getDocumentId( uri );
                final String[] split = docId.split( ":" );
                final String type = split[ 0 ];

                Uri contentUri = null;
                if( "image".equals( type ) ) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if( "video".equals( type ) ) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if( "audio".equals( type ) ) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[ 1 ] };

                return getDataColumn( context, contentUri, selection, selectionArgs );
            }
        }
        // MediaStore (and general)
        else if( "content".equalsIgnoreCase( uri.getScheme() ) ) {

            // Return the remote address
            if( isGooglePhotosUri( uri ) )
                return uri.getLastPathSegment();

            return getDataColumn( context, uri, null, null );
        }
        // File
        else if( "file".equalsIgnoreCase( uri.getScheme() ) ) {
            return uri.getPath();
        }

        return null;
    }


    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context
     *            The context.
     * @param uri
     *            The Uri to query.
     * @param selection
     *            (Optional) Filter used in the query.
     * @param selectionArgs
     *            (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn( Context context, Uri uri, String selection, String[] selectionArgs ) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query( uri, projection, selection, selectionArgs, null );
            if( cursor != null && cursor.moveToFirst() ) {
                final int index = cursor.getColumnIndexOrThrow( column );
                return cursor.getString( index );
            }
        } finally {
            if( cursor != null )
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument( Uri uri ) {

        return "com.android.externalstorage.documents".equals( uri.getAuthority() );
    }


    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument( Uri uri ) {

        return "com.android.providers.downloads.documents".equals( uri.getAuthority() );
    }


    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument( Uri uri ) {

        return "com.android.providers.media.documents".equals( uri.getAuthority() );
    }


    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri( Uri uri ) {

        return "com.google.android.apps.photos.content".equals( uri.getAuthority() );
    }

    /**
     *
     * 将Bitmap转换成Base64字符串
     * @param bitmap 待转换bitmap
     * @param quality 图片质量（1~100）
     * @return
     *String   转换后Base64字符串
     * @since  1.0.0
     */
    public static String bitmaptoString(Bitmap bitmap, int quality) {

        // 将Bitmap转换成Base64字符串
        StringBuffer string = new StringBuffer();
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();

        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bStream);
            bStream.flush();
            bStream.close();
            byte[] bytes = bStream.toByteArray();
            string.append(Base64.encodeToString(bytes, Base64.NO_WRAP));
        } catch (IOException e) {
            LogUtil.e(TAG, e.getMessage(), e);
        }

        return string.toString();
    }


}
