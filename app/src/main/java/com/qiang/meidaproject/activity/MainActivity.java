package com.qiang.meidaproject.activity;

import android.content.Intent;
import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qiang.meidaproject.R;
import com.qiang.meidaproject.utils.LogUtil;
import com.qiang.meidaproject.utils.MediaIDHelper;
import com.qiang.meidaproject.utils.NetworkHelper;
import com.qiang.meidaproject.activity.base.BaseActivity;

import java.util.List;

public class MainActivity extends BaseActivity {

    private LinearLayout llMusicQueue;
    private LinearLayout llSearch;
    private LinearLayout llFavouriteQueue;
    private LinearLayout llMyQueue;
    private LinearLayout llDownloadManage;
    private LinearLayout llRecentQueue;
    private LinearLayout llMusicLibrary;
    private String mMediaId;


    private static final String ARG_MEDIA_ID = "media_id";
    private View mErrorView;
    private TextView mErrorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeToolbar();
        findById();
        addListener();
    }


    protected void onConnected(){
        mMediaId = getMediaId();
        if (mMediaId == null) {
            mMediaId = getMediaBrowser().getRoot();
        }
        updateTitle();

        // Unsubscribing before subscribing is required if this mediaId already has a subscriber
        // on this MediaBrowser instance. Subscribing to an already subscribed mediaId will replace
        // the callback, but won't trigger the initial callback.onChildrenLoaded.
        //
        // This is temporary: A bug is being fixed that will make subscribe
        // consistently call onChildrenLoaded initially, no matter if it is replacing an existing
        // subscriber or not. Currently this only happens if the mediaID has no previous
        // subscriber or if the media content changes on the service side, so we need to
        // unsubscribe first.
        getMediaBrowser().unsubscribe(mMediaId);

        getMediaBrowser().subscribe(mMediaId, mSubscriptionCallback);

        // Add MediaController callback so we can redraw the list when metadata changes:
        if (getActivity().getMediaController() != null) {
            getActivity().getMediaController().registerCallback(mMediaControllerCallback);
        }
    }

    // Receive callbacks from the MediaController. Here we update our state such as which queue
    // is being shown, the current title and description and the PlaybackState.
    private final MediaController.Callback mMediaControllerCallback = new MediaController.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            super.onMetadataChanged(metadata);
            if (metadata == null) {
                return;
            }
            LogUtil.d(TAG, "Received metadata change to media ", metadata.getDescription().getMediaId());
            //mBrowserAdapter.notifyDataSetChanged();
        }

        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackState state) {
            super.onPlaybackStateChanged(state);
            LogUtil.d(TAG, "Received state change: ", state);
            checkForUserVisibleErrors(false);
           // mBrowserAdapter.notifyDataSetChanged();
        }
    };


    private final MediaBrowser.SubscriptionCallback mSubscriptionCallback =
            new MediaBrowser.SubscriptionCallback() {
                @Override
                public void onChildrenLoaded(@NonNull String parentId,
                                             @NonNull List<MediaBrowser.MediaItem> children) {
                    try {
                        LogUtil.d(TAG, "fragment onChildrenLoaded, parentId=" + parentId + "  count=" + children.size());
                        checkForUserVisibleErrors(children.isEmpty());
                       // mBrowserAdapter.clear();
                        for (MediaBrowser.MediaItem item : children) {
                           // mBrowserAdapter.add(item);
                        }
                       // mBrowserAdapter.notifyDataSetChanged();
                    } catch (Throwable t) {
                        LogUtil.e(TAG, "Error on childrenloaded", t);
                    }
                }

                @Override
                public void onError(@NonNull String id) {
                    LogUtil.e(TAG, "browse fragment subscription onError, id=" + id);
                    Toast.makeText(getActivity(), R.string.error_loading_media, Toast.LENGTH_LONG).show();
                    checkForUserVisibleErrors(true);
                }
            };


    private void checkForUserVisibleErrors(boolean forceError) {
        boolean showError = forceError;
        // If offline, message is about the lack of connectivity:
        if (!NetworkHelper.isOnline(getActivity())) {
            mErrorMessage.setText(R.string.error_no_connection);
            showError = true;
        } else {
            // otherwise, if state is ERROR and metadata!=null, use playback state error message:
            MediaController controller = getActivity().getMediaController();
            if (controller != null
                    && controller.getMetadata() != null
                    && controller.getPlaybackState() != null
                    && controller.getPlaybackState().getState() == PlaybackState.STATE_ERROR
                    && controller.getPlaybackState().getErrorMessage() != null) {
                mErrorMessage.setText(controller.getPlaybackState().getErrorMessage());
                showError = true;
            } else if (forceError) {
                // Finally, if the caller requested to show error, show a generic message:
                mErrorMessage.setText(R.string.error_loading_media);
                showError = true;
            }
        }
        mErrorView.setVisibility(showError ? View.VISIBLE : View.GONE);
        LogUtil.d(TAG, "checkForUserVisibleErrors. forceError=", forceError,
                " showError=", showError,
                " isOnline=", NetworkHelper.isOnline(getActivity()));
    }


    private void updateTitle() {
            if (MediaIDHelper.MEDIA_ID_ROOT.equals(mMediaId)) {
                return;
            }

            final String parentId = MediaIDHelper.getParentMediaID(mMediaId);

            // MediaBrowser doesn't provide metadata for a given mediaID, only for its children. Since
            // the mediaId contains the item's hierarchy, we know the item's parent mediaId and we can
            // fetch and iterate over it and find the proper MediaItem, from which we get the title,
            // This is temporary - a better solution (a method to get a mediaItem by its mediaID)
            // is being worked out in the platform and should be available soon.
            LogUtil.d(TAG, "on updateTitle: mediaId=", mMediaId, " parentID=", parentId);
            if (parentId != null) {
                MediaBrowser mediaBrowser = getMediaBrowser();
                LogUtil.d(TAG, "on updateTitle: mediaBrowser is ",
                        mediaBrowser==null?"null":("not null, connected="+mediaBrowser.isConnected()));
                if (mediaBrowser != null && mediaBrowser.isConnected()) {
                    // Unsubscribing is required to guarantee that we will get the initial values.
                    // Otherwise, if there is another callback subscribed to this mediaID, mediaBrowser
                    // will only call this callback when the media content change.
                    mediaBrowser.unsubscribe(parentId);
                    mediaBrowser.subscribe(parentId, new MediaBrowser.SubscriptionCallback() {
                        @Override
                        public void onChildrenLoaded(@NonNull String parentId,
                                                     @NonNull List<MediaBrowser.MediaItem> children) {
                            LogUtil.d(TAG, "Got ", children.size(), " children for ", parentId,
                                    ". Looking for ", mMediaId);
                            for (MediaBrowser.MediaItem item: children) {
                                LogUtil.d(TAG, "child ", item.getMediaId());
                                if (item.getMediaId().equals(mMediaId)) {
                                        //setToolbarTitle(item.getDescription().getTitle());
                                    return;
                                }
                            }
                            getMediaBrowser().unsubscribe(parentId);
                        }

                        @Override
                        public void onError(@NonNull String id) {
                            super.onError(id);
                            LogUtil.d(TAG, "subscribe error: id=", id);
                        }
                    });
                }
            }
    }

    public String getMediaId() {
        Intent intent=getIntent();
        if(intent==null){return null;}
        Bundle args = intent.getExtras();
        if (args != null) {
            return args.getString(ARG_MEDIA_ID);
        }
        return null;
    }
    @Override
    protected void onMediaControllerConnected() {
        super.onMediaControllerConnected();


    }

    private void findById() {
        llMusicQueue= (LinearLayout) findViewById(R.id.llMusicQueue);
        llSearch= (LinearLayout) findViewById(R.id.llSearch);
        llFavouriteQueue = (LinearLayout) findViewById(R.id.llFavouriteQueue);
        llMyQueue= (LinearLayout) findViewById(R.id.llMyQueue);
        llDownloadManage= (LinearLayout) findViewById(R.id.llDownloadManage);
        llRecentQueue= (LinearLayout) findViewById(R.id.llRecentQueue);
        llMusicLibrary= (LinearLayout) findViewById(R.id.llMusicLibrary);
        mErrorView = findViewById(R.id.playback_error);
        if (mErrorView != null) {
            mErrorMessage = (TextView) mErrorView.findViewById(R.id.error_message);
        }

    }

    private void addListener() {
        llMusicQueue.setOnClickListener(onClickListener);
        llSearch.setOnClickListener(onClickListener);
        llFavouriteQueue.setOnClickListener(onClickListener);
        llMyQueue.setOnClickListener(onClickListener);
        llDownloadManage.setOnClickListener(onClickListener);
        llRecentQueue.setOnClickListener(onClickListener);
        llMusicLibrary.setOnClickListener(onClickListener);

    }

    private View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent=new Intent();
            switch (v.getId()){
                case R.id.llMusicQueue:
                    intent.setClass(MainActivity.this,MusicPlayerActivity.class);
                    startActivity(intent);
                    break;
                case R.id.llSearch:
                    intent.setClass(MainActivity.this,SearchActivity.class);
                    startActivity(intent);
                    break;
                case R.id.llFavouriteQueue:
                    intent.setClass(MainActivity.this,FavouriteActivity.class);
                    startActivity(intent);
                    break;
                case R.id.llMyQueue:
                    intent.setClass(MainActivity.this,MyMusicMenuActivity.class);
                    startActivity(intent);
                    break;
                case R.id.llDownloadManage:
                    intent.setClass(MainActivity.this,DownloadManageActivity.class);
                    startActivity(intent);
                    break;
                case R.id.llRecentQueue:
                    intent.setClass(MainActivity.this,RecentPlayActivity.class);
                    startActivity(intent);
                case R.id.llMusicLibrary:
                    intent.setClass(MainActivity.this,MusicLibraryActivity.class);
                    startActivity(intent);
                    break;
                default:break;

            }
        }
    };


}
