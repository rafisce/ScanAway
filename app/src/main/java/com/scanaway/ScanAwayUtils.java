package com.scanaway;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class ScanAwayUtils {

    public static void deleteFolderContent(File folder) {
        if (folder.exists()) {
            String[] children = folder.list();
            for (int i = 0; i < children.length; i++) {
                new File(folder, children[i]).delete();
            }
        }
    }

    public static void savePfd(Context ctx, ArrayList<File> imgList) {
        File folderPath = new File(Environment.getExternalStorageDirectory() + "/YourImagesFolder");
        File[] imageList = folderPath.listFiles();
        ArrayList<File> imagesArrayList = new ArrayList<>();
        for (File absolutePath : imageList) {
            imagesArrayList.add(absolutePath);
        }
        new CreatePdfTask(ctx, imgList).execute();
    }

    public static Bitmap changeBitmapContrastBrightness(Bitmap bmp, float contrast, float brightness)
    {
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

    public static float brightnessConversion(int oldValue)
    {
        float old_value = oldValue;
        Log.i("brightness sta", String.valueOf(old_value));
        float old_min =0;
        float old_max = 100;
        float new_min = -255;
        float new_max = 255;

        float new_value = ( (old_value - old_min) / (old_max - old_min) ) * (new_max - new_min) + new_min;
        return new_value;
    }


}
