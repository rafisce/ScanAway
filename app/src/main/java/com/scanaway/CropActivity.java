package com.scanaway;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.OnTouch;

public class CropActivity extends AppCompatActivity {


    int imagesCount = 0;
    Bitmap cropped;
    ImageButton confirm, rRight, rLeft, crop;
    CropImageView cropImageView;
    ConstraintLayout cropActions;
    ArrayList<Bitmap> croppedImages = new ArrayList<>();

    static ArrayList<Bitmap> scans;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        cropActions = findViewById(R.id.crop_actions);
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
                scans.set(imagesCount,ScanAwayUtils.rotateBitmap(scans.get(imagesCount),90));
                cropImageView.setImageBitmap(scans.get(imagesCount));
            }
        });
        rLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                scans.set(imagesCount,ScanAwayUtils.rotateBitmap(scans.get(imagesCount),-90));
                cropImageView.setImageBitmap(scans.get(imagesCount));
            }
        });
        crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cropped = cropImageView.getCroppedImage();
                scans.set(imagesCount,cropped);
                cropImageView.setImageBitmap(cropped);

            }
        });


        if (scans.size() == 1) {
            confirm.setImageResource(R.drawable.ic_check);
        }
        cropImageView = findViewById(R.id.cropImageView);
        cropImageView.setAutoZoomEnabled(true);
        cropImageView.setShowProgressBar(true);
        cropImageView.setImageBitmap(scans.get(imagesCount));

        cropImageView.setOnSetCropOverlayMovedListener(new CropImageView.OnSetCropOverlayMovedListener() {
            @Override
            public void onCropOverlayMoved(Rect rect) {

                cropActions.setVisibility(View.INVISIBLE);
            }


        });

        cropImageView.setOnSetCropOverlayReleasedListener(new CropImageView.OnSetCropOverlayReleasedListener() {
            @Override
            public void onCropOverlayReleased(Rect rect) {
                cropActions.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void cropNext() throws IOException, InterruptedException {

        if (imagesCount < scans.size() - 1 && scans.size() != 1) {

            imagesCount++;
            cropImageView.setImageBitmap(scans.get(imagesCount));


            if (imagesCount == scans.size() - 1) {
                confirm.setImageResource(R.drawable.ic_check);
            }

        } else {

            croppedImages.add(cropImageView.getCroppedImage());
            goToFiltering();
        }
    }


    public void goToFiltering() {

        FilterActivity.cropped = scans;
        Intent intent = new Intent(getBaseContext(), FilterActivity.class);
        startActivity(intent);
        finish();


    }


}
