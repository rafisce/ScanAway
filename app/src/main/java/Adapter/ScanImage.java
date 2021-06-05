package Adapter;

import android.graphics.Bitmap;

import com.scanaway.ScanAwayUtils;

public class ScanImage {

    Bitmap img;
    int number;

    public ScanImage(Bitmap img, int number) {

        this.img = img;
        this.number = number;
    }

    public Bitmap getImg() {
        return img;
    }

    public void setImg(Bitmap img) {
        this.img = img;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
