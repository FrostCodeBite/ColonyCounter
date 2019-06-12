package com.example.user.colonycounter;

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
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 27-May-19.
 */

public class Magic extends AppCompatActivity{
    protected static final String TAG = null;

    private static int RESULT_LOAD_IMAGE = 1;
//    public Mat img=new Mat();
//    public Mat result=new Mat();
    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
    }
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watershed);

        Button buttonLoadImage = (Button) findViewById(R.id.buttonLoadPicture);
        buttonLoadImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            ImageView imageView = (ImageView) findViewById(R.id.imgView);
            Bitmap bmp = BitmapFactory.decodeFile(picturePath);
            Log.i(TAG, picturePath);
            Mat img= Imgcodecs.imread(picturePath);

            Process process = new Process();
            //TurntoGrey
            Mat resultGrey = process.turnGrey(img);
//            Utils.matToBitmap(resultGrey, bmp, true);
//            imageView.setImageBitmap(bmp);
//            Boolean grayBool = Imgcodecs.imwrite(picturePath + "gray.jpg", result);

            //foreground
            Mat fg = process.foreGround(resultGrey);
//            Utils.matToBitmap(fg, bmp, true);
//            imageView.setImageBitmap(bmp);

            Mat bg = process.backGround(resultGrey);
//            Utils.matToBitmap(bg, bmp, true);
//            imageView.setImageBitmap(bmp);

            Mat markers = process.setMarkers(resultGrey,fg,bg);
//            Utils.matToBitmap(markers, bmp, true);
//            imageView.setImageBitmap(bmp);

            Mat finalStep = process.stepToWatershed(markers,img);

            Mat circles = process.findCircle(finalStep);
            process.drawCircle(finalStep, circles);

            int num = circles.cols();
            // draw
            if (num > 0)
            {
                Toast.makeText(this, "Colonies : " +num , Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(this, "No colonies found", Toast.LENGTH_LONG).show();
            }

            Utils.matToBitmap(finalStep, bmp, true);
            imageView.setImageBitmap(bmp);

            TextView txt = findViewById(R.id.textView);
            txt.setText(String.valueOf(num));
        }
    }

    public class Process{

        public Mat findCircle(Mat img){
            //hough circles
            Mat circles = new Mat();

            // parameters
            int iCannyUpperThreshold = 5;
            int iMinRadius = 0;
            int iMaxRadius = 3;
            int iAccumulator = 3;

            Imgproc.HoughCircles(img,circles,Imgproc.CV_HOUGH_GRADIENT,1, img.rows()/100,iCannyUpperThreshold,iAccumulator,iMinRadius,iMaxRadius);
            return circles;
        }

        public void drawCircle(Mat result, Mat circles){
            for (int x = 0; x < circles.cols(); x++) {
                double[] c = circles.get(0, x);
                Point center = new Point(Math.round(c[0]), Math.round(c[1]));
                // circle center
                Imgproc.circle(result, center, 1, new Scalar(255,255,0), 2, 8, 0 );
                // circle outline
                int radius = (int) Math.round(c[2]);
                Imgproc.circle(result, center, radius, new Scalar(0,0,255), 2, 8, 0 );
            }
        }

//        public List findContours(Mat img){
//            // Find total markers
//            List<MatOfPoint> contours = new ArrayList<>();
//            Mat hierarchy = new Mat();
//            Imgproc.findContours(img, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//            return contours;
//        }
//
//        public void drawForeground(Mat markers, List contours){
//            // Draw the foreground markers
//            for (int i = 0; i < contours.size(); i++) {
//                Imgproc.drawContours(markers, contours, i, new Scalar(i + 1), -1);
//            }
//        }
//
//        public void drawBackground(Mat markers){
//            // Draw the background marker
//            Mat markersScaled = new Mat();
//            markers.convertTo(markersScaled, CvType.CV_32F);
//            Core.normalize(markersScaled, markersScaled, 0.0, 255.0, Core.NORM_MINMAX);
//            Imgproc.circle(markersScaled, new Point(5, 5), 3, new Scalar(255, 255, 255), -1);
//            Mat markersDisplay = new Mat();
//            markersScaled.convertTo(markersDisplay, CvType.CV_8U);
//            Imgproc.circle(markers, new Point(5, 5), 3, new Scalar(255, 255, 255), -1);
//        }

        public Mat turnGrey(Mat img){
            Mat grey = new Mat();
            Imgproc.cvtColor(img, grey, Imgproc.COLOR_BGR2GRAY);
            Imgproc.threshold(grey, grey, 120, 255, Imgproc.THRESH_BINARY);
            return grey;
        }
        public Mat foreGround(Mat img){
            //fg = fore ground
            Mat fg = new Mat(img.size(), CvType.CV_8U);
            Imgproc.erode(img,fg,new Mat());
            return fg;
        }
        public Mat backGround(Mat img){
            //bg = background
            Mat bg = new Mat(img.size(),CvType.CV_8U);
            Imgproc.dilate(img,bg,new Mat());
            //There might be some problems here regarding to threading
            Imgproc.threshold(bg,bg,1, 1,Imgproc.THRESH_BINARY_INV);
            return bg;
        }
        public Mat setMarkers(Mat img, Mat fg,Mat bg){
            Mat markers = new Mat(img.size(),CvType.CV_8U, new Scalar(0));
            Core.add(fg, bg, markers);
            return markers;
        }
        public Mat stepToWatershed(Mat markers, Mat img){
            Mat finalStep=new Mat();
            WatershedSegmenter segmenter = new WatershedSegmenter();
            //used function for segmentation of watershed
            segmenter.setMarkers(markers);
            finalStep = segmenter.process(img);
            return finalStep;
        }
    }

    //function in Class
    public class WatershedSegmenter
    {
        public Mat markers=new Mat();

        //set markers color ==> background color
        public void setMarkers(Mat markerImage)
        {
            markerImage.convertTo(markers, CvType.CV_32SC1);
        }

        //process watershed in image
        public Mat process(Mat image)
        {
            Imgproc.watershed(image,markers);
            markers.convertTo(markers,CvType.CV_8U);
            return markers;
        }
    }
}
