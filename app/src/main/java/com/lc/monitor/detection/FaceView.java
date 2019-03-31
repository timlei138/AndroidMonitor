package com.lc.monitor.detection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class FaceView extends View {

    private String TAG = getClass().getSimpleName();

    private Paint mPaint;

    private int mRectColor = Color.parseColor("#42ed45");

    private List<RectF> mFaceRectList;

    public FaceView(Context context) {
        this(context,null);
    }

    public FaceView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FaceView(Context context,  AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    public FaceView(Context context,  AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mRectColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(4);
        mFaceRectList = new ArrayList<>();
    }

    public void setFaceRect(List<RectF> faces){
        mFaceRectList.clear();
        if(faces != null || faces.size() > 0){
            mFaceRectList.addAll(faces);
        }
        postInvalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (RectF rectF : mFaceRectList){
            canvas.drawRect(rectF,mPaint);
        }
    }
}
