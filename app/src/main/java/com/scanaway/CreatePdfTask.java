package com.scanaway;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class CreatePdfTask extends AsyncTask<String, Integer, File> {
    Context context;
    ArrayList<File> files;
    ProgressDialog progressDialog;
    String folder;
    String name;


    public CreatePdfTask(Context context2, ArrayList<File> arrayList,String name) {
        context = context2;
        files = arrayList;
        folder = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/ScanAway";
        this.name=name;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait...");
        progressDialog.setMessage("Creating pdf...");
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
                    Environment.DIRECTORY_DOCUMENTS), "ScanAway/"+name+".pdf");

            Document document = new Document(PageSize.A4, 0, 0, 0, 0);
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
                        Bitmap bitmap = BitmapFactory.decodeFile(files.get(i).getAbsolutePath());
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);
                        byte[] bitmapData = bos.toByteArray();
                        FileOutputStream fos = new FileOutputStream(f);
                        fos.write(bitmapData);
                        fos.flush();
                        fos.close();


                        Image image = Image.getInstance(f.getAbsolutePath());

                        float scalar = ((document.getPageSize().getWidth() - document.leftMargin()
                                - document.rightMargin() - 0) / image.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
                        image.scalePercent(scalar);
                        image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);
                        image.setAbsolutePosition((document.getPageSize().getWidth() - image.getScaledWidth()) / 2.0f,
                                (document.getPageSize().getHeight() - image.getScaledHeight()) / 2.0f);

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
                    ScanAwayUtils.deleteRecursive(new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DCIM) +
                            File.separator + "ScanAway"));
                    ScanAwayUtils.deleteRecursive(new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DCIM) +
                            File.separator + "ScanAwayTemp"));
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
        progressDialog.dismiss();

        Toast.makeText(context, "Pdf נשמר ב - " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
        ((Activity)context).finish();

    }
}