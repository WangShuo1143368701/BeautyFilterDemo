package com.nevaryyy.beautyfilterdemo.main;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nevaryyy.beautyfilterdemo.R;
import com.nevaryyy.beautyfilterdemo.base.BaseActivity;
import com.nevaryyy.beautyfilterdemo.permission.PermissionCheck;
import com.nevaryyy.beautyfilterdemo.util.DialogUtil;
import com.nevaryyy.beautyfilterdemo.util.LogUtil;

import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;

/**
 * @author ws
 */
public class MainActivity extends BaseActivity {

    private static final int PICTURE_WIDTH = 1280;
    private static final int PICTURE_HEIGHT = 720;

    private static final int PREVIEW_WIDTH = 1280;
    private static final int PREVIEW_HEIGHT = 720;

    private GLSurfaceView glSurfaceView;
    private ImageButton magicImageButton;
    private ImageButton switchImageButton;

    private GPUImage gpuImage;
    private GPUImageFilterGroup magicFilterGroup;
    private GPUImageFilterGroup noMagicFilterGroup;

    private boolean isInMagic;

    private boolean cameraDenied;

    private boolean isPreviewing;

    private int currentCameraId;

    private Camera camera;

    protected SeekBar sb_tone;
    protected SeekBar sb_beauty;
    protected SeekBar sb_bright;
    protected SeekBar sb_stepoffset;
    protected TextView mTextView;
    protected Button picBtn;
    private GPUImageBeauty3Filter mGPUImageBeautyFilter;

    private static float minToneValue= -5;
    private static float maxToneValue= 5;
    private static float minbeautyValue= 0;
    private static float maxbeautyValue= 2.5f;
    private static float minbrightValue= 0;
    private static float maxbrightValue= 1;
    private static float minstepoffset= -10;
    private static float maxstepoffset= 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        glSurfaceView = (GLSurfaceView) findViewById(R.id.glsv_main);
        magicImageButton = (ImageButton) findViewById(R.id.ib_main_magic);
        switchImageButton = (ImageButton) findViewById(R.id.ib_main_switch);

        magicImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isInMagic = !isInMagic;
                gpuImage.setFilter(isInMagic ? magicFilterGroup : noMagicFilterGroup);
            }
        });

        switchImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });



        mGPUImageBeautyFilter=new GPUImageBeauty3Filter();
        mTextView = (TextView) findViewById(R.id.textView);
        sb_tone = (SeekBar) findViewById(R.id.sb_tone);
        sb_beauty = (SeekBar) findViewById(R.id.sb_beauty);
        sb_bright = (SeekBar) findViewById(R.id.sb_bright);
        sb_stepoffset = (SeekBar) findViewById(R.id.sb_stepoffset);
        picBtn = (Button) findViewById(R.id.picBtn);
        picBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,PicActivity.class);
                startActivity(intent);
            }
        });

        sb_stepoffset.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if(isInMagic && mGPUImageBeautyFilter!=null){
                    Log.e("wangshuo","sb_stepoffset ProgressChanged");
                    mGPUImageBeautyFilter.setTexelOffset(range(progress,minstepoffset,maxstepoffset));

                    mTextView.setText("setTexelOffset: " +range(progress,minstepoffset,maxstepoffset) +" beauty: " +range(sb_beauty.getProgress(),minbeautyValue,maxbeautyValue)+" bright: "+range(sb_bright.getProgress(),minbrightValue,maxbrightValue));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb_tone.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                 if(isInMagic && mGPUImageBeautyFilter!=null){
                     Log.e("wangshuo","sb_tone ProgressChanged");
                     mGPUImageBeautyFilter.setParams(range(sb_beauty.getProgress(),minbeautyValue,maxbeautyValue) , range(progress,minToneValue,maxToneValue));

                     mTextView.setText("tone: " +range(progress,minToneValue,maxToneValue) +" beauty: " +range(sb_beauty.getProgress(),minbeautyValue,maxbeautyValue)+" bright: "+range(sb_bright.getProgress(),minbrightValue,maxbrightValue));
                 }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb_beauty.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(isInMagic && mGPUImageBeautyFilter!=null){
                    Log.e("wangshuo","sb_beauty ProgressChanged");
                    mGPUImageBeautyFilter.setParams(range(progress,minbeautyValue,maxbeautyValue),range(sb_tone.getProgress(),minToneValue,maxToneValue));

                    mTextView.setText("tone: " +range(sb_tone.getProgress(),minToneValue,maxToneValue) +" beauty: " +range(progress,minbeautyValue,maxbeautyValue)+" bright: "+range(sb_bright.getProgress(),minbrightValue,maxbrightValue));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb_bright.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(isInMagic && mGPUImageBeautyFilter!=null){
                    Log.e("wangshuo","sb_bright ProgressChanged");
                    mGPUImageBeautyFilter.setBrightLevel(range(progress,minbrightValue,maxbrightValue));

                    mTextView.setText("tone: " +range(sb_tone.getProgress(),minToneValue,maxToneValue) +" beauty: " +range(sb_beauty.getProgress(),minbeautyValue,maxbeautyValue)+" bright: "+range(progress,minbrightValue,maxbrightValue));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        init();
    }

    @Override
    protected void onPause() {
        super.onPause();

        clearCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!cameraDenied) {
            checkPermission(PermissionCheck.getCameraPermissionCheck(
                    new PermissionCheck.OnPermissionChecked() {
                        @Override
                        public void callback() {
                            initCamera(currentCameraId);
                        }
                    },
                    new PermissionCheck.OnPermissionChecked() {
                        @Override
                        public void callback() {
                            cameraDenied = true;
                            DialogUtil.showNoPermissionDialog(MainActivity.this,
                                    getString(R.string.dialog_no_camera_permission_message), null);
                        }
                    }
            ));
        }
    }

    private void init() {
        isInMagic = true;
        isPreviewing = false;
        currentCameraId = 0;

        gpuImage = new GPUImage(this);
        gpuImage.setGLSurfaceView(glSurfaceView);

        magicFilterGroup = new GPUImageFilterGroup();
        magicFilterGroup.addFilter(mGPUImageBeautyFilter);

        noMagicFilterGroup = new GPUImageFilterGroup();
        noMagicFilterGroup.addFilter(new GPUImageFilter());

        //start
        GPUImagePicFilter PicFilter = new GPUImagePicFilter();
        PicFilter.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.filter_weimei));
        magicFilterGroup.addFilter(PicFilter);
        //end

        gpuImage.setFilter(magicFilterGroup);
    }

    private void initCamera(int cameraId) {
        try {
            if (camera == null) {
                currentCameraId = cameraId;
                LogUtil.d(currentCameraId + "!");
                camera = Camera.open(cameraId);
                //camera.setDisplayOrientation(90);

                Camera.Parameters parameters = camera.getParameters();
                parameters.setPictureFormat(PixelFormat.JPEG);
                List<Camera.Size> sizeList = parameters.getSupportedPictureSizes();
                int width = 0;
                int height = 0;

                for (Camera.Size size : sizeList) {
                    if (size.width * size.height <= PICTURE_HEIGHT * PICTURE_WIDTH) {
                        if (size.width * size.height > width * height) {
                            width = size.width;
                            height = size.height;
                        }
                    }
                }
                parameters.setPictureSize(width, height);
                LogUtil.d("picture: " + width + " " + height);

                sizeList = parameters.getSupportedPreviewSizes();
                width = 0;
                height = 0;
                for (Camera.Size size : sizeList) {
                    if (size.width * size.height <= PREVIEW_WIDTH * PREVIEW_HEIGHT) {
                        if (size.width * size.height > width * height) {
                            width = size.width;
                            height = size.height;
                        }
                    }
                }
                parameters.setPreviewSize(width, height);
                LogUtil.d("preview: " + width + " " + height);

                List<String> stringList = parameters.getSupportedFocusModes();
                for (String s : stringList) {
                    LogUtil.d(s);
                }
                if (cameraId == 0) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }

                camera.setParameters(parameters);

//                try {
//                    camera.setPreviewDisplay(surfaceHolder);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return;
//                }

                if (!isPreviewing) {
                    isPreviewing = true;
                    //camera.startPreview();
                    gpuImage.setUpCamera(camera, cameraId == 0 ? 90 : 270, cameraId > 0, false);
                }
            }
        }
        catch (Exception e) {
            DialogUtil.showNoPermissionDialog(MainActivity.this,
                    getString(R.string.dialog_no_camera_permission_message), null);
        }
    }

    private void switchCamera() {
        clearCamera();
        checkPermission(PermissionCheck.getCameraPermissionCheck(
                new PermissionCheck.OnPermissionChecked() {
                    @Override
                    public void callback() {
                        initCamera(currentCameraId ^ 1);
                    }
                },
                new PermissionCheck.OnPermissionChecked() {
                    @Override
                    public void callback() {
                        cameraDenied = true;
                        DialogUtil.showNoPermissionDialog(MainActivity.this,
                                getString(R.string.dialog_no_camera_permission_message), null);
                    }
                }
        ));
    }

    private void clearCamera() {
        if (camera != null) {
            gpuImage.deleteImage();
            camera.setPreviewCallback(null);
            camera.stopPreview();
            isPreviewing = false;
            camera.release();
            camera = null;
        }
    }

    protected float range(final int percentage, final float start, final float end) {
        return (end - start) * percentage / 100.0f + start;
    }
}
