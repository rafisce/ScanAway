package com.scanaway;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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

public class CropActivity extends AppCompatActivity {

    ImageView confirm, rRight, rLeft, crop;
    CropImageView cropImageView;
    ArrayList<File> scan;
    ArrayList<Bitmap> croppedImages = new ArrayList<>();
    ArrayList<String> crops = new ArrayList<>();
    int imagesCount = 0;

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
                } catch (IOException e) {
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


        scan = (ArrayList<File>) getIntent().getSerializableExtra("scan");
        if (scan.size() == 1) {
            confirm.setImageResource(R.drawable.ic_check);
        }
        cropImageView = (CropImageView) findViewById(R.id.cropImageView);
        cropImageView.setScaleType(CropImageView.ScaleType.CENTER_CROP);
        cropImageView.setAutoZoomEnabled(true);
        cropImageView.setShowProgressBar(true);
        cropImageView.setImageUriAsync(Uri.fromFile(scan.get(imagesCount)));


    }

    @Override
    public void onBackPressed() {
        // your code.
        finish();
    }

    private void cropNext() throws IOException {

        Log.i("CropActivity", "cropNext " + String.valueOf(imagesCount));

        if (imagesCount <= scan.size() - 2 && scan.size() != 1) {

            Bitmap cropped = cropImageView.getCroppedImage();
            croppedImages.add(cropped);
            imagesCount++;
            cropImageView.setImageUriAsync(Uri.fromFile(scan.get(imagesCount)));

            if (imagesCount == scan.size() - 1) {
                confirm.setImageResource(R.drawable.ic_check);
            }

        } else {

            if (scan.size() == 1) {
                Bitmap cropped = cropImageView.getCroppedImage();
                croppedImages.add(cropped);

            }
            for (Bitmap bm : croppedImages) {
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                String formattedDate = df.format(c.getTime());
                File folder = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM) +
                        File.separator + "ScanAwayTemp");
                boolean success = true;
                if (!folder.exists()) {
                    success = folder.mkdirs();
                } else {

                    ScanAwayUtils.deleteFolderContent(folder);
                }

                if (success) {

                    File dir = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DCIM), "ScanAwayTemp/ScanAway_" + formattedDate + ".jpg");
                    crops.add(dir.getAbsolutePath());
                    try {

                        FileOutputStream out = new FileOutputStream(dir);
                        bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        out.flush();
                        out.close();

                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }
            }
            goToFiltering();
        }
    }


    public void goToFiltering() {
        Intent intent = new Intent(this, FilterActivity.class);
        intent.putExtra("crops", crops);
        startActivity(intent);
        finish();
    }


}
