package com.henryxzw.videodemo.login;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.henryxzw.videodemo.ActivityFoundPasswordBinding;
import com.henryxzw.videodemo.R;

/**
 * Created by Administrator on 2017/4/24.
 */

public class FoundPasswordActivity extends AppCompatActivity {
    private ActivityFoundPasswordBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_found_password);
        binding.includeTitle.title.setText("忘记密码");
    }
}
