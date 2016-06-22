package com.qiang.meidaproject.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qiang.meidaproject.R;
import com.qiang.meidaproject.activity.base.BaseActivity;

public class MainActivity extends BaseActivity {

    private LinearLayout llMusicQueue;
    private LinearLayout llSearch;
    private LinearLayout llFavouriteQueue;
    private LinearLayout llMyQueue;
    private LinearLayout llDownloadManage;
    private LinearLayout llRecentQueue;
    private LinearLayout llMusicLibrary;


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
