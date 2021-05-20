package com.scanaway;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

public class ScanActivity extends AppCompatActivity {

    ImageView iv, iv2, iv3, iv4;
    ProgressDialog progressDialog;
    private static String TAG = "MainActivity";

    static {

        if (OpenCVLoader.initDebug()) {

            Log.i(TAG, "opencv true");
        } else {
            Log.i(TAG, "opencv false");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        iv = findViewById(R.id.imageView);
        iv2 = findViewById(R.id.imageView2);
        iv3 = findViewById(R.id.imageView3);
        iv4 = findViewById(R.id.imageView4);


        Bitmap currentBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.t);
        Mat grayMat = new Mat();
        Mat cannyEdges = new Mat();
        Mat hierarchy = new Mat();
        Mat originalMat = new Mat();
        Mat thresh = new Mat();

        Utils.bitmapToMat(currentBitmap,originalMat);
        Imgproc.cvtColor(originalMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.adaptiveThreshold(grayMat, thresh, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 15, 12);
        Utils.bitmapToMat(currentBitmap, originalMat);
        List<MatOfPoint> contourList = new ArrayList<MatOfPoint>(); //A list to store all the contours

        //Converting the image to grayscale
        Imgproc.cvtColor(originalMat, grayMat, Imgproc.COLOR_BGR2GRAY);

        Imgproc.adaptiveThreshold(grayMat, thresh, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 15, 12);
        Imgproc.dilate(grayMat,grayMat,new Mat());
        Imgproc.erode(grayMat,grayMat,new Mat());
        Imgproc.Canny(grayMat, cannyEdges, 30, 100);

        //finding contours
        Imgproc.findContours(cannyEdges, contourList, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        hierarchy.release();
        double maxVal = 0;
        int maxValIdx = 0;
        for (int contourIdx = 0; contourIdx < contourList.size(); contourIdx++) {
            double contourArea = Imgproc.contourArea(contourList.get(contourIdx));
            if (maxVal < contourArea) {
                maxVal = contourArea;
                maxValIdx = contourIdx;
            }
        }

        Imgproc.drawContours(originalMat, contourList, maxValIdx, new Scalar(0, 255, 0), 5);
        Bitmap grayb = BitmapFactory.decodeResource(getResources(), R.drawable.t);
        Bitmap threshb = BitmapFactory.decodeResource(getResources(), R.drawable.t);
        Bitmap original = BitmapFactory.decodeResource(getResources(), R.drawable.t);

        Utils.matToBitmap(grayMat,grayb);
        Utils.matToBitmap(thresh,threshb);
        Utils.matToBitmap(originalMat,original);

        iv.setImageBitmap(currentBitmap);
        iv2.setImageBitmap(original);
        iv3.setImageBitmap(grayb);
        iv4.setImageBitmap(threshb);
    }
}