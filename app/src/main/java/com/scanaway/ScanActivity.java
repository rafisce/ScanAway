package com.scanaway;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import static org.opencv.imgproc.Imgproc.floodFill;

public class ScanActivity extends AppCompatActivity {

    private int REQUEST_CODE_PERMISSIONS = 101;
    private String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};



    int count = 0;
    ImageButton check;
    ImageView lastPage;
    TextView imageCount;
    TextureView textureView;
    FloatingActionButton capture;
    ArrayList<Bitmap> scans = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);


        textureView = findViewById(R.id.view_finder);
        imageCount = findViewById(R.id.pages_count);
        lastPage = findViewById(R.id.last_page);
        check = findViewById(R.id.check);
        capture = findViewById(R.id.capture);

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToEdit();
            }
        });

        imageCount.setVisibility(View.INVISIBLE);
        lastPage.setVisibility(View.INVISIBLE);
        if (allPermissionsGranted()) {
            startCamera(); //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);

        }

    }

    private void startCamera() {
        //make sure there isn't another camera instance running before starting
        CameraX.unbindAll();

        /* start preview */
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        int aspRatioW = ((int) metrics.widthPixels); //get width of scree
        int aspRatioH = ((int) metrics.heightPixels); //get height
        Rational asp = new Rational(aspRatioW, aspRatioH); //aspect ratio
        Size screen = new Size(aspRatioW, aspRatioH); //size of the screen

        //config obj for preview/viewfinder thingy.

        PreviewConfig pConfig = new PreviewConfig.Builder().setTargetAspectRatio(asp).setTargetResolution(screen).build();
        Preview preview = new Preview(pConfig); //lets build it

        preview.setOnPreviewOutputUpdateListener(
                new Preview.OnPreviewOutputUpdateListener() {
                    //to update the surface texture we have to destroy it first, then re-add it
                    @Override
                    public void onUpdated(Preview.PreviewOutput output) {
                        ViewGroup parent = (ViewGroup) textureView.getParent();
                        parent.removeView(textureView);
                        parent.addView(textureView, 0);
                        textureView.setSurfaceTexture(output.getSurfaceTexture());
                        updateTransform();
                    }
                });

        /* image capture */

        //config obj, selected capture mode
        ImageCaptureConfig imgCapConfig = new ImageCaptureConfig.Builder().setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY).setTargetAspectRatio(asp)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();
        final ImageCapture imgCap = new ImageCapture(imgCapConfig);

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                File folder = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM) +
                        File.separator + "ScanAway");
                boolean success = true;
                if (!folder.exists()) {
                    success = folder.mkdirs();
                }
                if (success) {
                    File dir = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DCIM), "ScanAway/ScanAway_" + UUID.randomUUID().toString() + ".jpg");
                    imgCap.takePicture(dir, new ImageCapture.OnImageSavedListener() {
                        @Override
                        public void onImageSaved(@NonNull File file) {
                            String msg = "Photo capture succeeded: " + file.getAbsolutePath();
//                            galleryAddPic(file.getAbsolutePath());
                            Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
                            trackPhotos(file);
                        }

                        @Override
                        public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                            String msg = "Photo capture failed: " + message;
                            Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
                            if (cause != null) {
                                cause.printStackTrace();
                            }
                        }
                    });
                }
            }
        });

        /* image analyser */

        ImageAnalysisConfig imgAConfig = new ImageAnalysisConfig.Builder().setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE).build();
        ImageAnalysis analysis = new ImageAnalysis(imgAConfig);

        analysis.setAnalyzer(
                new ImageAnalysis.Analyzer() {
                    @Override
                    public void analyze(ImageProxy image, int rotationDegrees) {
                        //y'all can add code to analyse stuff here idek go wild.
                    }
                });

        //bind to lifecycle:
        CameraX.bindToLifecycle((LifecycleOwner) this, analysis, imgCap, preview);
    }

    private void updateTransform() {
        /*
         * compensates the changes in orientation for the viewfinder, bc the rest of the layout stays in portrait mode.
         * methinks :thonk:
         * imgCap does this already, this class can be commented out or be used to optimise the preview
         */
        Matrix mx = new Matrix();
        float w = textureView.getMeasuredWidth();
        float h = textureView.getMeasuredHeight();

        float centreX = w / 2f; //calc centre of the viewfinder
        float centreY = h / 2f;

        int rotationDgr;
        int rotation = (int) textureView.getRotation(); //cast to int bc switches don't like floats

        switch (rotation) { //correct output to account for display rotation
            case Surface.ROTATION_0:
                rotationDgr = 0;
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }

        mx.postRotate((float) rotationDgr, centreX, centreY);
        textureView.setTransform(mx); //apply transformations to textureview
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //start camera when permissions have been granted otherwise exit app
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted() {
        //check if req permissions have been granted
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

//    private void galleryAddPic(String path) {
//        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        File f = new File(path);
//        Uri contentUri = Uri.fromFile(f);
//        mediaScanIntent.setData(contentUri);
//        this.sendBroadcast(mediaScanIntent);
//    }

    private void trackPhotos(File file) {

        count++;
        if (file.exists()) {

            Bitmap scan = BitmapFactory.decodeFile(file.getAbsolutePath());
            scans.add(scan);
            lastPage.setImageBitmap(ScanAwayUtils.rotateBitmap(scan,90));
            imageCount.setText(String.valueOf(count));
            imageCount.setVisibility(View.VISIBLE);
            lastPage.setAlpha(255);
            lastPage.setVisibility(View.VISIBLE);
            (new Handler()).postDelayed(this::setImageAlpha, 2500);

        }
    }

    private void setImageAlpha() {
        lastPage.setAlpha(50);
    }


    private void goToEdit() {
        if (!scans.isEmpty()) {
            CropActivity.scans = scans;
            Intent intent = new Intent(this, CropActivity.class);
            startActivity(intent);
            finish();
        }
    }
}