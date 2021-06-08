package com.scanaway;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import java.util.ArrayList;
import Adapter.MyRecyclerAdapterPaging;
import Helper.MyItemTouchHelperCallback;
import Helper.OnStartDragListener;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PagingActivity extends AppCompatActivity {

    @BindView(R.id.recycler_view_paging)


    RecyclerView recyclerView;
    ImageButton addImages;
    ItemTouchHelper itemTouchHelper;
    ImageButton toCrop;
    MyRecyclerAdapterPaging adapter;
    ArrayList<Bitmap> bitmapsToAdd;
    static ArrayList<Bitmap> images;
    int PICK_IMAGE_MULTIPLE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paging);

        toCrop = findViewById(R.id.go_to_crop);
        addImages = findViewById(R.id.add_photos);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);



        init();
        generateItems();

        toCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropActivity.scans = adapter.getScans();
                Intent intent = new Intent(getBaseContext(), CropActivity.class);
                startActivity(intent);
                finish();
            }
        });

        addImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmapsToAdd = new ArrayList<>();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "בחר תמונה"), PICK_IMAGE_MULTIPLE);
            }
        });
    }

    private void generateItems() {

         adapter = new MyRecyclerAdapterPaging(this, images
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
                        bitmapsToAdd.add(MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri));

                    }

                } else {
                    Uri imageUri = data.getData();
                    bitmapsToAdd.add(MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri));

                }


               adapter.addItems(bitmapsToAdd);
            }
        } catch (Exception e) {
            Toast.makeText(this, "משהו השתבש", Toast.LENGTH_LONG)
                    .show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(this, ScanActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(myIntent);

        return true;
    }

}