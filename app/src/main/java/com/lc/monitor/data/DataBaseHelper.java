package com.lc.monitor.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.lc.monitor.CommCont;

public class DataBaseHelper extends SQLiteOpenHelper {

    private String TAG = getClass().getSimpleName();

    public DataBaseHelper(Context context) {
        super(context, CommCont.DB_NAME, null, CommCont.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG,"onCreate");
        createTable(db);
    }

    private void createTable(SQLiteDatabase db){
        db.execSQL("CREATE TABLE "+CommCont.TABLE_NAME+"("
                +CommCont.FILED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                +CommCont.FILED_NAME + " TEXT NOT NULL,"
                +CommCont.FILED_TYPE + " INTEGER NOT NULL,"
                +CommCont.FILED_FACE_COUNT+" INTEGER NOT NULL,"
                +CommCont.FILED_DATE + " LONG NOT NULL,"
                +CommCont.FILE_PATH + " TEXT NOT NULL )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
