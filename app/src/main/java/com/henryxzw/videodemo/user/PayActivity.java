package com.henryxzw.videodemo.user;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.henryxzw.videodemo.ActivityPayBinding;
import com.henryxzw.videodemo.R;

/**
 * Created by Administrator on 2017/4/24.
 */

public class PayActivity extends AppCompatActivity {
    private ActivityPayBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pay);
        binding.includeTitle.title.setText("在线支付");
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
