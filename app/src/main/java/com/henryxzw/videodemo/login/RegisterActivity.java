package com.henryxzw.videodemo.login;

import android.database.DatabaseUtils;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.henryxzw.videodemo.ActivityRegisterBinding;
import com.henryxzw.videodemo.R;

/**
 * Created by Administrator on 2017/4/24.
 */

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register);
        binding.includeTitle.title.setText("在线注册");

        InitListener();

    }

    private void InitListener()
    {

    }
}
