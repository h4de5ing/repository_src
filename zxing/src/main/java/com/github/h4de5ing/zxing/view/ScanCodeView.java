package com.github.h4de5ing.zxing.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.h4de5ing.zxing.R;
import com.github.h4de5ing.zxing.coder.OnScanCodeListener;
import com.github.h4de5ing.zxing.coder.ZXReader;
import com.github.h4de5ing.zxing.utils.CameraManager;
import com.github.h4de5ing.zxing.utils.FocusSensor;
import com.google.zxing.Result;

/**
 * 扫码视图
 * <uses-permission android:name="android.permission.CAMERA"/>
 */
public class ScanCodeView extends FrameLayout implements CameraManager.OnCameraPreviewListener,
        FocusSensor.OnFocusSensorChangedListener, OnScanCodeListener {

    private TextureView textureView;
    private CameraManager cameraManager;
    private ScanAreaView scanAreaView;
    private FocusSensor focusSensor;
    private OnScanCodeListener onScanCodeListener;
    private long previewTime = 0;
    public static boolean BETA = false;
    private long interval = 200;
    private String TAG = ScanCodeView.class.getSimpleName();

    public ScanCodeView(@NonNull Context context) {
        super(context);
        initAttributeSet(context, null);
    }

    public ScanCodeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttributeSet(context, attrs);
    }

    public ScanCodeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributeSet(context, attrs);
    }

    /**
     * 初始化
     *
     * @param context
     * @param attrs
     */
    protected void initAttributeSet(Context context, AttributeSet attrs) {
        //相机预览
        textureView = new TextureView(context);
        LayoutParams textureParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(textureView, textureParams);
        //扫描区域
        scanAreaView = new ScanAreaView(context);
        LayoutParams areaParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(scanAreaView, areaParams);
        //扫描区域参数
        initScanParams(context, attrs);
        //传感器
        focusSensor = new FocusSensor(getContext());
        //震动器
        focusSensor.setOnFocusSensorChangedListener(this);
        cameraManager = new CameraManager();
        cameraManager.setOnCameraPreviewListener(this);
        cameraManager.startPreview(textureView);
    }


    /**
     * 初始化扫描参数
     *
     * @param context
     * @param attrs
     */
    protected void initScanParams(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ScanCodeView);
        float centerX = array.getDimension(R.styleable.ScanCodeView_areaCenterX, scanAreaView.getCenterX());
        float centerY = array.getDimension(R.styleable.ScanCodeView_areaCenterY, scanAreaView.getCenterY());
        float areaWidth = array.getDimension(R.styleable.ScanCodeView_areaWidth, scanAreaView.getAreaWidth());
        float areaHeight = array.getDimension(R.styleable.ScanCodeView_areaHeight, scanAreaView.getAreaHeight());
        int backgroundColor = array.getColor(R.styleable.ScanCodeView_backgroundColor, scanAreaView.getBackgroundColor());
        int cornerLineColor = array.getColor(R.styleable.ScanCodeView_cornerLineColor, scanAreaView.getCornerLineColor());
        boolean cornerVisible = array.getBoolean(R.styleable.ScanCodeView_cornerVisible, scanAreaView.isCornerVisible());
        float cornerLineMargin = array.getDimension(R.styleable.ScanCodeView_cornerLineMargin, scanAreaView.getCornerLineMargin());
        float cornerLineLength = array.getDimension(R.styleable.ScanCodeView_cornerLineLength, scanAreaView.getCornerLineLength());
        float cornerLineWidth = array.getDimension(R.styleable.ScanCodeView_cornerLineWidth, scanAreaView.getCornerLineWidth());
        int duration = array.getInt(R.styleable.ScanCodeView_duration, scanAreaView.getDuration());
        int lineDrawable = array.getResourceId(R.styleable.ScanCodeView_lineDrawable, scanAreaView.getLineDrawable());
        boolean vibrator = array.getBoolean(R.styleable.ScanCodeView_vibrator, scanAreaView.isVibrator());
        scanAreaView.setCenterX(centerX);
        scanAreaView.setCenterY(centerY);
        scanAreaView.setAreaWidth(areaWidth);
        scanAreaView.setAreaHeight(areaHeight);
        scanAreaView.setBackgroundColor(backgroundColor);
        scanAreaView.setCornerVisible(cornerVisible);
        scanAreaView.setCornerLineColor(cornerLineColor);
        scanAreaView.setCornerLineMargin(cornerLineMargin);
        scanAreaView.setCornerLineLength(cornerLineLength);
        scanAreaView.setCornerLineWidth(cornerLineWidth);
        scanAreaView.setDuration(duration);
        scanAreaView.setLineDrawable(lineDrawable);
        scanAreaView.setVibrator(vibrator);
        array.recycle();
    }

    @Override
    public void onFocusSensorChanged(boolean stay) {
        if (stay) {
            cameraManager.autoFocus();
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (System.currentTimeMillis() - previewTime > interval) {
            int width = camera.getParameters().getPreviewSize().width;
            int height = camera.getParameters().getPreviewSize().height;
            float xScale = (height * 1.0F) / (scanAreaView.getWidth() * 1.0F);
            float yScale = (width * 1.0F) / (scanAreaView.getHeight() * 1.0F);
            int left = (int) (xScale * scanAreaView.getBorderLeft());
            int top = (int) (yScale * scanAreaView.getBorderTop());
            int right = (int) (xScale * (scanAreaView.getBorderRight() - scanAreaView.getBorderLeft()));
            int bottom = (int) (xScale * scanAreaView.getBorderBottom() - scanAreaView.getBorderTop());
            if (BETA) {
                Log.i(TAG, "->Frame " + "width=" + width + ",height=" + height);
                Log.i(TAG, "->Scale " + "xScale=" + xScale + ",yScale=" + yScale);
                Log.i(TAG, "->Border " + "left=" + scanAreaView.getBorderLeft() + ",top=" + scanAreaView.getBorderTop() + ",right=" + scanAreaView.getBorderRight() + ",bottom=" + scanAreaView.getBorderBottom());
                Log.i(TAG, "->Rect left=" + left + ",top=" + top + ",right=" + right + ",bottom=" + bottom);
            }
            ZXReader.fromYuv420(data, width, height, left, top, right, bottom, false, this);
            previewTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        int sizeWidth = cameraManager.getSize().width;
        int sizeHeight = cameraManager.getSize().height;
        int measureWidth = getMeasuredWidth();
        int measureHeight = getMeasuredHeight();
        if (sizeWidth > measureHeight) {
            textureView.setScaleY(sizeWidth / measureHeight);
        } else {
            textureView.setScaleY(measureHeight / sizeWidth);
        }
        if (sizeHeight > measureWidth) {
            textureView.setScaleY(sizeHeight / measureWidth);
        } else {
            textureView.setScaleY(measureWidth / sizeHeight);
        }
    }

    @Override
    public void onSurfaceTextureDestroyed(SurfaceTexture surface) {
        cameraManager.release();
    }

    /**
     * 扫码成功
     *
     * @param result 条码数据
     */
    @Override
    public void onScanCodeSucceed(Result result) {
        if (onScanCodeListener != null) {
            onScanCodeListener.onScanCodeSucceed(result);
        }
    }


    /**
     * 扫码失败
     *
     * @param exception
     */
    @Override
    public void onScanCodeFailed(Exception exception) {
        if (onScanCodeListener != null) {
            onScanCodeListener.onScanCodeFailed(exception);
        }
    }

    /**
     * 设置扫码监听
     *
     * @param onScanCodeListener
     */
    public void setOnScanCodeListener(OnScanCodeListener onScanCodeListener) {
        this.onScanCodeListener = onScanCodeListener;
    }

    public interface OnScanCodeListener {

        /**
         * 扫码成功
         *
         * @param result
         */
        void onScanCodeSucceed(Result result);

        /**
         * 扫码失败
         *
         * @param exception
         */
        void onScanCodeFailed(Exception exception);

    }

    /**
     * 获取预览视图
     *
     * @return
     */
    public TextureView getTextureView() {
        return textureView;
    }

    /**
     * 获取相机管理对象
     *
     * @return
     */
    public CameraManager getCameraManager() {
        return cameraManager;
    }

    /**
     * 获取焦点传感器
     *
     * @return
     */
    public FocusSensor getFocusSensor() {
        return focusSensor;
    }

    /**
     * 获取扫描区域
     *
     * @return
     */
    public ScanAreaView getScanAreaView() {
        return scanAreaView;
    }

    /**
     * 设置预览间隔
     *
     * @param interval
     */
    public void setInterval(long interval) {
        this.interval = interval;
    }

}