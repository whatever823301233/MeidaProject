package com.qiang.meidaproject.ui.activity;

import android.os.Bundle;

import com.qiang.meidaproject.R;
import com.qiang.meidaproject.ui.activity.base.BaseActivity;

public class DownloadManageActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_manage);
        initializeToolbar();
    }


}
