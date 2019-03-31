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

    //默认后置摄像头
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
            Toast.makeText(mContext,"Device not support FaceDetect!",Toast.LENGTH_SHORT).show();
            Log.e(TAG,"Hardware not support face detect");
            return;
        }
        //预览区域
        this.mPreviewSize = previewSize;
        //camera成像区域
        Rect activeArraySizeRect = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        Log.d(TAG,"PreviewSize:"+mPreviewSize.toString()+"Aspect Ratio = "+((float) mPreviewSize.getWidth()/ mPreviewSize.getHeight()));
        Log.d(TAG,"ActiveArraySize"+activeArraySizeRect.toString()+",Aspec Ratio:"+((float)activeArraySizeRect.width() / activeArraySizeRect.height()));
        float scaleWidth = (float)mPreviewSize.getWidth() / activeArraySizeRect.width();
        float scaleHeight = (float)mPreviewSize.getHeight() / activeArraySizeRect.height();
        boolean mirror = (mCameraFacing == CameraCharacteristics.LENS_FACING_FRONT);
        Log.d(TAG,"scaleHeight:"+scaleHeight+",scaleWidth:"+scaleWidth+",mirror:"+mirror);
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
        Log.d(TAG,"find face count->"+(faces!=null ? faces.length:"0"));
        mFaceList.clear();
        for (Face face : faces){
            Rect bounds = face.getBounds();
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
                realRectF = new RectF(rawRectF.left,rawRectF.top - mPreviewSize.getWidth(),rawRectF.right,rawRectF.bottom-mPreviewSize.getWidth());
            }
            mFaceList.add(realRectF);
        }

        if(mCallback != null){
            mCallback.showFace(faces,mFaceList);
        }
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
        void showFace(Face[] faces ,List<RectF> realRectf);
    }

}
