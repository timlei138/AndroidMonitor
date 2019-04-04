package com.lc.monitor.history;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.lc.monitor.CommCont;
import com.lc.monitor.GlideApp;
import com.lc.monitor.R;
import com.lc.monitor.bean.Record;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryItemAdapter extends BaseAdapter{

    private Context mContext;
    private List<Record> mRecordList;
    LayoutInflater mInflater;

    private int width ;
    private int height;

    public HistoryItemAdapter(Context context){
        mContext = context;
        mRecordList = new ArrayList<>();
        mInflater = LayoutInflater.from(context);
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        width = metrics.heightPixels / 3;
    }

    public void setHistory(List<Record> list){
        mRecordList.clear();
        mRecordList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mRecordList.size();
    }

    @Override
    public Object getItem(int position) {
        return mRecordList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.layout_history_item,null);
            viewHolder = new ViewHolder();
            viewHolder.videoBtn = convertView.findViewById(R.id.video);
            viewHolder.contentView = convertView.findViewById(R.id.image);
            viewHolder.nameTv = convertView.findViewById(R.id.name);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Record record = mRecordList.get(position);

        if(record.getType() == CommCont.TYPE_VIDEO){
            viewHolder.videoBtn.setVisibility(View.VISIBLE);
        }else{
            viewHolder.videoBtn.setVisibility(View.GONE);
        }
        GlideApp.with(mContext).load(record.getFile()).override(width,width).centerCrop().into(viewHolder.contentView);
        viewHolder.nameTv.setText(getSimpleName(record.getName()));
        return convertView;
    }


    private String getSimpleName(String name){
        Log.d("demo","name:"+name);
        String tmp = name.substring(0,name.lastIndexOf("."));
        String date = tmp.substring(tmp.lastIndexOf("-")+1,tmp.length());
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddmmss");
        String time = sf.format(new Date(Long.valueOf(date)));
        return tmp.substring(0,tmp.lastIndexOf("-")) + time;
    }

    class ViewHolder{
        private ImageView contentView;
        private TextView nameTv;
        private View videoBtn;
    }
}
