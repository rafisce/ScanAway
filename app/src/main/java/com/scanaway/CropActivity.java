package com.scanaway;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

public class CropActivity extends AppCompatActivity {


    int imagesCount = 0;
    ImageView confirm, rRight, rLeft, crop;
    CropImageView cropImageView;
    ArrayList<Bitmap> croppedImages = new ArrayList<>();

    static ArrayList<Bitmap> scans;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        confirm = findViewById(R.id.confirm_crop);
        rRight = findViewById(R.id.rotate_right);
        rLeft = findViewById(R.id.rotate_left);
        crop = findViewById(R.id.crop);


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    cropNext();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        rRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageView.rotateImage(90);
            }
        });
        rLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cropImageView.rotateImage(-90);

            }
        });
        crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bitmap cropped = cropImageView.getCroppedImage();
                cropImageView.setImageBitmap(cropped);

            }
        });



        if (scans.size() == 1) {
            confirm.setImageResource(R.drawable.ic_check);
        }
        cropImageView = (CropImageView) findViewById(R.id.cropImageView);
        cropImageView.setAutoZoomEnabled(true);
        cropImageView.setShowProgressBar(true);
        cropImageView.setScaleType(CropImageView.ScaleType.CENTER_CROP);
        cropImageView.setImageBitmap(scans.get(imagesCount));

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void cropNext() throws IOException, InterruptedException {

        if (imagesCount <= scans.size() - 2 && scans.size() != 1) {

            croppedImages.add(cropImageView.getCroppedImage());
            cropImageView.setImageBitmap(scans.get(imagesCount));
            imagesCount++;

            if (imagesCount == scans.size() - 1) {
                confirm.setImageResource(R.drawable.ic_check);
            }

        } else {

            croppedImages.add(cropImageView.getCroppedImage());
            goToFiltering();
        }
    }


    public void goToFiltering() {

        FilterActivity.cropped = croppedImages;
        Intent intent = new Intent(getBaseContext(), FilterActivity.class);
        startActivity(intent);
        finish();


    }


}
