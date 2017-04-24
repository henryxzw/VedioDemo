package com.henryxzw.videodemo.login;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.henryxzw.videodemo.ActivityLoginBinding;
import com.henryxzw.videodemo.R;
import com.henryxzw.videodemo.main.HomeActivity;

/**
 * Created by Administrator on 2017/4/24.
 */

public class LoginActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener{
    private ActivityLoginBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.includeTitle.title.setText("登录");
        binding.includeTitle.toolBar.getMenu().add(0,R.menu.right_person,0,"").setIcon(R.mipmap.nav_icon_member).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        binding.includeTitle.toolBar.setOnMenuItemClickListener(this);

        InitListener();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId())
        {
            case R.menu.right_person:
            {
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
            }
                break;
        }
        return false;
    }

    private void InitListener()
    {
        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
        binding.tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);

            }
        });
        binding.tvFoundPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, FoundPasswordActivity.class);
                startActivity(intent);
            }
        });
    }
}
