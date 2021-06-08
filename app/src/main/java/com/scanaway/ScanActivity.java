package com.scanaway;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraXConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class ScanActivity extends AppCompatActivity implements CameraXConfig.Provider {


    PreviewView previewView;
    Preview preview;
    ProcessCameraProvider cameraProvider;
    ImageCapture imageCapture;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    Rational asp;
    Size screen;
    int aspRatioW;
    int aspRatioH;
    DisplayMetrics metrics;
    int count = 0;
    ImageButton check, flash;
    ImageView lastPage;
    TextView imageCount;
    TextureView textureView;
    FloatingActionButton capture;
    ArrayList<Bitmap> scans = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        previewView = findViewById(R.id.preview);

        loadActivity();



    }


    private void startCamera() {
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imageCapture.takePicture(ContextCompat.getMainExecutor(getBaseContext()), new ImageCapture.OnImageCapturedCallback() {

                    @Override
                    public void onCaptureSuccess(ImageProxy imageProxy) {
                        Log.i("image proxy", String.valueOf(imageProxy.getHeight()) + " " + String.valueOf(imageProxy.getWidth()));
                        Log.i("screen", String.valueOf(String.valueOf(screen)));
                        Log.i("aspect ratio", String.valueOf(aspRatioW)+" "+String.valueOf(aspRatioH));
                        Log.i("screen physical", String.valueOf(metrics.widthPixels)+" "+String.valueOf(metrics.heightPixels));
                        Log.i("preview view", String.valueOf(previewView.getWidth())+" "+String.valueOf(previewView.getHeight()));


                        trackPhotos(Bitmap.createScaledBitmap(ScanAwayUtils.getBitmap(imageProxy), 1955, aspRatioW, true));

                        imageProxy.close();
                    }

                    @Override
                    public void onError(ImageCaptureException exception) {
                        Toast.makeText(getBaseContext(),"צילום תמונה השתבש",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


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

    private void trackPhotos(Bitmap scan) {

        count++;
        if (scan != null) {

            scan = ScanAwayUtils.rotateBitmap(scan, 90);
            scans.add(scan);
            lastPage.setImageBitmap(scan);
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
            PagingActivity.images = scans;
            Intent intent = new Intent(this, PagingActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //start camera when permissions have been granted otherwise exit app
        if (requestCode == PermissionsUtils.REQUEST_CODE_PERMISSIONS) {
            if (PermissionsUtils.allPermissionsGranted(this)) {
                startCamera();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("לאפליקציה אין הרשאות!")
                        .setMessage("הרשאות לא אושרו על ידי המשתמש.\nאשר הרשאות כדי להשתמש באפליקציה.")
                        .setPositiveButton("להרשאות", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", getPackageName(), null));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton("יציאה", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })

                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                finish();
            }
        }
    }


    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        preview = new Preview.Builder().setTargetResolution(screen)
                .build();

        imageCapture =
                new ImageCapture.Builder()
                        .setFlashMode(ImageCapture.FLASH_MODE_OFF)
                        .setTargetResolution(screen)
                        .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector,imageCapture, preview);
    }

    public void loadActivity()
    {
        imageCount = findViewById(R.id.pages_count);
        lastPage = findViewById(R.id.last_page);
        check = findViewById(R.id.check);
        flash = findViewById(R.id.flash);
        capture = findViewById(R.id.capture);
        metrics = getResources().getDisplayMetrics();
        aspRatioW =((int) metrics.widthPixels);
        aspRatioH = ((int) metrics.heightPixels); //get height
        asp = new Rational(aspRatioW, aspRatioH); //aspect ratio
        screen = new Size(aspRatioW, aspRatioH); //size of the screen
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(getBaseContext()));
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToEdit();
            }
        });

        flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageCapture.getFlashMode() == ImageCapture.FLASH_MODE_OFF) {
                    imageCapture.setFlashMode(ImageCapture.FLASH_MODE_ON);
                    flash.setImageDrawable(getResources().getDrawable(R.drawable.flash_on));
                } else if (imageCapture.getFlashMode() == ImageCapture.FLASH_MODE_ON) {
                    imageCapture.setFlashMode(ImageCapture.FLASH_MODE_AUTO);
                    flash.setImageDrawable(getResources().getDrawable(R.drawable.flash_auto));
                } else {
                    imageCapture.setFlashMode(ImageCapture.FLASH_MODE_OFF);
                    flash.setImageDrawable(getResources().getDrawable(R.drawable.flash_off));
                }
            }
        });

        imageCount.setVisibility(View.INVISIBLE);
        lastPage.setVisibility(View.INVISIBLE);
        if (PermissionsUtils.allPermissionsGranted(getBaseContext())) {
            startCamera(); //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(ScanActivity.this, PermissionsUtils.REQUIRED_PERMISSIONS, PermissionsUtils.REQUEST_CODE_PERMISSIONS);

        }
    }



}