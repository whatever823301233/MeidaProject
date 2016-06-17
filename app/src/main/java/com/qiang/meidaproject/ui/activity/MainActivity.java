package com.qiang.meidaproject.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.qiang.meidaproject.R;
import com.qiang.meidaproject.ui.activity.base.BaseActivity;

public class MainActivity extends BaseActivity {

    private LinearLayout llMusicQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeToolbar();
        findById();
        addListener();

    }

    private void findById() {
        llMusicQueue= (LinearLayout) findViewById(R.id.ll_music_queue);
    }

    private void addListener() {
        llMusicQueue.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){

                case R.id.ll_music_queue:
                    Intent intent =new Intent(MainActivity.this,MusicPlayerActivity.class);
                    startActivity(intent);
                    break;
                default:break;

            }
        }
    };


}
