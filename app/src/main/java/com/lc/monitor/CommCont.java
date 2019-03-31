package com.lc.monitor;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.IntDef;
import android.util.Log;

import java.io.File;

public class CommCont {


    private static final String TAG = "APP_LOG";

    //sharedpreference name
    public static final String SHAREDPREFS_NAME = "app_setting";
    public static final String SP_KEY_RECORD_TIME = "pref_record_time";
    public static final String SP_KEY_STORAGE= "pref_storage";
    public static final String SP_KEY_MONITOR_NAME= "pref_monitor_name";
    //db
    public static final String DB_NAME = "monitor_db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_NAME = "record";
    public static final String FILED_ID = "_id";
    public static final String FILED_NAME = "name";
    public static final String FILED_TYPE = "type";
    public static final String FILED_FACE_COUNT = "face_count";
    public static final String FILED_DATE = "date";
    public static final String FILE_PATH = "file_path";

    public static final String PROVIDER = "com.lc.monitor.RecordProvider.record";

    public static final Uri CONTENT_URI = Uri.parse("content://"+PROVIDER+"/"+TABLE_NAME);

    public static final int TYPE_IMAGE = 1 ;
    public static final int TYPE_VIDEO = 2 ;

    public static String DEFAULT_ROOT_DIR = Environment.getExternalStorageDirectory()+"/monitor/";

    private static final String IMAGE_DIR = "image";

    private static final String VIDEO_DIR = "video";




    public static void checkFileDirs(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPREFS_NAME,Context.MODE_PRIVATE);
        String rootPath = sharedPreferences.getString(SP_KEY_STORAGE,DEFAULT_ROOT_DIR);
        File root = new File(rootPath);
        Log.d(TAG,"checkFileDirs->"+rootPath);
        if(!(root.exists() && root.isDirectory())){
            root.mkdirs();
        }
        File imageDir = new File(root,IMAGE_DIR);
        Log.d(TAG,"checkImageFileDirs->"+imageDir.getAbsolutePath());
        if(!(imageDir.exists() && imageDir.isDirectory())){
            imageDir.mkdirs();
        }
        File videoDir = new File(root,VIDEO_DIR);
        Log.d(TAG,"checkVideoFileDirs->"+videoDir.getAbsolutePath());
        if(!(videoDir.exists() && videoDir.isDirectory())){
            videoDir.mkdirs();
        }
    }

    public static String getMediaDir(Context context, int type){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPREFS_NAME,Context.MODE_PRIVATE);
        String rootPath = sharedPreferences.getString(SP_KEY_STORAGE,DEFAULT_ROOT_DIR);
        if(type == TYPE_IMAGE){
            return new File(rootPath,IMAGE_DIR).getAbsolutePath();
        }else if(type == TYPE_VIDEO){
            return new File(rootPath,VIDEO_DIR).getAbsolutePath();
        }
        Log.e(TAG,"unSupport Media Type for App type->"+type);
        return null;
    }



    public static void insertRecord(Context context,int type,String savePath,int faceCount){
        String fileName = savePath.substring(savePath.lastIndexOf(File.separator)+1,savePath.length());
        String date = fileName.substring(0,fileName.lastIndexOf("."));
        ContentValues values = new ContentValues();
        values.put(FILED_NAME,fileName);
        values.put(FILED_DATE,Long.valueOf(date));
        values.put(FILE_PATH,savePath);
        values.put(FILED_TYPE,type);
        values.put(FILED_FACE_COUNT,faceCount);
        context.getContentResolver().insert(CONTENT_URI,values);
    }




    public static long getRecordTime(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPREFS_NAME,Context.MODE_PRIVATE);
        return Long.parseLong(sharedPreferences.getString(SP_KEY_RECORD_TIME,"30"));
    }

}
