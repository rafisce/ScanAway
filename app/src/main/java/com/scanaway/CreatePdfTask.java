package com.scanaway;

import android.app.ProgressDialog;
import android.content.Context;
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


    public CreatePdfTask(Context context2, ArrayList<File> arrayList) {
        context = context2;
        files = arrayList;
        folder = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/ScanAway";

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
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        File folder = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS) +
                File.separator + "ScanAway");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (success) {

            File outputMediaFile = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), "ScanAway/ScanAway_" + formattedDate + ".pdf");

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
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
                        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                        byte[] bitmapdata = bos.toByteArray();
                        FileOutputStream fos = new FileOutputStream(f);
                        fos.write(bitmapdata);
                        fos.flush();
                        fos.close();


                        Image image = Image.getInstance(f.getAbsolutePath());

                        float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                                - document.rightMargin() - 0) / image.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
                        image.scalePercent(scaler);
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
        sb.append("Processing images (");
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
        Toast.makeText(context, "Pdf store at " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
    }
}