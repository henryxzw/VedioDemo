package com.henryxzw.videodemo;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

import com.yixia.videoeditor.adapter.UtilityAdapter;
import com.yixia.weibo.sdk.FFMpegUtils;
import com.yixia.weibo.sdk.VCamera;

import java.io.File;

/**
 * Created by Administrator on 2017/4/22.
 */

public class ControlActivity extends Activity implements SurfaceHolder.Callback {
    private com.henryxzw.videodemo.ActivityControlBinding binding;
    private String path;

    private SurfaceHolder surfaceHolder;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_control);
        InitListener();

        surfaceHolder = binding.surfaceView.getHolder();
        surfaceHolder.addCallback(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        path = getIntent().getStringExtra("path");
    }

    private void InitListener()
    {
        binding.btnSelf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        binding.btnAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pic = Environment.getExternalStorageDirectory()+ File.separator+"u.jpg";
               // UtilityAdapter.FFmpegRun("","ffmpeg -i "+path+" "+pic+" -filter_complex \"overlay=x=0:y=0\" -f mp4 out.mp4");
            }
        });
    }

    public void Play()
    {
        try {


            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDisplay(surfaceHolder);
            mediaPlayer.setDataSource(this, Uri.parse( path));
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
            mediaPlayer.prepare();
        }catch (Exception ex)
        {
            Log.e("controlactivity",ex.getMessage());
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //Play();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
