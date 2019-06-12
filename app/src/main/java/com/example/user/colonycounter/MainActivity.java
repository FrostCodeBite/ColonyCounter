package com.example.user.colonycounter;

import android.media.MediaActionSound;
import android.nfc.Tag;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

//    CameraBridgeViewBase cameraBridgeViewBase;

    Mat mat1,mat2,mat3;

    //part3 continued
    BaseLoaderCallback baseLoaderCallback;

    Button imgButton;

    static {
        System.loadLibrary("native-lib");
    }
    private static final String TAG= "OCVSample:Activity";
    myJavaCameraView cameraBridgeViewBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraBridgeViewBase = (myJavaCameraView)findViewById(R.id.myCameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

//        imgButton = findViewById(R.id.take_picture);
//        imgButton.setOnClickListener(new View.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
//            @Override
//            public void onClick(View view) {
//                MediaActionSound sound = new MediaActionSound();
//                sound.play(MediaActionSound.SHUTTER_CLICK);
//                Log.i(TAG,"on Button Click");
//                Date sdf = new Date();
//                String currentDateTime = sdf.toString();
//                String fileName = Environment.getExternalStorageDirectory().getPath()+
//                        "/sample_picture"+currentDateTime+".jpeg";
//                cameraBridgeViewBase.takePicture(fileName);
//
//            }
//        });

        //part 3 continued
        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
                switch (status){
                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mat1 = new Mat(width,height, CvType.CV_8UC4);
//        mat2 = new Mat(width,height, CvType.CV_8UC4);
//        mat3 = new Mat(width,height, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mat1.release();
//        mat2.release();
//        mat3.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mat1 = inputFrame.rgba();
        //rotate the frame 90o ==> doesn't work
//        Core.transpose(mat1,mat3);
//
//        Imgproc.resize(mat3,mat2,mat2.size(),0,0,0);
//        Core.flip(mat2,mat1,1);

//        Mat mRgbaT = mat1.t();
//        Core.flip(mat1.t(),mRgbaT,1);
//        Imgproc.resize(mRgbaT,mRgbaT,mat1.size());
//        return mat1;
//        return mRgbaT;
        Mat mRgbaT = mat1.t();
        Core.flip(mat1.t(),mRgbaT,1);
        Imgproc.resize(mRgbaT,mRgbaT,mat1.size());
        return mRgbaT;
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if(cameraBridgeViewBase!=null){
//            cameraBridgeViewBase.disableView();
//        }
        cameraBridgeViewBase.disableView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(),"There is a problem in opencv",Toast.LENGTH_SHORT).show();
        }else{
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        if(cameraBridgeViewBase!=null){
//            cameraBridgeViewBase.disableView();
//        }
    }
}
