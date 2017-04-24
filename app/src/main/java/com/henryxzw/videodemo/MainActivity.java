package com.henryxzw.videodemo;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.BitmapCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.henryxzw.videodemo.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Button btn_start,btnChoose;
    private TextView tv_service_msg,tv_file_msg;

    private Timer timer;
    private TimerTask timerTask;

    private ArrayList<String> files,imgs;

    private  String fileTemp="";
    private VideoInfo videoInfo;

    private final String parentPath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"tencent"+File.separator+"MicroMsg"+File.separator;

    private  String x = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"u.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        InitView();
        InitListener();
        InitData();
        if( Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            ArrayList<String> permission = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permission.add(Manifest.permission.READ_EXTERNAL_STORAGE);

            }
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED)
            {
                permission.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (permission.size() > 0) {
                String[] strings = new String[permission.size()];
                permission.toArray(strings);

                ActivityCompat.requestPermissions(this, strings, 1);
            }
        }
    }

    private void InitView()
    {
        btn_start = binding.btnStart;
        btnChoose = binding.btnChoose;
        tv_service_msg = binding.tvServiceMsg;
        tv_file_msg = binding.tvVideoMsg;

        binding.topBar.title.setText("ceshi");
    }

    private void InitListener()
    {
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(timer==null)
                {
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
                                        copyFile(x,file.getAbsolutePath());
                                        files.clear();
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
                    tv_service_msg.setText("服务已经开启,监听文件夹为"+parentPath);
                    btn_start.setText("关闭服务");
                }
                else
                {
                    timerTask.cancel();
                    timer.cancel();
                    timer = null;
                    timerTask = null;
                    tv_service_msg.setText("服务已经关闭");
                    btn_start.setText("开启服务");
                }

            }
        });
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseVideo();

            }
        });
        binding.btnControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ControlActivity.class);
                intent.putExtra("path",videoInfo.getPath());
                startActivityForResult(intent,102);
            }
        });
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


    private void InitData()
    {
        files = new ArrayList<>();
        imgs = new ArrayList<>();
        videoInfo = new VideoInfo();

        tv_file_msg.setText("默认分享路径："+x);

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
    }

    public void InitVideoDataInfo()
    {
        File file = new File(x);
        float videoSize = file.length()*1.0f/1024/1024;

        tv_file_msg.setText(String.format(Locale.CHINA,"路径：%s \r\n大小：%.1fMB",x,videoSize));
        Bitmap bitmap = getVideoThumbnail(x);
        binding.ivPreview.setImageBitmap(bitmap);

        videoInfo.setName(file.getName());
        videoInfo.setPath(x);
        videoInfo.setSize(videoSize);
        videoInfo.setPreview(bitmap);

        if(videoSize>2)
        {
            binding.tvStatus.setText("视频文件超过2M，发送朋友圈可能失败");
            binding.btnControl.setVisibility(View.VISIBLE);
        }
        else
        {
            binding.tvStatus.setText("");
            binding.btnControl.setVisibility(View.GONE);
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
                x = v_path;
                InitVideoDataInfo();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

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
