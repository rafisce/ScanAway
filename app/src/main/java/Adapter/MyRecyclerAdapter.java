package Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.scanaway.FilterActivity;
import com.scanaway.R;
import com.scanaway.ScanActivity;
import com.scanaway.ViewPdf;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import Helper.ItemTouchHelperAdapter;
import Helper.OnStartDragListener;
import Helper.Scan;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.MyViewHolder>
implements ItemTouchHelperAdapter {


    Context ctx;
    ArrayList<Scan> scans;
    OnStartDragListener listener;

    public MyRecyclerAdapter(Context ctx, ArrayList<Scan> scans, OnStartDragListener listener) {
        this.ctx = ctx;
        this.scans = scans;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(ctx).inflate(R.layout.scan_card_item,parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.scanName.setText(scans.get(position).getName());
        holder.scanDate.setText(scans.get(position).getDate());
        holder.scanImg.setImageBitmap(scans.get(position).getBitmap());

        holder.item.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                    listener.onStartDrag(holder);

                return false;
            }
        });

        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(ctx, ViewPdf.class);
                intent.putExtra("path",scans.get(position).getFile().getAbsolutePath());
                ctx.startActivity(intent);

            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ctx)
                        .setTitle("מחיקת סריקה")
                        .setMessage("למחוק את "+scans.get(position).getName()+"?")
                        .setPositiveButton("כן", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                scans.get(position).getFile().delete();
                                removeAt(position);
                                Log.i("position:",String.valueOf(position));
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton("לא", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });


    }
    public void removeAt(int position) {
        scans.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, scans.size());
    }

    @Override
    public int getItemCount() {
        return scans.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(scans,fromPosition,toPosition);
        notifyItemMoved(fromPosition,toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {

        scans.remove(position);
        notifyItemRemoved(position);
    }




    public class MyViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.scan_name)
        TextView scanName;

        @BindView(R.id.scan_date)
        TextView scanDate;

        @BindView(R.id.scan_img)
        ImageView scanImg;

        @BindView(R.id.share_scan)
        ImageButton share;

        @BindView(R.id.delete_scan)
        ImageButton delete;

        @BindView(R.id.item)
        CardView item;

        Unbinder unbinder;

        public MyViewHolder(@NonNull View itemView) {

            super(itemView);

            unbinder = ButterKnife.bind(this,itemView);
        }





    }
}
