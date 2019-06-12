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
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * Created by user on 27-May-19.
 */

public class Test2 extends AppCompatActivity{
    protected static final String TAG = null;

    private static int RESULT_LOAD_IMAGE = 1;
    public Mat img=new Mat();
    public Mat result=new Mat();
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

            result = steptowatershed(img);

            //hough circles
            Mat circles = new Mat();

            // parameters
            int iCannyUpperThreshold = 10;
            int iMinRadius = 0;
            int iMaxRadius = 20;
            int iAccumulator = 5;

            Imgproc.HoughCircles(result,circles,Imgproc.CV_HOUGH_GRADIENT,1,result.rows()/8,iCannyUpperThreshold,iAccumulator,iMinRadius,iMaxRadius);
            Log.i("cccccccccccccccccccccc","cccc "+circles.cols());
            // draw
            if (circles.cols() > 0)
            {
                Toast.makeText(this, "Coins : " +circles.cols() , Toast.LENGTH_LONG).show();
//                alertString = "Number of coins detected : " + circles.cols();
//                displayAlert(alertString);
            }
            else
            {
                Toast.makeText(this, "No coins found", Toast.LENGTH_LONG).show();
//                alertString = "No objects detected";
//                displayAlert(alertString);
            }

            for (int x = 0; x < circles.cols(); x++) {
                double[] c = circles.get(0, x);
                Point center = new Point(Math.round(c[0]), Math.round(c[1]));
                // circle center
                Imgproc.circle(result, center, 1, new Scalar(255,255,0), 2, 8, 0 );
                // circle outline
                int radius = (int) Math.round(c[2]);
                Imgproc.circle(result, center, radius, new Scalar(0,0,255), 2, 8, 0 );
            }

            Utils.matToBitmap(result, bmp, true);
            Log.i(TAG, "all okay");
            imageView.setImageBitmap(bmp);
//            Boolean grayBool = Imgcodecs.imwrite(picturePath + "gray.jpg", result);

        }
    }

    public Mat steptowatershed(Mat img)
    {
        Mat threeChannel = new Mat();
        Imgproc.cvtColor(img, threeChannel, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(threeChannel, threeChannel, 120, 255, Imgproc.THRESH_BINARY);

//        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//        Mat hierarchy = new Mat();
//        // find contours:
//        Imgproc.findContours(threeChannel, contours, hierarchy, Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);

        //fg = fore ground
        Mat fg = new Mat(img.size(), CvType.CV_8U);
        Imgproc.erode(threeChannel,fg,new Mat());

        //bg = background
        Mat bg = new Mat(img.size(),CvType.CV_8U);
        Imgproc.dilate(threeChannel,bg,new Mat());
        Imgproc.threshold(bg,bg,1, 1,Imgproc.THRESH_BINARY_INV);

        Mat markers = new Mat(img.size(),CvType.CV_8U, new Scalar(0));
        Core.add(fg, bg, markers);

        Mat result1=new Mat();
        WatershedSegmenter segmenter = new WatershedSegmenter();
        //used function for segmentation of watershed
        segmenter.setMarkers(markers);
        result1 = segmenter.process(img);

        return result1;
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
