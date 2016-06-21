package com.qiang.meidaproject.activity;

import android.os.Bundle;

import com.qiang.meidaproject.R;
import com.qiang.meidaproject.activity.base.BaseActivity;

/**
 * Created by Qiang on 2016/3/28.
 *
 */
public class PlaceholderActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeholder);
        initializeToolbar();
    }

}
