package com.ws.ijk.camera;

import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private SurfaceView mSview;
    private Camera mCamera;
    private SurfaceHolder mHolder;

    private Camera.PictureCallback pictureCallback=new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File file=new File("/sdcard/temp.png");
            try {
                FileOutputStream fos=new FileOutputStream(file);
                fos.write(data);

                fos.close();

                Intent intent =new Intent(MainActivity.this,ResultActivity.class);
                intent.putExtra("picpath",file.getAbsolutePath());
                startActivity(intent);
                MainActivity.this.finish();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        mHolder=mSview.getHolder();
        mHolder.addCallback(this);
    }

    private void initView() {
        mSview= (SurfaceView) findViewById(R.id.preView);

        mSview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.autoFocus(null);//点击自动对焦
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCamera==null){
            mCamera=getCamera();

            if (mHolder!=null){
                setStartPreview(mCamera,mHolder);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }
    //点击事件
    public void capture(View view){
        Camera.Parameters parameters=mCamera.getParameters();//参数
        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.setPictureSize(800, 400);
        parameters.setFlashMode(Camera.Parameters.FOCUS_MODE_AUTO);//自动对焦

        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    camera.takePicture(null, null, pictureCallback);
                }
            }
        });


    }

    /**
     * 获取系统camera对象
     * @return
     */
    private Camera getCamera(){
        Camera camera=null;

        try {
            camera=Camera.open(1);
        }catch (Exception e){
            camera=null;
            e.printStackTrace();
        }

        return camera;
    }
    //实时预览相机图像
    private void setStartPreview(Camera camera,SurfaceHolder holder){
        try {
            camera.setPreviewDisplay(holder);//将相机与surfaceView控件绑定
            camera.setDisplayOrientation(90);//将相机试图旋转90度，因为默认是横屏显示
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //释放相机资源
    private void releaseCamera(){

        if(mCamera!=null){
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera=null;
        }
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setStartPreview(mCamera, mHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCamera.stopPreview();
        setStartPreview(mCamera, mHolder);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }
}
