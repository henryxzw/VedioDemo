package videocompress.video;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.netcompss.ffmpeg4android.CommandValidationException;
import com.netcompss.ffmpeg4android.GeneralUtils;
import com.netcompss.ffmpeg4android.Prefs;
import com.netcompss.ffmpeg4android.ProgressCalculator;
import com.netcompss.loader.LoadJNI;

import java.io.File;

import videocompress.util.AppUtil;
import videocompress.util.SGLog;
import videocompress.util.VideoUtil;

/**
 * Created by caizhiming on 2016/11/8.
 * 视频压缩器
 */

public class VideoCompressor {
    private Context mContext;
//    private static String mStrCmd = " -strict -2 -vcodec libx264 -preset ultrafast -acodec aac -ar 44100 -ac 1 -b:a 32k -s 480x240 -aspect 2:1 -r 24 ";
    private static String mStrCmd = " -strict -2 -vcodec libx264 -vb 32K -ab 32K ";
    private static String mStrCmdPre = "ffmpeg -y -i ";
    private static String mOutputFile = AppUtil.getAppDir() + "/video_compress.mp4";



    private static String workFolder = null;
    private static String demoVideoFolder = null;
    private static String vkLogPath = null;
    private static LoadJNI vk;
    private static boolean commandValidationFailedFlag = false;

    private VideoCompressor(Context context){
        mContext = context;
        init(context);
    }
    private static void init(Context context){
        demoVideoFolder = AppUtil.getAppDir() + "/videokit/";

        workFolder = context.getApplicationContext().getFilesDir() + "/";
        vkLogPath = workFolder + "vk.log";

        GeneralUtils.copyLicenseFromAssetsToSDIfNeeded(context, workFolder);
        GeneralUtils.copyDemoVideoFromAssetsToSDIfNeeded(context, demoVideoFolder);
        int rc = GeneralUtils.isLicenseValid(context.getApplicationContext(), workFolder);
        SGLog.e("License check RC: " + rc);
    }

    /**
     * 视频压缩工具方法
     * @param context
     * @param inputFile
     * @param listener
     */
    public static void compress(final Context context, final String inputFile, final VideoCompressListener listener, final String cmd){
        init(context);
        new Thread() {
            public void run() {
                try {
                    if(TextUtils.isEmpty(cmd)) {
                        compressByFFmpeg(context, inputFile, listener);
                    }
                    else
                    {
                       compressByFFmpegNew(context,inputFile,listener,cmd);
                    }
                } catch(Exception e) {
                    SGLog.e("compress exception:"+e.getMessage());
                }
            }
        }.start();
        getVideoDuration(inputFile);
        // Progress update thread
        new Thread() {
            ProgressCalculator pc = new ProgressCalculator(vkLogPath);
            public void run() {
                SGLog.e("Progress update started");
                int progress = -1;
                try {
                    while (true) {
                        sleep(50);
                        progress = pc.calcProgress();
                        if (progress != 0 && progress <100) {
                            //caizhiming
                            //todo  update progress;
                            SGLog.e("progress="+progress);
                            listener.onProgress(progress);
                        }
                        else if (progress >= 100) {
                            // compress finish;
//                            listener.onProgress(100);
                        }
                    }
                } catch(Exception e) {
                    SGLog.e("threadmessage:"+e.getMessage());
                }
            }
        }.start();
    }

    public static void InfoByFFmped(Context context, String inputFile,VideoCompressListener listener)
    {
        init(context);
        SGLog.e("runTranscodingUsingLoader started...");
        vk = new LoadJNI();
        String newFilename = null;
        try {
            newFilename = VideoUtil.getFileMD5(new File(inputFile)) + ".mp4";

            String cmdStr = "ffmpeg -i "+inputFile+"";
            vk.run(GeneralUtils.utilConvertToComplex(cmdStr), workFolder, context.getApplicationContext());

            Log.i(Prefs.TAG, "vk.run finished.");
        } catch (CommandValidationException e) {
            SGLog.e("vk run exeption."+ e);
            commandValidationFailedFlag = true;
        } catch (Throwable e) {
            SGLog.e("vk run exeption."+e);
        }
        // finished Toast
        String rc = null;
        if (commandValidationFailedFlag) {
            rc = "Command Vaidation Failed";
        }
        else {
            rc = GeneralUtils.getReturnCodeFromLog(vkLogPath);
        }
        final String status = rc;
        SGLog.e("compress rc="+rc);
        if (status.equals("Transcoding Status: Failed")) {
            String strFailInfo = "Check: " + vkLogPath + " for more information.";
            listener.onFail(strFailInfo);
        }else{
            listener.onSuccess(mOutputFile,newFilename, getVideoDuration(inputFile));
        }
    }

