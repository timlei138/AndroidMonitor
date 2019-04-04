package com.lc.monitor;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.lc.monitor.data.DataBaseHelper;

import java.util.HashMap;
import java.util.Map;

public class RecordProvider extends ContentProvider {

    private DataBaseHelper mDataBaseHelper;

    private static final int RECORDS = 1;
    private static final int RECORD = 2;

    private static UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(CommCont.CONTENT_URI.getAuthority(),CommCont.TABLE_NAME,RECORDS);
        uriMatcher.addURI(CommCont.CONTENT_URI.getAuthority(),CommCont.TABLE_NAME+"/#",RECORD);
    }


    @Override
    public boolean onCreate() {
        mDataBaseHelper = new DataBaseHelper(getContext());
        return mDataBaseHelper != null;
    }


    @Override
    public Cursor query( Uri uri,  String[] projection,  String selection,  String[] selectionArgs,  String sortOrder) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(CommCont.TABLE_NAME);

        switch (uriMatcher.match(uri)){
            case RECORDS:
                Map<String,String> projects = new HashMap<>();
                builder.setProjectionMap(projects);
                break;
            case RECORD:
                builder.appendWhere(CommCont.FILED_ID+"="+uri.getPathSegments().get(1));
                break;
                default:
                    throw new IllegalArgumentException("Unknown URI:"+uri);

        }

        if(TextUtils.isEmpty(sortOrder)){
            sortOrder = CommCont.FILED_DATE;
        }

        Cursor cursor = builder.query(mDataBaseHelper.getReadableDatabase(),projection,selection,selectionArgs,null,null,sortOrder);

        return cursor;
    }


    @Override
    public String getType( Uri uri) {
        switch (uriMatcher.match(uri)){
            case RECORD:
                return "vnd.android.cursor.dir/vnd.lc.record";
            case RECORDS:
                return "vnd.android.cursor.dir/vnd.lc.records";

                default:
                    throw new IllegalArgumentException("Unsupported Uri:"+uri);
        }

    }


    @Override
    public Uri insert( Uri uri,  ContentValues values) {
        Log.d("Provider","insert to db->"+values.toString());
        SQLiteDatabase db =  mDataBaseHelper.getWritableDatabase();
        long rowId = db.insert(CommCont.TABLE_NAME,"",values);
        if(rowId > 0){
            Uri _uri = ContentUris.withAppendedId(CommCont.CONTENT_URI,rowId);
            getContext().getContentResolver().notifyChange(_uri,null);
            return _uri;
        }
        throw new SQLException("Faild to add a record into "+uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDataBaseHelper.getWritableDatabase();
        int count = 0;
        switch (uriMatcher.match(uri)){
            case RECORDS:
                count = db.delete(CommCont.TABLE_NAME,selection,selectionArgs);
                break;
            case RECORD:
                String id = uri.getPathSegments().get(1);
                count = db.delete(CommCont.TABLE_NAME,CommCont.FILED_ID
                        +"="+id+(TextUtils.isEmpty(selection)?" AND ("+selection+")":""),selectionArgs);
                break;
                default:
                    throw new IllegalArgumentException("Unknown URI:"+uri);

        }
        getContext().getContentResolver().notifyChange(uri,null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values,String selection,String[] selectionArgs) {
        return 0;
    }



}
