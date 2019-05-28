package com.example.user.colonycounter;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;

import org.opencv.android.JavaCameraView;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by user on 27-May-19.
 */

public class myJavaCameraView extends JavaCameraView implements android.hardware.Camera.PictureCallback {

    static {
        System.loadLibrary("native-lib");
    }
    private static final String TAG="OpenCV";
    private String mPictureFileName;

    public myJavaCameraView(Context context, AttributeSet attrs){
        super(context,attrs);
    }

    public void takePicture(final String fileName){
        Log.i(TAG,"Taking Picture");
        this.mPictureFileName = fileName;
        mCamera.setPreviewCallback(null);
        mCamera.takePicture(null,null,this);
    }

    @Override
    public void onPictureTaken(byte[] bytes, android.hardware.Camera camera) {

        Log.i(TAG,"Saving bitmap to file");
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);

        try{
            FileOutputStream foe = new FileOutputStream(mPictureFileName);
            foe.write(bytes);
            foe.close();
        }catch (java.io.IOException e){
            Log.e("PictureDemo","Exception in Photo Callback",e);
        }
    }
}
