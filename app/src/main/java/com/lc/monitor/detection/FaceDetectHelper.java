package com.lc.monitor.detection;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.telecom.Call;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FaceDetectHelper {

    private String TAG = getClass().getSimpleName();

    private int maxDetectFaceCount;

    private int mDetectFaceMode;

    private Context mContext;

    private boolean openDectect = true;

    private Matrix mFaceDetectMatrix;

    private List<RectF> mFaceList;

    private Size mPreviewSize;

    private int mCameraFacing = CameraCharacteristics.LENS_FACING_BACK;

    public FaceDetectHelper(Context context){
        mContext = context;
        mFaceList = new ArrayList<>();
        mFaceDetectMatrix = new Matrix();
    }

    public void init(CameraCharacteristics cameraCharacteristics, Size previewSize,int cameraSensorOrientation,int displayOrientation){
        //同时检测的人脸数量
        maxDetectFaceCount = cameraCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT);
        //人脸检测模式
        int[] faceModes = cameraCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES);
        Log.d(TAG,"maxFaceCount:"+maxDetectFaceCount+",previewSize:"+previewSize.toString());
        Log.d(TAG,"displayOrientation:"+displayOrientation+",cameraSensor:"+cameraSensorOrientation);
        for (int mode : faceModes){
            Log.d(TAG,"FaceMode:"+mode);
            if(mode == CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL ||
                    mode == CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE){
                mDetectFaceMode = CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL;
            }else{
                mDetectFaceMode = CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF;
            }
        }
        if(mDetectFaceMode == CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF){
            Toast.makeText(mContext,"不支持人脸识别",Toast.LENGTH_SHORT).show();
            Log.e(TAG,"Hardware not support face detect");
            return;
        }
        //成想区域
        this.mPreviewSize = previewSize;
        Rect activeArraySizeRect = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        float scaleWidth = mPreviewSize.getWidth() / activeArraySizeRect.width();
        float scaleHeight = mPreviewSize.getHeight() / activeArraySizeRect.height();
        boolean mirror = (mCameraFacing == CameraCharacteristics.LENS_FACING_FRONT);
        Log.d(TAG,"activeSize"+activeArraySizeRect.toString()+",scaleHeight:"+scaleHeight+",scaleWidth:"+scaleWidth);
        mFaceDetectMatrix.setRotate(cameraSensorOrientation);

        if(mirror){
            mFaceDetectMatrix.postScale(-scaleWidth,scaleHeight);
        }else{
            mFaceDetectMatrix.postScale(scaleWidth,scaleHeight);
        }
        if(exchangeWidthAndHeight(displayOrientation,cameraSensorOrientation)){
            mFaceDetectMatrix.postTranslate(mPreviewSize.getHeight(),mPreviewSize.getWidth());
        }
    }

    public boolean canFaceDetect(){
        return mDetectFaceMode != CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF;
    }

    public CameraCaptureSession.CaptureCallback getCaptureCallback() {
        return captureCallback;
    }

    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

            if(openDectect && mDetectFaceMode != CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF){
                handleFace(result);
            }
        }
    };

    private void handleFace(TotalCaptureResult result ){
        Face[] faces = result.get(CaptureResult.STATISTICS_FACES);
        mFaceList.clear();
        for (Face face : faces){
            Rect bounds = face.getBounds();
            Log.d(TAG,"bounds:"+bounds.toString());
            int left = bounds.left;
            int right = bounds.right;
            int top = bounds.top;
            int bottom = bounds.bottom;

            RectF rawRectF = new RectF(left,top,right,bottom);

            mFaceDetectMatrix.mapRect(rawRectF);

            RectF realRectF;

            if(mCameraFacing == CaptureRequest.LENS_FACING_FRONT){
                realRectF = rawRectF;
            }else{
                realRectF = new RectF(rawRectF.left,rawRectF.top - mPreviewSize.getWidth(),rawRectF.right,rawRectF.bottom-mPreviewSize.getHeight());
            }
            mFaceList.add(realRectF);

            Log.d(TAG,"faceRect:"+realRectF.toString());

        }

        if(mCallback != null && mFaceList.size() > 0){
            mCallback.showFace(faces,mFaceList);
        }

        Log.d(TAG,"length:"+faces.length+",size:"+mFaceList.size());
    }

    /**
     * 根据屏幕方向和相机方向判断是否需要对宽高进行交换
     * @param displayRotation
     * @param sensorOrientation
     * @return
     */
    public boolean exchangeWidthAndHeight(int displayRotation,int sensorOrientation){
        boolean exchange = false;
        switch (displayRotation){
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                if(sensorOrientation == 90 || sensorOrientation == 270){
                    exchange = true;
                }
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                if(sensorOrientation == 0 || sensorOrientation == 180){
                    exchange = true;
                }
                 break;

        }
        return exchange;
    }


    public void tackPicture(){
        //create
    }


    private Callback mCallback;

    public void setCallback(Callback callback){
        this.mCallback = callback;
    }

    public interface Callback{
        void tackPictureSuccess();
        void showFace(Face[] faces ,List<RectF> realRectf);
    }

    public Size getBestSize(int targetWidth, int targetHeight,int maxWidth,int maxHeight, List<Size> sizeList){
        List<Size> bigEnough = new ArrayList();  //比指定宽高大的Size列表
        List<Size> notBigEnough = new ArrayList(); //比指定宽高小的Size列表
        for (Size size : sizeList) {
            //宽<=最大宽度  &&  高<=最大高度  &&  宽高比 == 目标值宽高比
            if (size.getWidth() <= maxWidth && size.getHeight() <= maxHeight
                    && size.getWidth() == size.getHeight() * targetWidth / targetHeight) {
                if (size.getWidth() >= targetWidth && size.getHeight() >= targetHeight)
                    bigEnough.add(size);
                else
                    notBigEnough.add(size);
            }
            Log.d(TAG,"系统支持的尺寸: ${size.width} * ${size.height} ,  比例 ：${size.width.toFloat() / size.height}");
        }

        Log.d(TAG,"最大尺寸 ：$maxWidth * $maxHeight, 比例 ：${targetWidth.toFloat() / targetHeight}");
        Log.d(TAG,"目标尺寸 ：$targetWidth * $targetHeight, 比例 ：${targetWidth.toFloat() / targetHeight}");

        //选择bigEnough中最小的值  或 notBigEnough中最大的值

        if(bigEnough.size() > 0){
            Collections.min(bigEnough,new MonitorFragment.CompareSizesByArea());
            return bigEnough.get(0);
        }else if(notBigEnough.size() > 0){
            Collections.max(notBigEnough,new MonitorFragment.CompareSizesByArea());
            return notBigEnough.get(0);
        }else{
            return sizeList.get(0);
        }
    }
}
