package com.scanaway;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
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
    ImageView scan;
    ProgressDialog dialog;
    ImageView gallery;
    ImageView sortDate, sortAlphabet;
    MyRecyclerAdapterMain adapter;
    static boolean checkFilterActivity = false;
    static String savedFile;
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

        if (PermissionsUtils.allPermissionsGranted(this)) {
            loadActivity();

        }
        else {
            ActivityCompat.requestPermissions(this, PermissionsUtils.REQUIRED_PERMISSIONS, PermissionsUtils.REQUEST_CODE_PERMISSIONS);
        }

    }


    private void generateItems() {

        adapter = new MyRecyclerAdapterMain(this, scanList
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

        @Override
        protected Void doInBackground(Void... voids) {

            if (!fileList.isEmpty()) {
                for (File f : fileList) {
                    scanList.add(new Scan(ScanAwayUtils.pdfToBitmap(f, getBaseContext(), 1), f.getName().split("_")[0], f));

                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {

            if (checkFilterActivity) {
                setTheme(R.style.Theme_ScanAway);
                dialog.setTitle("טוען");
                dialog.setMessage(savedFile);
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
            loadDashboard();

        }
    }

    public void loadDashboard() {

        setContentView(R.layout.activity_main);
        gallery = findViewById(R.id.gallery);
        sortDate = findViewById(R.id.date_sort);
        sortAlphabet = findViewById(R.id.alphabet_sort);
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

        sortDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.byDate();
            }
        });

        sortAlphabet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.byName();
            }
        });

        init();
        generateItems();
        checkFilterActivity = false;
    }

    public void loadActivity() {
        dialog = new ProgressDialog(this);
        fileList = ScanAwayUtils.getAllFiles(path);
        if (!checkFilterActivity) {
            if (!fileList.isEmpty()) {
                for (File f : fileList) {
                    scanList.add(new Scan(ScanAwayUtils.pdfToBitmap(f, getBaseContext(), 1), f.getName().split("_")[0], f));

                }
            }
            setTheme(R.style.Theme_ScanAway);
            loadDashboard();
        } else {
            new MyTaskMain().execute();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PermissionsUtils.REQUEST_CODE_PERMISSIONS) {
            if (PermissionsUtils.allPermissionsGranted(this)) {
                loadActivity();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("לאפליקציה אין הרשאות!")
                        .setMessage("הרשאות לא אושרו על ידי המשתמש.\nאשר הרשאות כדי להשתמש באפליקציה.")
                        .setPositiveButton("להרשאות", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", getPackageName(), null));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setNegativeButton("יציאה", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })

                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
    }
}