    private static void compressByFFmpeg(Context context,String inputFile,final VideoCompressListener listener) {
        SGLog.e("runTranscodingUsingLoader started...");
        vk = new LoadJNI();
        String newFilename = null;
        try {
            newFilename = VideoUtil.getFileMD5(new File(inputFile)) + ".mp4";
            mOutputFile = AppUtil.getAppDir() +"/" + newFilename;
            String cmdStr = mStrCmdPre + inputFile + mStrCmd + mOutputFile;
            vk.run(GeneralUtils.utilConvertToComplex(cmdStr), workFolder, context.getApplicationContext());

            Log.i(Prefs.TAG, "vk.run finished.");
            // copying vk.log (internal native log) to the videokit folder
            GeneralUtils.copyFileToFolder(vkLogPath, demoVideoFolder);
        } catch (CommandValidationException e) {
            SGLog.e("vk run exeption."+ e);
            commandValidationFailedFlag = true;
        } catch (Throwable e) {
            SGLog.e("vk run exeption."+e);
        }
        // finished Toast
        String rc = null;
        if (commandValidationFailedFlag) {
            rc = "Command Vaidation Failed";
        }
        else {
            rc = GeneralUtils.getReturnCodeFromLog(vkLogPath);
        }
        final String status = rc;
        SGLog.e("compress rc="+rc);
        if (status.equals("Transcoding Status: Failed")) {
            String strFailInfo = "Check: " + vkLogPath + " for more information.";
            listener.onFail(strFailInfo);
        }else{
            listener.onSuccess(mOutputFile,newFilename, getVideoDuration(inputFile));
        }
    }

    private static void compressByFFmpegNew(Context context,String inputFile,final VideoCompressListener listener,String cmd) {
        SGLog.e("runTranscodingUsingLoader started...");
        vk = new LoadJNI();
        String newFilename = null;
        try {
            newFilename = VideoUtil.getFileMD5(new File(inputFile)) + ".mp4";
            mOutputFile = AppUtil.getAppDir() +"/" + newFilename;
            String cmdStr = mStrCmdPre + inputFile + cmd + mOutputFile;
            vk.run(GeneralUtils.utilConvertToComplex(cmdStr), workFolder, context.getApplicationContext());

            Log.i(Prefs.TAG, "vk.run finished.");
            // copying vk.log (internal native log) to the videokit folder
            GeneralUtils.copyFileToFolder(vkLogPath, demoVideoFolder);
        } catch (CommandValidationException e) {
            SGLog.e("vk run exeption."+ e);
            commandValidationFailedFlag = true;
        } catch (Throwable e) {
            SGLog.e("vk run exeption."+e);
        }
        // finished Toast
        String rc = null;
        if (commandValidationFailedFlag) {
            rc = "Command Vaidation Failed";
        }
        else {
            rc = GeneralUtils.getReturnCodeFromLog(vkLogPath);
        }
        final String status = rc;
        SGLog.e("compress rc="+rc);
        if (status.equals("Transcoding Status: Failed")) {
            String strFailInfo = "Check: " + vkLogPath + " for more information.";
            listener.onFail(strFailInfo);
        }else{
            listener.onSuccess(mOutputFile,newFilename, getVideoDuration(inputFile));
        }
    }

    private static int getVideoDuration(String path){
        if(mVideoDuration <= 0){
            mVideoDuration = VideoUtil.getVideoDuration(path);
        }
        return mVideoDuration;
    }
    private static int mVideoDuration;
}
