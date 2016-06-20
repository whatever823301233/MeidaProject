package com.qiang.meidaproject.ui.activity;

import android.app.UiModeManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.qiang.meidaproject.R;
import com.qiang.meidaproject.ui.activity.base.BaseActivity;
import com.qiang.meidaproject.common.utils.LogUtil;

public class NowPlayingActivity extends BaseActivity {

    private static final String TAG = LogUtil.makeLogTag(NowPlayingActivity.class);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);
        initializeToolbar();
        Intent newIntent;
        /**判断是否为  电视模式*/
        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            LogUtil.d(TAG, "Running on a TV Device");
            newIntent = new Intent(this, PlaceholderActivity.class);// TODO: 2016/3/30 应改为电视界面
        } else {
            LogUtil.d(TAG, "Running on a non-TV Device");
            newIntent = new Intent(this, MusicPlayerActivity.class);
        }
        startActivity(newIntent);
        finish();

    }
}
