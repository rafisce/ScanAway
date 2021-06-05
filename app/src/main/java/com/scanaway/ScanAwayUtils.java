package com.scanaway;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.pdf.PdfRenderer;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_MEAN_C;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

public class ScanAwayUtils {

    public static void deleteFolderContent(File folder) {
        if (folder.exists()) {
            String[] children = folder.list();
            for (int i = 0; i < children.length; i++) {
                new File(folder, children[i]).delete();
            }
        }
    }

    public static void savePfd(Context ctx, ArrayList<Bitmap> imgList, String name) {

        new CreatePdfTask(ctx, imgList, name).execute();

    }

    public static Bitmap changeBitmapContrastBrightness(Bitmap bmp, float contrast, float brightness) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
        Canvas canvas = new Canvas(ret);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);
        return ret;
    }

    public static float brightnessConversion(int oldValue) {
        float old_value = oldValue;

        float old_min = 0;
        float old_max = 1000;
        float new_min = -255;
        float new_max = 255;

        float new_value = ((old_value - old_min) / (old_max - old_min)) * (new_max - new_min) + new_min;
        Log.i("brightness sta", String.valueOf(new_value));
        return new_value;
    }

    public static float contrastConversion(int oldValue) {
        float old_value = oldValue;

        float old_min = 0;
        float old_max = 1000;
        float new_min = 0;
        float new_max = 10;

        float new_value = ((old_value - old_min) / (old_max - old_min)) * (new_max - new_min) + new_min;
        Log.i("contrast sta", String.valueOf(new_value));
        return new_value;
    }

    public static ArrayList<Bitmap> pdfToBitmap(File pdfFile, Context ctx) {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();

        try {
            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY));

            Bitmap bitmap;
            final int pageCount = renderer.getPageCount();
            for (int i = 0; i < pageCount; i++) {
                PdfRenderer.Page page = renderer.openPage(i);

                int width = ctx.getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
                int height = ctx.getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                bitmaps.add(bitmap);

                // close the page
                page.close();

            }

            // close the renderer
            renderer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return bitmaps;

    }

    public static ArrayList<File> getAllFiles(String path) {
        ArrayList<File> result = new ArrayList<File>(); //ArrayList cause you don't know how many files there is
        File folder = new File(path);

        if (folder.exists()) {//This is just to cast to a File type since you pass it as a String
            File[] filesInFolder = folder.listFiles(); // This returns all the folders and files in your path
            for (File file : filesInFolder) { //For each of the entries do:
                if (!file.isDirectory()) { //check that it's not a dir
                    result.add(file); //push the filename as a string
                }
            }
        }
        return result;
    }

    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public static ArrayList<Bitmap> getBitmapsFromFiles(ArrayList<String> scan) {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        for (String s : scan) {
            File file = new File(s);
            bitmaps.add(BitmapFactory.decodeFile(file.getAbsolutePath()));
        }
        return bitmaps;
    }

    public static ArrayList<File> bitmapsToFiles(Context ctx, ArrayList<Bitmap> bitmaps,String dirName) throws IOException {

        ArrayList<File> files = new ArrayList<>();

        boolean checkContentDelete = true;
        for (Bitmap bm : bitmaps) {

            File dir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM) +
                    File.separator + dirName);
            boolean success = true;

            if (!dir.exists()) {
                success = dir.mkdirs();
            } else if (checkContentDelete) {
                ScanAwayUtils.deleteFolderContent(dir);
                checkContentDelete = false;
            }

            if (success) {

                File file = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM), dirName + "/ScanAway_" + UUID.randomUUID().toString() + ".jpg");
                try {

                    FileOutputStream out = new FileOutputStream(file);
                    bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.flush();
                    out.close();
                    files.add(file);

                } catch (Exception e) {
                    e.printStackTrace();

                }


            }

        }
        return files;
    }

    public static Bitmap filter(Bitmap bitmap, String filter) {
        Bitmap bitmap1 = bitmap;
        Mat originalMat, grayMat, thresh, thresh2, thresh3, thresh4;

        originalMat = new Mat();
        grayMat = new Mat();
        thresh = new Mat();
        thresh2 = new Mat();
        thresh3 = new Mat();
        thresh4 = new Mat();

        Log.i("bitmap second", String.valueOf(bitmap1));
        Utils.bitmapToMat(bitmap1, originalMat);

        Imgproc.cvtColor(originalMat, grayMat, Imgproc.COLOR_BGR2GRAY);


        switch (filter) {


            case "filter1": {

                Utils.matToBitmap(grayMat, bitmap1);
                return bitmap1;
            }
            case "filter2": {
                Imgproc.adaptiveThreshold(grayMat, thresh, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 15, 15);
                Utils.matToBitmap(thresh, bitmap1);
                return bitmap1;
            }
            case "filter3": {
                Imgproc.adaptiveThreshold(grayMat, thresh2, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
                Utils.matToBitmap(thresh2, bitmap1);
                return bitmap1;
            }
            case "filter4": {
                Imgproc.threshold(grayMat, thresh3, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
                Utils.matToBitmap(thresh3, bitmap1);
                return bitmap1;
            }
            case "filter5": {
                Imgproc.threshold(grayMat, thresh4, 120, 255, Imgproc.THRESH_BINARY_INV);
                Utils.matToBitmap(thresh4, bitmap1);
                return bitmap1;
            }
            default:
                return bitmap;
        }

    }

    public static Bitmap filteredResult(Bitmap bitmap, int filter, int contrast, int brightness) {

        float actualContrast = contrastConversion(contrast);
        float actualBrightness = brightnessConversion(brightness);
        Bitmap temp = ScanAwayUtils.filter(bitmap, "filter" + String.valueOf(filter));
        return ScanAwayUtils.changeBitmapContrastBrightness(temp, actualContrast, actualBrightness);

    }

    public static Bitmap rotateBitmap(Bitmap bitmap,int degree)
    {
        Matrix matrix = new Matrix();

        matrix.postRotate(degree);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

        return rotatedBitmap;

    }



    public static void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }



}
