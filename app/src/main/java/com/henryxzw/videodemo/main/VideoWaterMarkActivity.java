package com.henryxzw.videodemo.main;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.henryxzw.videodemo.ActivityVideoWaterMarkBinding;
import com.henryxzw.videodemo.R;
import com.henryxzw.videodemo.VideoInfo;
import com.yixia.videoeditor.adapter.UtilityAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;

import videocompress.util.AppUtil;
import videocompress.util.Worker;
import videocompress.video.VideoCompressListener;
import videocompress.video.VideoCompressor;

/**
 * Created by Administrator on 2017/4/24.
 */

public class VideoWaterMarkActivity extends AppCompatActivity {
    private ActivityVideoWaterMarkBinding binding;

    private VideoInfo videoInfo;
    private String wm_img="";
    private int originW=0,originH=0,place_int=0;
    private String[] places = new String[]{"20:20","20:main_h-overlay_h","main_w-overlay_w:20","main_w-overlay_w:main_h-overlay_h"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_water_mark);
        binding.includeTitle.title.setText("视频加水印");
        binding.includeTitle.toolBar.setNavigationIcon(R.mipmap.arrow_left);

        InitData();
        InitListener();
    }

    private void InitData()
    {
        videoInfo = new VideoInfo();
    }

    private void InitListener()
    {
        binding.includeTitle.toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.ivTemp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        binding.ivTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowOrHide(true);
            }
        });
        binding.btnPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.linearBottomWord.getVisibility()==View.VISIBLE)
                {
                    return;
                }
                if(TextUtils.isEmpty( videoInfo.getPath()))
                {
                    Toast.makeText(VideoWaterMarkActivity.this,"请先选择视频文件",Toast.LENGTH_LONG).show();
                }
                else
                {
                    binding.tvWordTemp.setText("");
                    chooseImg();
                }

            }
        });
        binding.btnWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.linearBottom.getVisibility()==View.VISIBLE)
                {
                    return;
                }
                if(TextUtils.isEmpty( videoInfo.getPath()))
                {
                    Toast.makeText(VideoWaterMarkActivity.this,"请先选择视频文件",Toast.LENGTH_LONG).show();
                }
                else
                {
                    binding.ivTemp.setImageBitmap(null);
                    ShowOrHideWord(true);
                }
            }
        });
        binding.btnCancelWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowOrHideWord(false);
                binding.tvWordTemp.setText("");

            }
        });
        binding.btnSetWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.tvWordTemp.setDrawingCacheEnabled(true);
                Bitmap bitmap =  binding.tvWordTemp.getDrawingCache();
                float m = binding.tvWordTemp.getWidth()*1.0f/(binding.ivBase.getWidth()+50);
                float k = binding.tvWordTemp.getHeight()*1.0f/(binding.ivBase.getHeight()+50);
                float w = videoInfo.getPreview().getWidth()*m;
                float h = videoInfo.getPreview().getHeight()*k;
                Matrix matrix = new Matrix();

                matrix.postScale(w/bitmap.getWidth(),h/bitmap.getHeight());
                if(w<h)
                {
                    matrix.postRotate(-90);
                }
                Bitmap newBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);

                try {
                    FileOutputStream fos = new FileOutputStream(AppUtil.getAppDir() + "/temp.png");
                    newBitmap.compress(Bitmap.CompressFormat.PNG, 100,fos );
                    fos.flush();
                    fos.close();
                    bitmap.recycle();
                    newBitmap.recycle();
                    binding.tvWordTemp.destroyDrawingCache();
                    Toast.makeText(VideoWaterMarkActivity.this,"图片生成成功，可以生成加水印视频",Toast.LENGTH_LONG).show();

                }catch (Exception ex)
                {
                    Toast.makeText(VideoWaterMarkActivity.this,"图片生成失败，"+ex.getMessage(),Toast.LENGTH_LONG).show();
                }
                ShowOrHideWord(false);
            }
        });
        binding.btnDo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InitPlaceInt();
                final ProgressDialog progressDialog = new ProgressDialog(VideoWaterMarkActivity.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setMessage("水印添加中...");
                progressDialog.setCancelable(true);
                progressDialog.setMax(100);
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        UtilityAdapter.FFmpegRun("","ffmpeg -i "+videoInfo.getPath()+" -i "+(AppUtil.getAppDir() + "/temp.png")+
                                " -filter_complex overlay="+places[place_int]+" -f mp4 "+AppUtil.getAppDir() + "/video_compress.mp4");
                        Worker.postMain(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                Toast.makeText(VideoWaterMarkActivity.this,"添加水印成功，位置："+AppUtil.getAppDir() + "/video_compress.mp4",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).start();

//
            }
        });

        binding.tvChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseVideo();
            }
        });
        binding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.ivTemp.setImageBitmap(null);
                wm_img = "";
                binding.rgPlace.clearCheck();

                ShowOrHide(false);
            }
        });
        binding.btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = BitmapFactory.decodeFile(wm_img);
               float x = binding.seekBarSize.getProgress()*1.0f/100;
                Matrix matrix = new Matrix();
                matrix.postScale(x,x);
                if(videoInfo.getPreview().getWidth()<videoInfo.getPreview().getHeight())
                {
                    matrix.postRotate(-90);
                }
                Bitmap newbitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);

                try {
                    FileOutputStream fos = new FileOutputStream(AppUtil.getAppDir() + File.separator + "temp.png");
                    newbitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    newbitmap.recycle();
                    bitmap.recycle();
                    fos.flush();
                    fos.close();
                    Toast.makeText(VideoWaterMarkActivity.this,"图片生成成功，可以生成加水印视频",Toast.LENGTH_LONG).show();
                    ShowOrHide(false);
                }
                catch (Exception ex)
                {
                    Toast.makeText(VideoWaterMarkActivity.this,"图片生成失败，"+ex.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });
        binding.rgPlace.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
               UpdateImgP(checkedId);
            }
        });
        binding.rgPlaceW.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                UpdateTextP(checkedId);
            }
        });
        binding.seekBarSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.ivTemp.getLayoutParams();
                params.width = originW *progress/100;
                params.height = originH *progress/100;
                binding.ivTemp.setLayoutParams(params);
                UpdateImgP(binding.rgPlace.getCheckedRadioButtonId());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(originH==0) {
                    originW = binding.ivTemp.getWidth();
                    originH = binding.ivTemp.getHeight();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        binding.edtWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                    binding.tvWordTemp.setText(s);
            }
        });
        binding.seekBarTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.tvWordTemp.setTextSize( 13+(progress-5));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private void UpdateTextP(int checkedId)
    {
        if(checkedId!=0)
        {
            if(checkedId==R.id.rb1_w)
            {
                binding.tvWordTemp.setX(binding.ivBase.getX());
                binding.tvWordTemp.setY(binding.ivBase.getY());

            }
            else  if(checkedId==R.id.rb2_w)
            {
                binding.tvWordTemp.setX(binding.ivBase.getX());
                binding.tvWordTemp.setY(binding.ivBase.getY()+binding.ivBase.getHeight()-binding.tvWordTemp.getHeight());

            }
            else if(checkedId==R.id.rb3_w)
            {
                binding.tvWordTemp.setX(binding.ivBase.getX()+binding.ivBase.getWidth()-binding.tvWordTemp.getWidth());
                binding.tvWordTemp.setY(binding.ivBase.getY());

            }
            else if(checkedId==R.id.rb4_w)
            {
                binding.tvWordTemp.setX(binding.ivBase.getX()+binding.ivBase.getWidth()-binding.tvWordTemp.getWidth());
                binding.tvWordTemp.setY(binding.ivBase.getY()+binding.ivBase.getHeight()-binding.tvWordTemp.getHeight());

            }
        }
    }
    private void UpdateImgP(int checkedId)
    {
        if(checkedId!=0)
        {
            if(checkedId==R.id.rb1)
            {
                binding.ivTemp.setX(binding.ivBase.getX());
                binding.ivTemp.setY(binding.ivBase.getY());
            }
            else  if(checkedId==R.id.rb2)
            {
                binding.ivTemp.setX(binding.ivBase.getX());
                binding.ivTemp.setY(binding.ivBase.getY()+binding.ivBase.getHeight()-binding.ivTemp.getHeight());
            }
            else if(checkedId==R.id.rb3)
            {
                binding.ivTemp.setX(binding.ivBase.getX()+binding.ivBase.getWidth()-binding.ivTemp.getWidth());
                binding.ivTemp.setY(binding.ivBase.getY());
            }
            else if(checkedId==R.id.rb4)
            {
                binding.ivTemp.setX(binding.ivBase.getX()+binding.ivBase.getWidth()-binding.ivTemp.getWidth());
                binding.ivTemp.setY(binding.ivBase.getY()+binding.ivBase.getHeight()-binding.ivTemp.getHeight());
            }
        }

    }

    public void InitPlaceInt()
    {
        if(binding.rgPlaceW.getCheckedRadioButtonId()==R.id.rb1_w || binding.rgPlaceW.getCheckedRadioButtonId()==R.id.rb1)
        {
            place_int =0;
        }
        else if(binding.rgPlaceW.getCheckedRadioButtonId()==R.id.rb2_w || binding.rgPlaceW.getCheckedRadioButtonId()==R.id.rb2)
        {
            place_int =2;
        }
        else if(binding.rgPlaceW.getCheckedRadioButtonId()==R.id.rb3_w || binding.rgPlaceW.getCheckedRadioButtonId()==R.id.rb3)
        {
            place_int =1;
        }
        else if(binding.rgPlaceW.getCheckedRadioButtonId()==R.id.rb4_w || binding.rgPlaceW.getCheckedRadioButtonId()==R.id.rb4)
        {
            place_int =3;
        }

        if(videoInfo.getPreview().getWidth()<videoInfo.getPreview().getHeight())
        {
             place_int = (4+(place_int-1))%4;
        }
    }

    private void InitVideoData()
    {
        int width = binding.relativeBg.getWidth() ;
        int height =binding.relativeBg.getHeight();

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) binding.ivBase.getLayoutParams();


        float m = videoInfo.getPreview().getWidth()*1.0f/videoInfo.getPreview().getHeight()*1.0f;
        if(width/m<=height )
        {
            lp.width = width;
            lp.height = (int)(width/m);
        }
        else if(height*m<=width)
        {
            lp.width = (int)(height*m);
            lp.height = height;
        }
        else {
            float x1 = width / videoInfo.getPreview().getWidth();
            float x2 = height / videoInfo.getPreview().getHeight();

            lp.width = (int) (videoInfo.getPreview().getWidth() * (x1 > x2 ? x2 : x1));
            lp.height = (int) (videoInfo.getPreview().getHeight() * (x1 > x2 ? x2 : x1));
        }
        binding.ivBase.setLayoutParams(lp);
        binding.ivBase.setImageBitmap(videoInfo.getPreview());
        binding.ivTemp.setX(binding.ivBase.getX());
        binding.ivTemp.setY(binding.ivBase.getY());

    }
    private void ShowOrHideWord(final boolean show)
    {

        if((show && binding.linearBottomWord.getVisibility()!= View.VISIBLE) || (!show && binding.linearBottomWord.getVisibility()==View.VISIBLE)) {
            int height = binding.linearBottomWord.getHeight();
            TranslateAnimation animation;
            if (show) {
                animation = new TranslateAnimation(0, 0, height, 0);
            } else {
                animation = new TranslateAnimation(0, 0, 0, height);
            }

            animation.setFillAfter(false);
            animation.setDuration(1000);
            animation.setInterpolator(new LinearInterpolator());
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    binding.linearBottomWord.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    binding.linearBottomWord.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            binding.linearBottomWord.startAnimation(animation);
        }
//        binding.linearBottom.startAnimation(animation);
    }

    private void ShowOrHide(final boolean show)
    {

        if((show && binding.linearBottom.getVisibility()!= View.VISIBLE) || (!show && binding.linearBottom.getVisibility()==View.VISIBLE)) {
            int height = binding.linearBottom.getHeight();
            TranslateAnimation animation;
            if (show) {
                animation = new TranslateAnimation(0, 0, height, 0);
            } else {
                animation = new TranslateAnimation(0, 0, 0, height);
            }

            animation.setFillAfter(false);
            animation.setDuration(1000);
            animation.setInterpolator(new LinearInterpolator());
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    binding.linearBottom.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    binding.linearBottom.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            binding.linearBottom.startAnimation(animation);
        }
//        binding.linearBottom.startAnimation(animation);
    }

    private void InitImgData()
    {
        Bitmap bitmap = BitmapFactory.decodeFile(wm_img);

        binding.ivTemp.setImageBitmap(bitmap);

        ShowOrHide(true);

    }

    private void chooseVideo() {
        Intent intent = new Intent();
        /* 开启Pictures画面Type设定为image */
        //intent.setType("image/*");
        // intent.setType("audio/*"); //选择音频
        intent.setType("video/*"); //选择视频 （mp4 3gp 是android支持的视频格式）

        // intent.setType("video/*;image/*");//同时选择视频和图片

        /* 使用Intent.ACTION_GET_CONTENT这个Action */
        intent.setAction(Intent.ACTION_GET_CONTENT);
        /* 取得相片后返回本画面 */
        startActivityForResult(intent, 1);


    }

    private void chooseImg()
    {
        Intent intent = new Intent();
        /* 开启Pictures画面Type设定为image */
        intent.setType("image/*");
        // intent.setType("audio/*"); //选择音频
//        intent.setType("video/*"); //选择视频 （mp4 3gp 是android支持的视频格式）

        // intent.setType("video/*;image/*");//同时选择视频和图片

        /* 使用Intent.ACTION_GET_CONTENT这个Action */
        intent.setAction(Intent.ACTION_GET_CONTENT);
        /* 取得相片后返回本画面 */
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 选取图片的返回值
        if(data==null)
        {
            return;
        }
        if (requestCode == 1) {
            //
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                videoInfo.setUri(uri);
                String v_path = getPathByUri4kitkat(this,uri);
                Bitmap bitmap = getVideoThumbnail(v_path);
                File file = new File(v_path);
                float videoSize = file.length()*1.0f/1024/1024;
                videoInfo.setName(file.getName());
                videoInfo.setPath(v_path);
                videoInfo.setSize(videoSize);
                videoInfo.setPreview(bitmap);
                InitVideoData();
            }
        }
        else if(requestCode==2)
        {
            Uri uri = data.getData();
            wm_img = getPathByUri4kitkat(this,uri);
           InitImgData();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * method
     * @param
     * @param
     * @return
     */

    public Bitmap getVideoThumbnail(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime();
        }
        catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }
        finally {
            try {
                retriever.release();
            }
            catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public static String getPathByUri4kitkat(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {// ExternalStorageProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {// DownloadsProvider
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {// MediaProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {// MediaStore
            // (and
            // general)
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {// File
            return uri.getPath();
        }
        return null;
    }
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
