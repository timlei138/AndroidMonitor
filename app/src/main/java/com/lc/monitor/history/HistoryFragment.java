package com.lc.monitor.history;

import android.content.Intent;
import android.database.Cursor;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.lc.monitor.CommCont;
import com.lc.monitor.R;
import com.lc.monitor.ToolsCallback;
import com.lc.monitor.bean.Record;

import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

public class HistoryFragment extends Fragment implements ToolsCallback, AdapterView.OnItemClickListener {

    private String TAG = getClass().getSimpleName();

    private LoaderManager mLoaderManager;

    private GridView mHistoryLayout;

    private int LOAD_RECORD = 1;

    private HistoryItemAdapter mAdapter;

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
                if(mAdapter!=null){
                    mAdapter.setHistory(records);
                }
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
        mHistoryLayout = view.findViewById(R.id.historyLayout);
        mAdapter = new HistoryItemAdapter(getContext());
        mHistoryLayout.setAdapter(mAdapter);
        mHistoryLayout.setOnItemClickListener(this);
    }

    @Override
    public int getTitleRes() {
        return R.string.title_history;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       Intent intent = new Intent();
       intent.setAction(Intent.ACTION_VIEW);
       Record record = (Record) mAdapter.getItem(position);
       if(record.getType() == CommCont.TYPE_VIDEO)
           intent.setDataAndType(Uri.fromFile(new File(record.getFile())),"video/*");
       else
           intent.setDataAndType(Uri.fromFile(new File(record.getFile())),"image/*");
       startActivity(intent);
    }
}
