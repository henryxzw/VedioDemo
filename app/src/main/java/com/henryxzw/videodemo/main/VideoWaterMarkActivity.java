package com.henryxzw.videodemo.main;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.henryxzw.videodemo.ActivityVideoWaterMarkBinding;
import com.henryxzw.videodemo.R;

/**
 * Created by Administrator on 2017/4/24.
 */

public class VideoWaterMarkActivity extends AppCompatActivity {
    private ActivityVideoWaterMarkBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_water_mark);
        binding.includeTitle.title.setText("视频加水印");
        binding.includeTitle.toolBar.setNavigationIcon(R.mipmap.arrow_left);

        InitListener();
    }

    private void InitListener()
    {
        binding.includeTitle.toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
