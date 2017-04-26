package com.henryxzw.videodemo.main;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.henryxzw.videodemo.ActivityVideoControlBinding;
import com.henryxzw.videodemo.R;
import com.henryxzw.videodemo.VideoInfo;

import java.io.File;
import java.util.Locale;

import videocompress.video.VideoCompressListener;
import videocompress.video.VideoCompressor;

/**
 * Created by Administrator on 2017/4/24.
 */

public class VideoControlActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private ActivityVideoControlBinding binding;

    private VideoInfo videoInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_control);
        binding.includeTitle.title.setText("视频压缩");
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

        binding.surfaceView.getHolder().addCallback(this);

        binding.includeTitle.toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.btnSelf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty( videoInfo.getPath()))
                {
                    Toast.makeText(VideoControlActivity.this,"请先选择视频文件",Toast.LENGTH_LONG).show();
                }
                else{
                    binding.viewSwitcher.showNext();
                }

            }
        });
        binding.btnAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty( videoInfo.getPath()))
                {
                    Toast.makeText(VideoControlActivity.this,"请先选择视频文件",Toast.LENGTH_LONG).show();
                }
                else{
//                    final ProgressDialog progressDialog = new ProgressDialog(VideoControlActivity.this);
//                    progressDialog.setMax(100);
//                    progressDialog.show();

                    VideoCompressor.compress(VideoControlActivity.this, videoInfo.getPath(), new VideoCompressListener() {
                        @Override
                        public void onSuccess(String outputFile, String filename, long duration) {
                            Log.e("tab",""+outputFile);
                        }

                        @Override
                        public void onFail(String reason) {
                            Log.e("tab",""+reason);
                        }

                        @Override
                        public void onProgress(int progress) {
                            Log.e("tab",""+progress);
//                            progressDialog.setProgress(progress);
                        }
                    });
                }
            }
        });
        binding.tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.viewSwitcher.showPrevious();
            }
        });

        binding.tvChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseVideo();
            }
        });
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

    private void InitVideoData()
    {

        binding.tvVideoOriginSize.setText(new String().format(Locale.CHINA,"(原始文件：%.2fMB)",videoInfo.getSize()));
        binding.tvVideoSize.setText(new String().format(Locale.CHINA,"%.2fMB",videoInfo.getSize()));

        binding.linearStatus.setVisibility(View.GONE);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) binding.surfaceView.getLayoutParams();

        lp.width = videoInfo.getPreview().getWidth();
        lp.height =videoInfo.getPreview().getHeight();
        binding.surfaceView.setLayoutParams(lp);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 选取图片的返回值
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


    public void Play()
    {
        try {


            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDisplay(binding.surfaceView.getHolder());
            mediaPlayer.setDataSource(this, Uri.parse( videoInfo.getPath()));
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
