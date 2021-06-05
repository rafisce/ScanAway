package com.scanaway;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

import Adapter.MyRecyclerAdapterMain;
import Helper.MyItemTouchHelperCallback;
import Helper.OnStartDragListener;
import Adapter.Scan;
import butterknife.ButterKnife;
import butterknife.BindView;


public class MainActivity extends AppCompatActivity {


    @BindView(R.id.recycler_view_main)
    RecyclerView recyclerView;
    ItemTouchHelper itemTouchHelper;
    FloatingActionButton scan;
    ProgressDialog dialog;
    ImageButton gallery;
    static boolean checkFilterActivity = false;
    ArrayList<File> fileList = new ArrayList<>();
    ArrayList<Scan> scanList = new ArrayList<>();
    ArrayList<Bitmap> bitmaps = new ArrayList<>();
    int PICK_IMAGE_MULTIPLE = 1;


    String path = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOCUMENTS) +
            File.separator + "ScanAway";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dialog = new ProgressDialog(this);

        fileList = ScanAwayUtils.getAllFiles(path);
        new MyTaskMain(this).execute();

    }


    private void generateItems() {

        MyRecyclerAdapterMain adapter = new MyRecyclerAdapterMain(this, scanList
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            // When an Image is picked
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK
                    && null != data) {

                if (data.getClipData() != null) {
                    ClipData mClipData = data.getClipData();
                    for (int i = 0; i < mClipData.getItemCount(); i++) {

                        ClipData.Item item = mClipData.getItemAt(i);
                        Uri uri = item.getUri();
                        bitmaps.add(MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri));

                    }

                } else {
                    Uri imageUri = data.getData();
                    bitmaps.add(MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri));

                }


                PagingActivity.images = bitmaps;
                Intent intent = new Intent(getBaseContext(), PagingActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(this, "משהו השתבש", Toast.LENGTH_LONG)
                    .show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    private class MyTaskMain extends AsyncTask<Void, Void, Void> {

        Context context;

        public MyTaskMain(Context ctx) {
            this.context = ctx;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            if(!fileList.isEmpty()) {
                for (File f : fileList) {
                    scanList.add(new Scan(ScanAwayUtils.pdfToBitmap(f, getBaseContext(), 1), f.getName().split("_")[0], f));

                }
            }
            return null;
        }

        // Runs in UI before background thread is called
        @Override
        protected void onPreExecute() {

            if (checkFilterActivity) {
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
            if (dialog.isShowing()) {
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

                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "בחר תמונה"), PICK_IMAGE_MULTIPLE);
                }
            });

            init();
            generateItems();
            checkFilterActivity = false;


        }
    }

}