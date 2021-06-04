package com.scanaway;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.util.ArrayList;
import Adapter.MyRecyclerAdapter;
import Helper.MyItemTouchHelperCallback;
import Helper.OnStartDragListener;
import Helper.Scan;
import butterknife.ButterKnife;
import butterknife.BindView;


public class MainActivity extends AppCompatActivity {


    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    ItemTouchHelper itemTouchHelper;
    FloatingActionButton scan;
    ProgressDialog dialog;
    ImageButton gallery;
    static boolean checkFilterActivity = false;
    ArrayList<File> fileList = new ArrayList<>();
    ArrayList<Scan> scanList = new ArrayList<>();


    String path = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOCUMENTS) +
            File.separator + "ScanAway";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dialog = new ProgressDialog(this);

        fileList = ScanAwayUtils.getAllFiles(path);
        if(!fileList.isEmpty()) {
            new MyTaskMain(this).execute();
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

        Context context;

        public MyTaskMain(Context ctx) {
            this.context = ctx;
        }

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

            if(checkFilterActivity){
                setTheme(R.style.Theme_ScanAway);
                dialog.setTitle("טוען");
                dialog.setMessage("אנא המתן");
                dialog.show();
            }
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            super.onPostExecute(aVoid);
            if(dialog.isShowing())
            {
                dialog.dismiss();
            }
            setTheme(R.style.Theme_ScanAway);
            getWindow().setBackgroundDrawableResource(R.drawable.clean_background);
            setContentView(R.layout.activity_main);

            gallery = findViewById(R.id.gallery);
            scan = findViewById(R.id.go_to_scan);
            scan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getBaseContext(), ScanActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            gallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            init();
            generateItems();
            checkFilterActivity = false;



        }
    }

}