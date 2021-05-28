package com.scanaway;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.marcinmoskala.arcseekbar.ArcSeekBar;
import com.marcinmoskala.arcseekbar.ProgressListener;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgcodecs.Imgcodecs.IMREAD_GRAYSCALE;
import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C;
import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_MEAN_C;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

public class FilterActivity extends AppCompatActivity {

    private static String TAG = "FilterActivity";

    static {

        if (OpenCVLoader.initDebug()) {

            Log.i(TAG, "opencv true");
        } else {
            Log.i(TAG, "opencv false");
        }
    }

    Bitmap original;
    Bitmap testPrev;
    ImageView mainPrev;
    Mat grayMat, originalMat, thresh, thresh2, thresh3, thresh4;
    ArcSeekBar contrast, brightness;
    ArrayList<Bitmap> filters = new ArrayList<>();
    ArrayList<RoundedImageView> prev = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

//        ArrayList<String> scan = (ArrayList<String>) getIntent().getSerializableExtra("crops");

        contrast = findViewById(R.id.contrast);
        brightness = findViewById(R.id.brightness);

        int[] colorArray = getResources().getIntArray(R.array.gradient);
        contrast.setProgressBackgroundGradient(colorArray);
        brightness.setProgressBackgroundGradient(colorArray);
        contrast.bringToFront();
        brightness.bringToFront();


        contrast.setOnProgressChangedListener(new ProgressListener() {
            @Override
            public void invoke(int i) {

                float contrastValue = i/10;
                mainPrev.setImageBitmap(ScanAwayUtils.changeBitmapContrastBrightness(testPrev,contrastValue,0));
                Log.i("contrast",String.valueOf(contrastValue));

            }
        });

        brightness.setOnProgressChangedListener(new ProgressListener() {
            @Override
            public void invoke(int i) {
                float brightnessValue = ScanAwayUtils.brightnessConversion(i);
                mainPrev.setImageBitmap(ScanAwayUtils.changeBitmapContrastBrightness(testPrev,1,brightnessValue));
                Log.i("brightness",String.valueOf(brightnessValue));
                Log.i("brightness old",String.valueOf(i));
            }
        });

        mainPrev = findViewById(R.id.main_prev);
        prev.add(findViewById(R.id.filter1));
        prev.add(findViewById(R.id.filter2));
        prev.add(findViewById(R.id.filter3));
        prev.add(findViewById(R.id.filter4));
        prev.add(findViewById(R.id.filter5));
        prev.add(findViewById(R.id.filter6));


//        File file = new File(scan.get(0));
//        Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
//        mainPrev.setImageBitmap(myBitmap);


        for (int i = 0; i < prev.size(); i++) {
            final int current = i;

            prev.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectPrev(current, filters);
                    mainPrev.setImageBitmap(filters.get(current));
                    testPrev = filters.get(current);
                }
            });
        }



        original = BitmapFactory.decodeResource(getResources(), R.drawable.t);
        testPrev = original;
        mainPrev.setImageBitmap(testPrev);
        filters.add(BitmapFactory.decodeResource(getResources(), R.drawable.t));
        grayMat = new Mat();
        originalMat = new Mat();
        thresh = new Mat();
        thresh2 = new Mat();
        thresh3 = new Mat();
        thresh4 = new Mat();


        Utils.bitmapToMat(filters.get(0), originalMat);
        Imgproc.cvtColor(originalMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.adaptiveThreshold(grayMat, thresh, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 15, 15);
        Imgproc.adaptiveThreshold(grayMat, thresh2, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
        Imgproc.threshold(grayMat, thresh3, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        Imgproc.threshold(grayMat, thresh4, 120, 255, Imgproc.THRESH_BINARY_INV);


        filters.add(BitmapFactory.decodeResource(getResources(), R.drawable.t));
        filters.add(BitmapFactory.decodeResource(getResources(), R.drawable.t));
        filters.add(BitmapFactory.decodeResource(getResources(), R.drawable.t));
        filters.add(BitmapFactory.decodeResource(getResources(), R.drawable.t));
        filters.add(BitmapFactory.decodeResource(getResources(), R.drawable.t));

        Utils.matToBitmap(grayMat, filters.get(1));
        Utils.matToBitmap(thresh, filters.get(2));
        Utils.matToBitmap(thresh2, filters.get(3));
        Utils.matToBitmap(thresh3, filters.get(4));
        Utils.matToBitmap(thresh4, filters.get(5));


        prev.get(0).setImageBitmap(filters.get(0));
        prev.get(1).setImageBitmap(filters.get(1));
        prev.get(2).setImageBitmap(filters.get(2));
        prev.get(3).setImageBitmap(filters.get(3));
        prev.get(4).setImageBitmap(filters.get(4));
        prev.get(5).setImageBitmap(filters.get(5));


    }


    public void selectPrev(int selected, ArrayList<Bitmap> bitmap) {
        for (int p = 0; p < 6; p++) {
            if (p == selected) {
                prev.get(p).setBorderColor(getResources().getColor(R.color.soft_orange));
            } else {
                prev.get(p).setBorderColor(getResources().getColor(R.color.transparent));
            }
            prev.get(p).setImageBitmap(bitmap.get(p));

        }
    }

}