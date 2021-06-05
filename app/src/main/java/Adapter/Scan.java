package Adapter;

import android.graphics.Bitmap;


import com.scanaway.ScanAwayUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class Scan {

    Bitmap bitmap;
    String name;
    String date;
    File file;

    public Scan(ArrayList<Bitmap> bitmaps, String name, File file) {
        this.bitmap = ScanAwayUtils.getResizedBitmap(bitmaps.get(0),300);
        this.name = name;
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        this.date = format.format(new Date(file.lastModified()));
        this.file = file;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
