package com.nevaryyy.beautyfilterdemo.main;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.nevaryyy.beautyfilterdemo.R;

import jp.co.cyberagent.android.gpuimage.GPUImage;


public class PicActivity extends AppCompatActivity {

    private Button picBtn,beauty_btn;
    private ImageView ivOld,ivNew;
    private String mImgPath;
    private GPUImage gpuImage;
    private Bitmap bmp;

    private GPUImageBeauty3Filter mBeautyFilter;
    private static float minbrightValue= 0;
    private static float maxbrightValue= 10;
    protected SeekBar sb_bright;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic);

        initView();
        gpuImage = new GPUImage(this);
        mBeautyFilter = new GPUImageBeauty3Filter();
    }

    private void initView() {
        ivOld = (ImageView) findViewById(R.id.ivOld);
        ivNew = (ImageView) findViewById(R.id.ivNew);
        picBtn = (Button) findViewById(R.id.search_btn);
        picBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //调用相册
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });


        sb_bright = (SeekBar) findViewById(R.id.sb_bright);
        sb_bright.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    Log.e("wangshuo","sb_bright ProgressChanged");
                    mBeautyFilter.setBrightLevel(range(progress,minbrightValue,maxbrightValue));
                   //mBeautyFilter.setParams(1.2f , range(progress,-5,5));
                    gpuImage.setFilter(mBeautyFilter);
                    ivNew.setImageBitmap(gpuImage.getBitmapWithFilterApplied(bmp));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        beauty_btn = (Button) findViewById(R.id.beauty_btn);
        beauty_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBeautyFilter.setBrightLevel(10);
                gpuImage.setFilter(new GPUImageBeauty3Filter(2.0f,-0.5f,1.2f,0.47f));
                ivNew.setImageBitmap(gpuImage.getBitmapWithFilterApplied(bmp));
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            mImgPath = c.getString(columnIndex);
            Log.e("wangshuo","img->"+mImgPath);
            bmp= BitmapFactory.decodeFile(mImgPath);

            ivOld.setImageBitmap(bmp);

            //gpuImage.setImage(bmp);
            gpuImage.setFilter(mBeautyFilter);
            //显示处理后的图片
            ivNew.setImageBitmap(gpuImage.getBitmapWithFilterApplied(bmp));
            c.close();
        }
    }

    protected float range(final int percentage, final float start, final float end) {
        return (end - start) * percentage / 100.0f + start;
    }
}
