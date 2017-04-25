package com.henryxzw.videodemo.user;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.henryxzw.videodemo.ActivityVipBinding;
import com.henryxzw.videodemo.R;

/**
 * Created by Administrator on 2017/4/24.
 */

public class VipActivity extends AppCompatActivity {
    private ActivityVipBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_vip);
        binding.includeTitle.title.setText("VIP会员");
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
        binding.btnVipTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(VipActivity.this,PayActivity.class);
                startActivity(intent);
            }
        });
    }
}
