package com.qiang.meidaproject.common.utils.image;

/**
 * Created by Qiang on 2016/3/24.
 */
public class ImageCache {

    /*private static final String TAG = "ImageCache";

    private static volatile ImageCache sInstance;
    private Context mContext;
    private ResourceManager mResourceManager;
    private String mCacheFolder;

    private DiskLruCache mDiskLruCache;
    private DiskLruCache mDiskLruCacheNoSkin;

    public static final String DEFAULT_IMG_FOLDER = "RemoteMenuImage";// asset目录下面的文件夹名
    private static final String DEFAULT_NO_SKIN_FOLDER = "noskin";

    private ExecutorService mExecutorService = Executors.newFixedThreadPool(5);

    private static final int MSG_DOWNLOAD_SUC = 1;
    private static final int MSG_DOWNLOAD_FAILED = 2;
    private static final int MSG_DOWNLOADING = 3;
    private Handler mHandler = new Handler( Looper.getMainLooper() ) {

        public void handleMessage( Message msg ) {

            LogUtil.d(TAG, "msg is " + msg);
            switch( msg.what ) {
                case MSG_DOWNLOAD_FAILED:
                    break;
                case MSG_DOWNLOAD_SUC:
                    ImageHolder holder = ( ImageHolder )msg.obj;
                    LogUtil.d( TAG, "img is " + holder.url );
                    if( holder.isNotInSkin ) {
                        setImage( mDiskLruCacheNoSkin, holder );
                    } else {
                        setImage( mDiskLruCache, holder );
                    }
                    break;
                case MSG_DOWNLOADING: {
                    ImageHolder holder1 = ( ImageHolder )msg.obj;
                    // download( holder1 );
                    displayImage( holder1.url, holder1.view, holder1.defaultDrawable );
                }
                break;
            }
        }
    };


    private class ImageHolder {

        String url;
        ImageView view;
        Drawable defaultDrawable;
        boolean isNotInSkin;
    }


    private ImageCache( Context context ) {

        mContext = context.getApplicationContext();
        mResourceManager = ResourceManager.getInstance( mContext );

        mCacheFolder = mResourceManager.getImageCachePath();
        setCacheFolder( mCacheFolder );
    }


    public static ImageCache getInstance( Context context ) {

        if( context == null ) {
            return null;
        }

        if( sInstance == null ) {
            synchronized( ImageCache.class ) {
                if( sInstance == null ) {
                    sInstance = new ImageCache( context );
                }
            }
        }
        return sInstance;
    }


    public void setCacheFolder( String cacheFolder ) {

        try {
            File cacheDir = new File( cacheFolder );
            if( !cacheDir.exists() ) {
                cacheDir.mkdirs();
            }
            if( mDiskLruCache != null ) {
                mDiskLruCache.close();
            }
            mDiskLruCache = DiskLruCache.open( cacheDir, getAppVersion( mContext ), 1, 20 * 1024 * 1024 );
            mCacheFolder = cacheFolder;
        } catch( IOException e ) {
            LogUtil.e( TAG, e.getMessage(), e );
        }
    }


    private void initNoSkin() {

        try {
            String cacheFolder = mContext.getFilesDir().getAbsolutePath() + File.separator + DEFAULT_NO_SKIN_FOLDER;
            File cacheDir = new File( cacheFolder );
            if( !cacheDir.exists() ) {
                cacheDir.mkdirs();
            }
            if( mDiskLruCacheNoSkin == null ) {
                mDiskLruCacheNoSkin = DiskLruCache.open( cacheDir, getAppVersion( mContext ), 1, 20 * 1024 * 1024 );
            }
        } catch( IOException e ) {
            LogUtil.e( TAG, e.getMessage(), e );
        }
    }


    private void setImage( DiskLruCache diskLruCache, ImageHolder holder ) {

        if( holder == null || holder.view == null ) {
            return;
        }
        String key = hashKeyForDisk( holder.url );
        try {
            DiskLruCache.Snapshot snapShot = diskLruCache.get( key );
            if( snapShot == null ) {
                holder.view.setImageDrawable( holder.defaultDrawable );
            } else {
                if( holder.url.equals( holder.view.getTag() ) ) {
                    InputStream is = snapShot.getInputStream( 0 );
                    Bitmap bitmap = BitmapFactory.decodeStream( is );
                    holder.view.setImageBitmap( bitmap );
                    mResourceManager.setBitmap( key, bitmap );
                }
            }
        } catch( IOException e ) {
            LogUtil.e( TAG, e.getMessage(), e );
        }

    }


    public void displayImage( final String imageUrl, ImageView view, int defaultDrawableResId ) {

        if( view == null ) {
            return;
        }
        Drawable defaultDrawable = mResourceManager.getDrawable( defaultDrawableResId );
        displayImage( imageUrl, view, defaultDrawable );
    }


    public void displayImage( final String imageUrl, ImageView view, Drawable defaultDrawable ) {

        displayImage( imageUrl, view, defaultDrawable, false );
    }


    public void displayImageNoSkin( final String imageUrl, ImageView view, int defaultDrawableResId ) {

        if( view == null ) {
            return;
        }
        Drawable defaultDrawable = mResourceManager.getDrawable( defaultDrawableResId );
        displayImageNoSkin( imageUrl, view, defaultDrawable );
    }


    public void displayImageNoSkin( final String imageUrl, ImageView view, Drawable defaultDrawable ) {

        displayImage( imageUrl, view, defaultDrawable, true );
    }


    private void displayImage( final String imageUrl, ImageView view, Drawable defaultDrawable, boolean isNotInSkin ) {

        if( view == null ) {
            return;
        }

        // 若为非皮肤图片，则可设置cache路径
        if( !isNotInSkin ) {
            // 设置cache路径，保障换肤后正常
            if( TextUtils.isEmpty( mCacheFolder ) || !mCacheFolder.equals( mResourceManager.getImageCachePath() ) ) {
                setCacheFolder( mResourceManager.getImageCachePath() );
            }
        }

        String key = hashKeyForDisk( imageUrl );
        Bitmap cacheBitmap = mResourceManager.getBitmap( key );
        if( cacheBitmap != null ) {
            view.setImageBitmap( cacheBitmap );
            return;
        }

        ImageHolder holder = new ImageHolder();
        holder.url = imageUrl;
        holder.view = view;
        holder.view.setTag( imageUrl );
        holder.defaultDrawable = defaultDrawable;
        if( isNotInSkin ) {
            holder.isNotInSkin = true;
            if( mDiskLruCacheNoSkin == null ) {
                initNoSkin();
            }
            download( mDiskLruCacheNoSkin, holder, key );
        } else {
            holder.isNotInSkin = false;
            download( mDiskLruCache, holder, key );
        }
    }


    public void removeImage( String imageUrl ) {

        try {
            String key = hashKeyForDisk( imageUrl );
            if( mDiskLruCache != null ) {
                mDiskLruCache.remove( key );
            }
        } catch( IOException e ) {
            LogUtil.e( TAG, e.getMessage(), e );
        }
    }


    public void removeAll() {

        try {
            if( mDiskLruCache != null ) {
                mDiskLruCache.delete();
            }
        } catch( IOException e ) {
            LogUtil.e( TAG, e.getMessage(), e );
        }
    }


    private void download( DiskLruCache diskLruCache, ImageHolder holder, String key ) {

        if( diskLruCache != null ) {
            DiskLruCache.Snapshot snapShot;
            try {
                snapShot = diskLruCache.get( key );
                if( snapShot == null ) {
                    holder.view.setImageDrawable( holder.defaultDrawable );
                } else {
                    InputStream is = snapShot.getInputStream( 0 );
                    Bitmap bitmap = BitmapFactory.decodeStream( is );
                    holder.view.setImageBitmap( bitmap );
                    return;
                }
            } catch( IOException e ) {
                e.printStackTrace();
                return;
            }
        }

        mExecutorService.submit( new DownloadThread( mHandler, diskLruCache, holder ) );
    }


    private class DownloadThread extends Thread {

        private Handler mHandler;
        private ImageHolder mHolder;
        private DiskLruCache mDiskLruCache;


        // private OnMultiImageListener mListener;

        public DownloadThread( Handler handler, DiskLruCache diskLruCache, ImageHolder holder ) {

            mHandler = handler;
            mHolder = holder;
            mDiskLruCache = diskLruCache;
            // mListener = listener;
        }


        @Override
        public void run() {

            if( mHolder != null && mHandler != null && mDiskLruCache != null ) {
                Message msg = mHandler.obtainMessage();

                String imageUrl = mHolder.url;
                try {
                    final String key = hashKeyForDisk( imageUrl );
                    DiskLruCache.Editor editor = mDiskLruCache.edit( key );
                    if( editor != null ) {
                        OutputStream outputStream = editor.newOutputStream( 0 );
                        if( downloadUrlToStream( imageUrl, outputStream ) ) {
                            editor.commit();
                            msg.what = MSG_DOWNLOAD_SUC;
                            msg.obj = mHolder;
                        } else {
                            editor.abort();
                            msg.what = MSG_DOWNLOAD_FAILED;
                        }
                    }

                    mDiskLruCache.flush();
                } catch( IOException e ) {
                    LogUtil.e( TAG, e.getMessage(), e );
                    msg.what = MSG_DOWNLOAD_FAILED;
                }

                mHandler.sendMessage( msg );
            }
        }
    }


    public boolean copy( String assetPath ) {

        if( mDiskLruCache == null || TextUtils.isEmpty( assetPath ) ) {
            return false;
        }

        boolean isSuc = true;
        try {
            String[] strName = mContext.getResources().getAssets().list( assetPath );
            InputStream in = null;
            BufferedInputStream inBuff = null;
            String folder = mDiskLruCache.getDirectory().getAbsolutePath();
            ResourceManager manager = ResourceManager.getInstance( mContext );
            for( int i = 0; i < strName.length; i++ ) {
                String imageName = strName[ i ];
                String imagePath = manager.getImageUrl( imageName );
                String key = hashKeyForDisk( imagePath );
                File file = new File( folder + File.separator + key );
                if( !file.exists() ) {
                    in = mContext.getResources().getAssets().open( assetPath + "/" + strName[ i ] );
                    inBuff = new BufferedInputStream( in );
                    copyImage( key, inBuff );
                    in.close();
                    inBuff.close();
                }
            }
        } catch( IOException e ) {
            LogUtil.e( TAG, e.getMessage(), e );
            isSuc = false;
        }

        return isSuc;
    }


    private void copyImage( String key, BufferedInputStream inbuff ) {

        try {
            DiskLruCache.Snapshot snapShot = mDiskLruCache.get( key );
            if( snapShot == null ) {
                DiskLruCache.Editor editor = mDiskLruCache.edit( key );
                if( editor != null ) {
                    OutputStream outputStream = editor.newOutputStream( 0 );
                    FileUtil.copyFile( inbuff, outputStream );
                    editor.commit();
                }
                mDiskLruCache.flush();
            }
        } catch( IOException e ) {
            LogUtil.e( TAG, e.getMessage(), e );
        }
    }


    public void renameto( String url, String src ) {

        try {
            String key = hashKeyForDisk( url );
            DiskLruCache.Snapshot snapShot = mDiskLruCache.get( key );
            if( snapShot == null ) {
                DiskLruCache.Editor editor = mDiskLruCache.edit( key );
                if( editor != null ) {
                    OutputStream outputStream = editor.newOutputStream( 0 );
                    FileUtil.copyFile( src, outputStream );
                    ditor.commit();
               }
                mDiskLruCache.flush();
            }
        } catch( IOException e ) {
            LogUtil.e( TAG, e.getMessage(), e );
        }
    }


    private int getAppVersion( Context context ) {

        try {
            PackageInfo info = context.getPackageManager().getPackageInfo( context.getPackageName(), 0 );
            return info.versionCode;
        } catch( NameNotFoundException e ) {
            e.PackageManager.printStackTrace();
        }
        return 1;
    }


    public static String hashKeyForDisk( String key ) {

        if( TextUtils.isEmpty( key ) ) {
            return null;
        }
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance( "MD5" );
            mDigest.update( key.getBytes() );
            cacheKey = bytesToHexString( mDigest.digest() );
        } catch( NoSuchAlgorithmException e ) {
            cacheKey = String.valueOf( key.hashCode() );
        }
        return cacheKey;
    }


    private static String bytesToHexString( byte[] bytes ) {

        StringBuilder sb = new StringBuilder();
        for( int i = 0; i < bytes.length; i++ ) {
            String hex = Integer.toHexString( 0xFF & bytes[ i ] );
            if( hex.length() == 1 ) {
                sb.append( '0' );
            }
            sb.append( hex );
        }
        return sb.toString();
    }


    private static boolean downloadUrlToStream( String urlString, OutputStream outputStream ) {

        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        try {
            final URL url = new URL( urlString );
            urlConnection = ( HttpURLConnection )url.openConnection();
            in = new BufferedInputStream( urlConnection.getInputStream(), 8 * 1024 );
            out = new BufferedOutputStream( outputStream, 8 * 1024 );
            int b;
            while( ( b = in.read() ) != -1 ) {
                out.write( b );
            }
            return true;
        } catch( final IOException e ) {
            e.printStackTrace();
        } finally {
            if( urlConnection != null ) {
                urlConnection.disconnect();
            }
            try {
                if( out != null ) {
                    out.close();
                }
                if( in != null ) {
                    in.close();
                }
            } catch( final IOException e ) {
                e.printStackTrace();
            }
        }
        return false;
    }


    *//**
     *
     *
     * OnMultiImageListener
     *
     * yanbo 2015年8月28日 下午3:47:16
     *
     * @version 1.2.0
     *
     *//*
    public interface OnMultiImageListener {

        void finish( ArrayList<String> imagePaths );
    }


    *//**
     *
     * getMultiImage(这里用一句话描述这个方法的作用) (这里描述这个方法适用条件 – 可选)
     *
     * @param imageUrls
     * @param listener
     * @permission void
     * @exception
     * @since 1.2.0
     *//*
    public void getMultiImage( ArrayList<String> imageUrls, OnMultiImageListener listener ) {

        if( listener == null ) {
            return;
        }

        if( imageUrls == null || imageUrls.size() < 1 ) {
            listener.finish( null );
            return;
        }

        mExecutorService.submit( new MultiDownloadThread( mHandler, mDiskLruCache, imageUrls, listener ) );
    }


    *//**
     *
     *
     * MultiDownloadThread
     *
     * yanbo 2015年8月28日 下午3:47:03
     *
     * @version 1.2.0
     *
     *//*
    private class MultiDownloadThread extends Thread {

        private Handler mHandler;
        private ArrayList<String> mImageUrls;
        private DiskLruCache mDiskLruCache;
        private OnMultiImageListener mListener;


        public MultiDownloadThread( Handler handler, DiskLruCache diskLruCache, ArrayList<String> imageUrls,
                                    OnMultiImageListener listener ) {

            mHandler = handler;
            mImageUrls = imageUrls;
            mDiskLruCache = diskLruCache;
            mListener = listener;
        }


        @Override
        public void run() {

            if( mImageUrls != null && mHandler != null && mDiskLruCache != null ) {
                Message msg = mHandler.obtainMessage();

                ArrayList<String> result = new ArrayList<String>();
                try {
                    for( String imageUrl : mImageUrls ) {
                        final String key = hashKeyForDisk( imageUrl );
                        DiskLruCache.Editor editor = mDiskLruCache.edit( key );
                        if( editor != null ) {
                            OutputStream outputStream = editor.newOutputStream( 0 );
                            if( downloadUrlToStream( imageUrl, outputStream ) ) {
                                editor.commit();
                                result.add( key );
                            } else {
                                editor.abort();
                                sendResult( null );
                                return;
                            }
                        }
                        mDiskLruCache.flush();
                    }

                    sendResult( result );
                } catch( IOException e ) {
                    LogUtil.e( TAG, e.getMessage(), e );
                    sendResult( null );
                }
                sendResult( null );
            }
        }


        private void sendResult( final ArrayList<String> result ) {

            if( mListener != null ) {
                mHandler.post( new Runnable() {

                    @Override
                    public void run() {

                        mListener.finish( result );
                    }
                } );
            }
        }
    }


    *//**
     *
     * getDrawableByPath(这里用一句话描述这个方法的作用) (这里描述这个方法适用条件 – 可选)
     *
     * @param imageUrl
     * @return
     * @permission Drawable
     * @exception
     * @since 1.2.0
     *//*
    public Drawable getDrawableByPath( String imageUrl ) {

        if( TextUtils.isEmpty( imageUrl ) ) {
            return null;
        }
        return getDrawableByKey( hashKeyForDisk( imageUrl ) );
    }


    *//**
     *
     * getDrawableByKey(这里用一句话描述这个方法的作用) (这里描述这个方法适用条件 – 可选)
     *
     * @param key
     * @return
     * @permission Drawable
     * @exception
     * @since 1.2.0
     *//*
    public Drawable getDrawableByKey( String key ) {

        if( !TextUtils.isEmpty(key) ) {
            Bitmap cacheBitmap = mResourceManager.getBitmap( key );
            if( cacheBitmap != null ) {
                Drawable drawable = new BitmapDrawable( mContext.getResources(), cacheBitmap );
                return drawable;
            }

            try {
                DiskLruCache.Snapshot snapShot = mDiskLruCache.get( key );
                if( snapShot != null ) {
                    InputStream is = snapShot.getInputStream( 0 );
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    Drawable drawable = new BitmapDrawable( mContext.getResources(), bitmap );
                    is.close();
                    mResourceManager.setBitmap( key, bitmap );
                    return drawable;
                }
            } catch( IOException e ) {
                e.printStackTrace();
            }
        }

        return null;
    }*/

}
