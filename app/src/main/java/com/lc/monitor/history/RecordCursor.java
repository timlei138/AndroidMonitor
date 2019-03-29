package com.lc.monitor.history;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.util.Log;

import com.lc.monitor.CommCont;
import com.lc.monitor.bean.Record;

import java.util.ArrayList;
import java.util.List;

public class RecordCursor extends CursorWrapper {


    private List<Record> mList = new ArrayList<>();

    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public RecordCursor(Cursor cursor) {
        super(cursor);
        int idIndex = cursor.getColumnIndex(CommCont.FILED_ID);
        int nameIndex = cursor.getColumnIndex(CommCont.FILED_NAME);
        int typeIndex = cursor.getColumnIndex(CommCont.FILED_TYPE);
        int faceCountIndex = cursor.getColumnIndex(CommCont.FILED_FACE_COUNT);
        int dateIndex = cursor.getColumnIndex(CommCont.FILED_DATE);
        int pathIndex = cursor.getColumnIndex(CommCont.FILE_PATH);
        mList.clear();
        Record record;
        cursor.moveToFirst();
        while(cursor.moveToNext()){
            int id = cursor.getInt(idIndex);
            String name = cursor.getString(nameIndex);
            int type = cursor.getInt(typeIndex);
            int faceCount = cursor.getInt(faceCountIndex);
            long date = cursor.getLong(dateIndex);
            String path = cursor.getString(pathIndex);
            record = new Record(id,name,type,faceCount,date,path);
            mList.add(record);
            Log.d("Record:",record.toString());
        }
    }



    public List<Record> getRecordList(){
        return mList;
    }
}
