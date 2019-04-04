package com.lc.monitor.detection;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.CameraProfile;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.lc.monitor.CommCont;
import com.lc.monitor.R;
import com.lc.monitor.ToolsCallback;
import com.lc.monitor.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MonitorFragment extends Fragment implements ToolsCallback, View.OnClickListener {


    private final static String TAG = "MonitorFragment";

    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    private static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    private AutoFitTextureView mTextureView;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mPreviewSession;

    /**
     * The {@link android.util.Size} of camera preview.
     */
    private Size mPreviewSize;

    /**
     * The {@link android.util.Size} of video recording.
     */
    private Size mVideoSize;

    /**
     * MediaRecorder
     */
    private MediaRecorder mMediaRecorder;

    /**
     * Whether the app is recording video now
     */
    private boolean mIsRecordingVideo;

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private Integer mSensorOrientation;
    private String mNextVideoAbsolutePath;
    private CaptureRequest.Builder mPreviewBuilder;

    private TextView mMonitorBtn;

    private FaceView mFaceView;
    private FaceDetectHelper mFaceDetechHelper;
    private ImageReader mImageReader;

    private int mDisplayRotation; //手机方向

    private static boolean startWatch = false;

    private int tackPictureCount  = 0;

    private boolean isTacking = false;

    private final int MSG_TACK_PICTURE = 10;
    private final int MSG_START_RECORD = 11;
    private final int MSG_STOP_RECORD = 12;
    private final int MSG_START_PREVIEW = 13;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_monitor,null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextureView = view.findViewById(R.id.preview);
        mMonitorBtn = view.findViewById(R.id.monitor_btn);
        mMonitorBtn.setOnClickListener(this);
        mFaceView = view.findViewById(R.id.faceview);
        mFaceDetechHelper = new FaceDetectHelper(getContext());
        mFaceDetechHelper.setCallback(faceCallback);
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        mDisplayRotation = wm.getDefaultDisplay().getRotation();

    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int faceCount = 0;

    private FaceDetectHelper.Callback faceCallback = new FaceDetectHelper.Callback() {
        @Override
        public void showFace(Face[] faces, final List<RectF> realRectf) {
            faceCount = realRectf != null ? realRectf.size() : 0;
            if(startWatch && faceCount > 0){
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mFaceView.setFaceRect(realRectf);
                    }
                });
                if(!isTacking)
                    //tackPicture();
                    switchHandle.sendEmptyMessageDelayed(MSG_TACK_PICTURE,0);
            }
        }
    };

    @Override
    public void onClick(View v) {
        if(!startWatch){
            CountDownDialog countDownDialog = new CountDownDialog(new CountDownDialog.TickCallback() {
                @Override
                public void onFinishTick() {
                    mMonitorBtn.setEnabled(true);
                    mMonitorBtn.setText(R.string.monitor_progress);
                }
            });

            countDownDialog.showNow(getFragmentManager(),"CountDownDialog");
        }else{
            startWatch = false;
            mMonitorBtn.setEnabled(true);
            mMonitorBtn.setText(R.string.monitor_start);
        }

    }

    private List<File> savedImageList = new ArrayList<>();

    private ImageReader.OnImageAvailableListener mImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            long starttime = System.currentTimeMillis();
            Image image = reader.acquireNextImage();
            ByteBuffer imageBuff = image.getPlanes()[0].getBuffer();
            byte[] data = new byte[imageBuff.remaining()];
            imageBuff.get(data);
            image.close();
            String savePath = Utils.SaveImage(getContext(),data);
            savedImageList.add(new File(savePath));
            CommCont.insertRecord(getContext(),CommCont.TYPE_IMAGE,savePath,faceCount);
            isTacking = false;
            tackPictureCount++;
            Log.d(TAG,"saveImage:"+savePath+",use time:"+((float)System.currentTimeMillis() - starttime)/1000);
            Log.d(TAG,"tackPictureCount:"+tackPictureCount);
            if(tackPictureCount < 2){
                switchHandle.sendEmptyMessageDelayed(MSG_TACK_PICTURE,1000);
            }else{
                switchHandle.sendEmptyMessageDelayed(MSG_START_RECORD,1000);
            }
        }
    };


    private void alertEvent(){
        if(CommCont.isWatchEmailEnable(getContext())){
            Utils.sendEmail(getContext(),savedImageList);
        }
        if(CommCont.isWatchPhoneEnable(getContext())){
            Utils.sendSms(getContext());
        }
    }



    Handler switchHandle = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG,"switch Handled msg what:"+msg.what);
            switch (msg.what){
                case MSG_START_RECORD:
                    startWatch = false;
                    tackPictureCount = 0;
                    mFaceView.setFaceRect(null);
                    alertEvent();
                    startRecordingVideo();
                    long counttime = CommCont.getRecordTime(getContext());
                    Log.d(TAG,"record video count down time:"+counttime);
                    switchHandle.sendEmptyMessageDelayed(MSG_STOP_RECORD,counttime * 1000);
                    break;
                case MSG_STOP_RECORD:
                    stopRecordingVideo();
                    if(mPreviewSession != null){
                        mPreviewSession.close();
                        mPreviewSession = null;
                    }
                    switchHandle.sendEmptyMessageDelayed(MSG_START_PREVIEW,500);
                    break;
                case MSG_TACK_PICTURE:
                    tackPicture();
                    break;
                case MSG_START_PREVIEW:
                    startPreview();
                    startWatch = true;
                    break;
            }


        }
    };

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview();
            mCameraOpenCloseLock.release();
            if (null != mTextureView) {
                configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }

    };

    private Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio,float facedetectratio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            Log.d(TAG,"PreviewSize->"+option.toString()+",ratio"+((float) option.getWidth() / option.getHeight()));

            if(w == option.getWidth() && h == option.getHeight()){
                return option;
            }

            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        for(Size size : bigEnough){
            Log.d(TAG,"BigSize:"+size.toString());
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    /**
     * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
     * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
     *
     * @param choices The list of available sizes
     * @return The video size
     */
    private static Size chooseVideoSize(Size[] choices,int reqWidth,int reqHeight,int orienation,float ratio) {

        int tmpWidth,tmpHeight;
        if(orienation == Configuration.ORIENTATION_PORTRAIT){
            tmpHeight = reqWidth;
            tmpWidth = reqHeight;
        }else{
            tmpWidth = reqWidth;
            tmpHeight = reqHeight;
        }
        for (Size size : choices) {

            if(tmpHeight == size.getHeight() && tmpWidth == size.getWidth()){
                return size;
            }
            float sizeRotio = (float) size.getWidth() / size.getHeight();
            if((tmpHeight == size.getHeight() || tmpWidth == size.getWidth()) &&
                    sizeRotio == ratio){
                return size;
            }
            Log.d(TAG,"videoSize:"+size.toString()+",ratio:"+(float)size.getWidth()/size.getHeight());
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        Log.e(TAG, "Couldn't find any suitable video size");
        return choices[choices.length - 1];

    }




    private TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            Log.d(TAG,"width:"+width+",height:"+height);
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }

    };

    private void openCamera(int width, int height) {
        final Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) {
            return;
        }
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            Log.d(TAG, "tryAcquire");
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            String cameraId = manager.getCameraIdList()[0];

            // Choose the sizes for camera preview and video recording
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            if (map == null) {
                throw new RuntimeException("Cannot get available preview/video sizes");
            }
            int orientation = getResources().getConfiguration().orientation;
            float faceDetectRatio = mFaceDetechHelper.getFaceDetectAspecRatio(characteristics);
            mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class),width,height,orientation,faceDetectRatio);
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                    width, height, mVideoSize,faceDetectRatio);
            mFaceDetechHelper.setPreViewTopSize(mTextureView);
            Log.d(TAG,"mVideoSize:"+mVideoSize.toString()+",mPreviewSize:"+mPreviewSize.toString()+",width:"+width+",height:"+height+",orientation:"+orientation);
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            } else {
                mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            }
            configureTransform(width, height);


            mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(),mPreviewSize.getHeight(), ImageFormat.JPEG,1);

            mImageReader.setOnImageAvailableListener(mImageAvailableListener,mBackgroundHandler);

            mFaceDetechHelper.init(characteristics,mPreviewSize,mSensorOrientation,mDisplayRotation);

            mMediaRecorder = new MediaRecorder();

            manager.openCamera(cameraId, mStateCallback, null);

        } catch (CameraAccessException e) {
            Toast.makeText(activity, "Cannot access the camera.", Toast.LENGTH_SHORT).show();
            activity.finish();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.");
        }
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            closePreviewSession();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Start tack picture
     */

    private void tackPicture(){
        if(mCameraDevice == null || mTextureView == null || !mTextureView.isAvailable()){
            Log.e(TAG,"Can't take picture return!");
            return;
        }
        isTacking = true;
        try {
            CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            if(mImageReader == null){
                Log.e(TAG,"takPicture error!");
                return;
            }
            builder.addTarget(mImageReader.getSurface());
            builder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_AUTO);
            builder.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            builder.set(CaptureRequest.JPEG_ORIENTATION,mSensorOrientation);
            if(mPreviewSession == null){
              Toast.makeText(getContext(),"拍照异常！",Toast.LENGTH_SHORT).show();
            }else
                mPreviewSession.capture(builder.build(),null,mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Log.e(TAG,"Can't take picture");

        }
    }

    /**
     * Start the camera preview.
     */
    private void startPreview() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            Surface previewSurface = new Surface(texture);
            mPreviewBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface,mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mPreviewSession = session;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Activity activity = getActivity();
                            if (null != activity) {
                                Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update the camera preview. {@link #startPreview()} needs to be called in advance.
     */
    private void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            setFaceDetectRequestBuild(mPreviewBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), mFaceDetechHelper.getCaptureCallback(), mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }

    private void setFaceDetectRequestBuild(CaptureRequest.Builder build){
        if(mFaceDetechHelper.canFaceDetect() && !mIsRecordingVideo){
            build.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CameraCharacteristics.STATISTICS_FACE_DETECT_MODE_SIMPLE);
        }
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should not to be called until the camera preview size is determined in
     * openCamera, or until the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    private void setUpMediaRecorder() throws IOException {
        Log.d(TAG,"setUpMediaRecorder");
        final Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath.isEmpty()) {
            mNextVideoAbsolutePath = getVideoFilePath(getActivity());
        }
        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (mSensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                break;
        }
        mMediaRecorder.prepare();
    }

    private String getVideoFilePath(Context context) {
        String dir = CommCont.getMediaDir(context,CommCont.TYPE_VIDEO);
        String name = CommCont.getRecordName(context);
        File videoFile = new File(dir,name+"-"+System.currentTimeMillis()+".mp4");
        try {
            videoFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return videoFile.getAbsolutePath();
    }

    private void startRecordingVideo() {
        Log.d(TAG,"startRecordingVideo");
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();
            setUpMediaRecorder();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            // Set up Surface for the camera preview
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

            // Set up Surface for the MediaRecorder
            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            mPreviewBuilder.addTarget(recorderSurface);

            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mPreviewSession = cameraCaptureSession;
                    updatePreview();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // UI
                            mMonitorBtn.setText(R.string.record_progress);
                            mMonitorBtn.setEnabled(false);
                            mIsRecordingVideo = true;

                            // Start recording
                            mMediaRecorder.start();
                        }
                    });
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Activity activity = getActivity();
                    if (null != activity) {
                        Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException | IOException e) {
            e.printStackTrace();
        }

    }

    private void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    private void stopRecordingVideo() {
        // UI
        mIsRecordingVideo = false;
        mMonitorBtn.setText(R.string.monitor_progress);
        mMonitorBtn.setEnabled(true);
        if(mPreviewSession!=null) {
            try {
                mPreviewSession.abortCaptures();
                mPreviewSession.stopRepeating();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        // Stop recording
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        Log.d(TAG, "Video saved: " + mNextVideoAbsolutePath);
        CommCont.insertRecord(getContext(),CommCont.TYPE_VIDEO,mNextVideoAbsolutePath,0);
        mNextVideoAbsolutePath = null;

        //startPreview();
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    public static class CountDownDialog extends DialogFragment{

        private CountDownTimer mCountDownTimer;

        private AlertDialog mInstance;

        private TickCallback callback;

        public CountDownDialog(){}

        public CountDownDialog(TickCallback callback){
            this.callback = callback;
        }


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mCountDownTimer = new CountDownTimer(10000,1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    mInstance.setMessage("监控将于"+(millisUntilFinished / 1000)+"S后开启");
                }

                @Override
                public void onFinish() {
                    startWatch = true;
                    if(callback!=null){
                        callback.onFinishTick();
                    }
                    if(mInstance!=null){
                        mInstance.dismiss();
                    }
                }
            };
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("监控倒计时");
            builder.setMessage("监控将于10S后开启");
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(mCountDownTimer!= null){
                        mCountDownTimer.cancel();
                        dialog.dismiss();
                    }
                }
            });
            mInstance = builder.create();
            return mInstance;
        }

        @Override
        public void showNow(FragmentManager manager, String tag) {
            super.showNow(manager, tag);
            mCountDownTimer.start();
        }


        public interface TickCallback {
            void onFinishTick();
        }
    }

    @Override
    public int getTitleRes() {
        return R.string.title_monitor;
    }
}
