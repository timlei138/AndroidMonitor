package com.lc.monitor.history;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lc.monitor.CommCont;
import com.lc.monitor.R;
import com.lc.monitor.ToolsCallback;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

public class HistoryFragment extends Fragment implements ToolsCallback {

    private String TAG = getClass().getSimpleName();

    private LoaderManager mLoaderManager;

    private int LOAD_RECORD = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoaderManager = LoaderManager.getInstance(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mLoaderManager.initLoader(LOAD_RECORD, null, loadAllRecord);
    }

    private LoaderManager.LoaderCallbacks<Cursor> loadAllRecord = new LoaderManager.LoaderCallbacks<Cursor>() {
        @NonNull
        @Override
        public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
            CursorLoader recordLoader = new CursorLoader(getContext()){
                @Override
                public Cursor loadInBackground() {
                    Log.d(TAG,"loadInBackground");
                    Cursor cursor =  super.loadInBackground();
                    if(cursor == null){
                        return null;
                    }
                    RecordCursor recordCursor = new RecordCursor(cursor);
                    return recordCursor;
                }

            };
            recordLoader.setUri(CommCont.CONTENT_URI);
            return recordLoader;
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {

            if(cursor instanceof RecordCursor){
                List records = ((RecordCursor)cursor).getRecordList();

                Log.d(TAG,"record size:"+records.size());
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history,null);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public int getTitleRes() {
        return R.string.title_history;
    }
}
