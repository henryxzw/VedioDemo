package com.henryxzw.videodemo.main;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.henryxzw.videodemo.ActivityVedioControlBinding;
import com.henryxzw.videodemo.R;

/**
 * Created by Administrator on 2017/4/24.
 */

public class VideoControlActivity extends AppCompatActivity {

    private ActivityVedioControlBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_control);
        binding.includeTitle.title.setText("视频压缩");
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

        binding.btnSelf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.viewSwitcher.showNext();
            }
        });
        binding.tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.viewSwitcher.showPrevious();
            }
        });
    }
}
