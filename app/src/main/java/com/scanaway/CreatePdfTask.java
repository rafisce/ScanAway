package com.scanaway;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class CreatePdfTask extends AsyncTask<String, Integer, File> {

    Context context;
    ArrayList<Bitmap> files;
    ProgressDialog progressDialog;
    String folder;
    String name;


    public CreatePdfTask(Context context2, ArrayList<Bitmap> arrayList, String name) {
        context = context2;
        files = arrayList;
        folder = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/ScanAway";
        this.name = name;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("אנא המתן");
        progressDialog.setMessage("יוצר קובץ pdf...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setCancelable(true);
        progressDialog.show();
    }

    @Override
    protected File doInBackground(String... strings) {

        File folder = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS) +
                File.separator + "ScanAway");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (success) {

            File outputMediaFile = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), "ScanAway/" + name + ".pdf");

            Document document = new Document();
            try {
                PdfWriter.getInstance(document, new FileOutputStream(outputMediaFile));
            } catch (DocumentException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
            document.open();

            int i = 0;
            while (true) {
                if (i < this.files.size()) {
                    try {

                        File f = new File(context.getCacheDir(), "filename");
                        f.createNewFile();

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        files.get(i).compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);
                        byte[] bitmapData = bos.toByteArray();
                        FileOutputStream fos = new FileOutputStream(f);
                        fos.write(bitmapData);
                        fos.flush();
                        fos.close();


                        Image image = Image.getInstance(f.getAbsolutePath());
                        image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_MIDDLE);

                        float scale = Math.min(document.getPageSize().getWidth() / image.getWidth(), document.getPageSize().getHeight() / image.getHeight());


                        float width = image.getWidth() * scale * 0.98f;
                        float height = image.getHeight() * scale * 0.98f;
                        float absoluteX = (document.getPageSize().getWidth()-width) / 2f;
                        float absoluteY = (document.getPageSize().getHeight() - height) / 2f;

//                        Log.i("ss document size",String.valueOf(document.getPageSize().getWidth()+"x"+String.valueOf(document.getPageSize().getHeight())));
//                        Log.i("ss image size",String.valueOf(image.getWidth()+"x"+String.valueOf(image.getHeight())));
//                        Log.i("ss scale",String.valueOf(scale));
//                        Log.i("ss image after",String.valueOf(image.getWidth() * scale)+"x"+String.valueOf(image.getHeight() * scale));
//                        Log.i("ss image after 0.98",String.valueOf(width)+"x"+String.valueOf(height));

                        image.scaleToFit(width, height);
                        image.setAbsolutePosition(absoluteX, absoluteY);
                        document.add(image);
                        document.newPage();
                        publishProgress(i);
                        i++;
                    } catch (BadElementException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                } else {
                    document.close();
                    return outputMediaFile;
                }

            }
            // Do something on success

        } else {
            // Do something else on failure
            return null;
        }

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        this.progressDialog.setProgress(((values[0] + 1) * 100) / this.files.size());
        StringBuilder sb = new StringBuilder();
        sb.append("מאבד תמונות (");
        sb.append(values[0] + 1);
        sb.append("/");
        sb.append(this.files.size());
        sb.append(")");
        progressDialog.setTitle(sb.toString());
    }

    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);
        MainActivity.checkFilterActivity = true;
        MainActivity.savedFile = "הקובץ "+name+".Pdf נשמר ב - "+file.getPath();
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
        ((Activity) context).finish();

    }
}