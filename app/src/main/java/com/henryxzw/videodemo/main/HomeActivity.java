package com.henryxzw.videodemo.main;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.henryxzw.videodemo.ActivityHomeBinding;
import com.henryxzw.videodemo.R;
import com.henryxzw.videodemo.login.LoginActivity;
import com.henryxzw.videodemo.user.VipActivity;

/**
 * Created by Administrator on 2017/4/24.
 */

public class HomeActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener{
    private ActivityHomeBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        binding.includeTitle.title.setText(R.string.static_msg_name_1);
        binding.includeTitle.toolBar.getMenu().add(0,R.menu.right_person,0,"").setIcon(R.mipmap.nav_icon_member).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        binding.includeTitle.toolBar.setOnMenuItemClickListener(this);

        InitListener();
    }

    private void InitListener()
    {
        binding.btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, VideoControlActivity.class);
                startActivity(intent);

            }
        });
        binding.btnWaterMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, VideoWaterMarkActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId())
        {
            case R.menu.right_person:
            {
                Intent intent = new Intent(HomeActivity.this, VipActivity.class);
                startActivity(intent);
            }
            break;
        }
        return false;
    }
}
