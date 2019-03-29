package com.lc.monitor;

import android.net.Uri;
import android.os.Environment;

public class CommCont {

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

    public static final String DEFAULT_DIR = Environment.getExternalStorageDirectory()+"/monitor/";

}
