package com.henryxzw.videodemo;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Created by Administrator on 2017/4/22.
 */

public class VideoInfo {
    private String name;
    private float size;
    private String path;
    private Uri uri;
    private Bitmap preview;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Bitmap getPreview() {
        return preview;
    }

    public void setPreview(Bitmap preview) {
        this.preview = preview;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
