package com.scanaway;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

import Adapter.MyRecyclerAdapter;
import Helper.MyItemTouchHelperCallback;
import Helper.OnStartDragListener;
import Helper.Scan;
import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {


    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    ItemTouchHelper itemTouchHelper;
    FloatingActionButton scan;
    private ProgressDialog dialog;
    ArrayList<File> fileList = new ArrayList<>();
    ArrayList<Scan> scanList = new ArrayList<>();


    String path = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOCUMENTS) +
            File.separator + "ScanAway";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialog = new ProgressDialog(this);
        scan = findViewById(R.id.go_to_scan);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), ScanActivity.class);
                startActivity(intent);
                finish();
            }
        });

        fileList = ScanAwayUtils.getAllFiles(path);
        if(!fileList.isEmpty()) {
            new MyTaskMain().execute();
        }
    }



    private void generateItems() {

        MyRecyclerAdapter adapter = new MyRecyclerAdapter(this, scanList
                , new OnStartDragListener() {
            @Override
            public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                itemTouchHelper.startDrag(viewHolder);
            }
        });

        recyclerView.setAdapter(adapter);
        ItemTouchHelper.Callback callback = new MyItemTouchHelperCallback(adapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void init() {

        ButterKnife.bind(this);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
    }

    private class MyTaskMain extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            for(File f: fileList)
            {

                scanList.add(new Scan(ScanAwayUtils.pdfToBitmap(f,getBaseContext()),f.getName().split("_")[0],f));

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

            init();
            generateItems();
            if(dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }
    
}