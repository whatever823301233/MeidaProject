package com.qiang.meidaproject.activity;

import android.os.Bundle;

import com.qiang.meidaproject.R;
import com.qiang.meidaproject.activity.base.BaseActivity;

public class RecentPlayActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_play);
        initializeToolbar();
    }
}
