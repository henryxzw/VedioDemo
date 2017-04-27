package com.henryxzw.videodemo;

import android.app.Application;

import com.yixia.camera.VCamera;


/**
 * Created by Administrator on 2017/4/22.
 */

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
      // VCamera.initialize(this);

        VCamera.initialize(this);
    }
}
