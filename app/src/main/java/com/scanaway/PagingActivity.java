package com.scanaway;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import Adapter.MyRecyclerAdapterPaging;
import Helper.MyItemTouchHelperCallback;
import Helper.OnStartDragListener;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PagingActivity extends AppCompatActivity {

    @BindView(R.id.recycler_view_paging)

    RecyclerView recyclerView;
    ItemTouchHelper itemTouchHelper;
    FloatingActionButton toCrop;
    MyRecyclerAdapterPaging adapter;
    static ArrayList<Bitmap> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paging);

        toCrop = findViewById(R.id.go_to_crop);



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
}