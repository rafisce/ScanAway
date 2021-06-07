package com.scanaway;

import androidx.appcompat.app.AppCompatActivity;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

public class ViewPdf extends AppCompatActivity {

    PDFView pdfView;
    ProgressDialog dialog;
    String path;
    File pdf;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pdf);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        dialog = new ProgressDialog(this);

        new MyTaskViewPdf().execute();
        pdfView = findViewById(R.id.pdfView);

    }

    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(this, MainActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(myIntent);

        return true;
    }

    private class MyTaskViewPdf extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            path = getIntent().getStringExtra("path");
            Log.i("ppp",path);
            pdf = new File(path);
            while(!pdf.exists())
            {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        // Runs in UI before background thread is called
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setTitle("טוען");
            dialog.setMessage("אנא המתן");
            dialog.setCancelable(false);
            dialog.show();
            // Do something like display a progress bar
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(dialog.isShowing()) {
                dialog.dismiss();
            }
            pdfView.fromFile(pdf).spacing(10).load();
        }
    }
}