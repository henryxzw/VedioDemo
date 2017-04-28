package com.henryxzw.videodemo.main;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.henryxzw.videodemo.ActivityVideoControlBinding;
import com.henryxzw.videodemo.R;
import com.henryxzw.videodemo.VideoInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import videocompress.util.Worker;
import videocompress.video.VideoCompressListener;
import videocompress.video.VideoCompressor;

/**
 * Created by Administrator on 2017/4/24.
 */

public class VideoControlActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private ActivityVideoControlBinding binding;

    private VideoInfo videoInfo;
    private String originPath ="";
    private float duration=0;

    private final String parentPath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"tencent"+File.separator+"MicroMsg"+File.separator;
    private  String fileTemp="";
    private ArrayList<String> files,imgs;

    private Timer timer;
    private TimerTask timerTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_control);
        binding.includeTitle.title.setText("视频压缩");
        binding.includeTitle.toolBar.setNavigationIcon(R.mipmap.arrow_left);

        InitData();
        InitListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
       ReleaseMedia();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void InitData()
    {
        videoInfo = new VideoInfo();
        files = new ArrayList<>();
        imgs = new ArrayList<>();
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
                    ReleaseMedia();
                    final ProgressDialog progressDialog = new ProgressDialog(VideoControlActivity.this);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setMessage("压缩中...");
                    progressDialog.setCancelable(true);
                    progressDialog.setMax(100);
                    progressDialog.show();

                    String min = "32",ac="32";
                    File file = new File(videoInfo.getPath());
                    float videoSize = file.length()*1.0f/1024/1024;
                    if(videoSize<1)
                    {
                        videoSize = videoSize*0.9f;
                    }
                    else
                    {
                        videoSize = 0.9f;
                    }
                    if(duration<120)
                    {
                        min = ""+(int)( videoSize*1024*8/duration-32);
                    }
                    else
                    {
                        min = ""+(int)( videoSize*1024*8/duration-20);
                        ac = "20";
                    }

                    VideoCompressor.compress(VideoControlActivity.this, videoInfo.getPath(), new VideoCompressListener() {
                        @Override
                        public void onSuccess(final String outputFile, String filename, long duration) {
                            Worker.postMain(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                    Toast.makeText(VideoControlActivity.this,"压缩成功",Toast.LENGTH_LONG).show();
                                    videoInfo.setPath(outputFile);
                                    InitVideoData();
                                }
                            });

                        }

                        @Override
                        public void onFail(String reason) {
                            Log.e("tab",""+reason);
                        }

                        @Override
                        public void onProgress(final int progress) {

                            Worker.postMain(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.setMessage("压缩中..."+progress+"%");

                                }
                            });

                        }
                    },new String().format(Locale.CHINA," -strict -2 -vcodec libx264 -vb %sK -ab %sK ",min,ac));
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


        binding.linearSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty( videoInfo.getPath()))
                {
                    Toast.makeText(VideoControlActivity.this,"请先选择视频文件",Toast.LENGTH_LONG).show();
                    return;
                }
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/DCIM/vd/");
                if(!file.exists())
                {
                    file.mkdirs();
                }
                copyFile(videoInfo.getPath(),Environment.getExternalStorageDirectory().getAbsolutePath()+"/DCMI/vd/"+videoInfo.getName());
                Toast.makeText(VideoControlActivity.this,"保存成功",Toast.LENGTH_LONG).show();
            }
        });
        binding.linearShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty( videoInfo.getPath()))
                {
                    Toast.makeText(VideoControlActivity.this,"请先选择视频文件",Toast.LENGTH_LONG).show();
                    return;
                }


                if(timer==null)
                {
                    binding.tvWechat.setText("点击关闭监听");
                    timer = new Timer();
                    timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            ArrayList<String> fileList = GetFileList();
                            ArrayList<String> imgsList = GetImgsList();

                            if(files.size() ==0)
                            {
                                files.addAll(fileList);
                            }
                            else if(files.size()<fileList.size())
                            {
                                for(int i=0;i<fileList.size();i++)
                                {
                                    if(!files.contains(fileList.get(i)))
                                    {
                                        Log.e("tg",fileList.get(i));
                                        File file = new File(fileTemp+File.separator+fileList.get(i));
                                        copyFile(videoInfo.getPath(),file.getAbsolutePath());
                                        files.clear();
                                        Worker.postMain(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(VideoControlActivity.this,"替换成功",Toast.LENGTH_LONG).show();
                                            }
                                        });
                                        break;

                                    }
                                }
                            }
                            else
                            {
                                files.clear();
                                files.addAll( fileList);
                            }

                            if(imgs.size() ==0)
                            {
                                imgs.addAll(imgsList);
                            }
                            else if(imgs.size()<imgsList.size())
                            {
                                for(int i=0;i<imgsList.size();i++)
                                {
                                    if(!imgs.contains(imgsList.get(i)))
                                    {
                                        Log.e("tg",imgsList.get(i));
                                        try
                                        {
                                            FileOutputStream fo = new FileOutputStream(new File(fileTemp+File.separator+imgsList.get(i)));
                                            videoInfo.getPreview().compress(Bitmap.CompressFormat.JPEG,100,fo);
//                                            bitmap.recycle();
                                            fo.flush();
                                            fo.close();
                                            imgs.clear();
                                        }catch (Exception ex)
                                        {}

                                        break;

                                    }
                                }
                            }
                            else
                            {
                                imgs.clear();
                                imgs.addAll( imgsList);
                            }

                        }
                    };
                    timer.schedule(timerTask,0,1000);
                    if(isWeixinAvilible(VideoControlActivity.this)) {
                        Intent intent = new Intent();
                        ComponentName cmp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
                        intent.setAction(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setComponent(cmp);
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(VideoControlActivity.this,"软件无法检测微信，如已经安装，请手动打开",Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    timerTask.cancel();
                    timer.cancel();
                    timer = null;
                    timerTask = null;
                    binding.tvWechat.setText("转朋友圈小视频");
                }


            }
        });
    }

    public static boolean isWeixinAvilible(Context context) {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 复制单个文件
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        }
        catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }

    }

    public ArrayList<String> GetFileList()
    {
        if(TextUtils.isEmpty(fileTemp)) {
            File file = new File(parentPath);
            String[] names = file.list();
            for (int i = 0; i < names.length; i++) {
                if (names[i].length() == 32) {
                    fileTemp = parentPath + File.separator + names[i] + File.separator + "video";
                    break;
                }
            }
        }
        File file1 = new File(fileTemp);
        String[] fs = file1.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if(name.lastIndexOf("mp4")>0)
                {
                    return true;
                }
                return false;
            }
        });
        ArrayList<String> fileList = new ArrayList<>();
        for(int i=0;i<fs.length;i++)
        {
            fileList.add(fs[i]);
        }
        return fileList;
    }

    public ArrayList<String> GetImgsList()
    {
        File file1 = new File(fileTemp);
        String[] fsImgs = file1.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if(name.lastIndexOf("jpg")>0)
                {
                    return true;
                }
                return false;
            }
        });
        ArrayList<String> fileList = new ArrayList<>();
        for(int i=0;i<fsImgs.length;i++)
        {
            fileList.add(fsImgs[i]);
        }
        return fileList;
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
        ReleaseMedia();


    }
    public void ReleaseMedia()
    {
        if(mediaPlayer!=null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void InitVideoData()
    {
        File file = new File(videoInfo.getPath());
        float videoSize = file.length()*1.0f/1024/1024;
        binding.tvVideoSize.setText(new String().format(Locale.CHINA,"%.2fMB",videoSize));

        int width = binding.relativeBg.getWidth() ;
        int height =binding.relativeBg.getHeight();
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) binding.surfaceView.getLayoutParams();


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
        binding.surfaceView.setLayoutParams(lp);

        Play();

    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mediaPlayer!=null)
        {
            if(mediaPlayer.isPlaying())
            {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if(timer!=null)
        {
            timerTask.cancel();
            timer.cancel();
            timerTask = null;
            timer= null;
        }
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
                originPath = v_path;
                binding.tvVideoOriginSize.setText(new String().format(Locale.CHINA,"(原始文件：%.2fMB)",videoInfo.getSize()));

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
            duration = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))/1000.f;
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


    private MediaPlayer mediaPlayer;
    public void Play()
    {
        try {

            if(mediaPlayer==null) {
                mediaPlayer = new MediaPlayer();
            }
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            mediaPlayer.setDataSource(this, Uri.parse( videoInfo.getPath()));
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.setDisplay(binding.surfaceView.getHolder());
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

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
