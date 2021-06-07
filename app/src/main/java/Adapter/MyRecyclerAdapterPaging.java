package Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.scanaway.R;
import com.scanaway.ScanAwayUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import Helper.ItemTouchHelperAdapter;
import Helper.OnStartDragListener;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyRecyclerAdapterPaging extends RecyclerView.Adapter<MyRecyclerAdapterPaging.MyViewHolder>
        implements ItemTouchHelperAdapter {


    Context ctx;
    ArrayList<Bitmap> scans;
    OnStartDragListener listener;


    public MyRecyclerAdapterPaging(Context ctx, ArrayList<Bitmap> scans, OnStartDragListener listener) {
        this.ctx = ctx;
        this.scans = scans;
        this.listener = listener;

    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(ctx).inflate(R.layout.page_item, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {


        holder.pageNumber.setText(String.valueOf(position + 1));
        holder.pageImg.setImageBitmap(scans.get(position));


        holder.item.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                final int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    listener.onStartDrag(holder);

                }
                return false;
            }
        });


        holder.rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                scans.set(position, ScanAwayUtils.rotateBitmap(scans.get(position),90));
                notifyItemChanged(position);
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ctx)
                        .setTitle("מחיקת סריקה")
                        .setMessage("למחוק את עמוד" + String.valueOf(position + 1) + " ? ")
                        .setPositiveButton("כן", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                removeAt(position);
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton("לא", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

    }

    public ArrayList<Bitmap> getScans() {
        return scans;
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
        Collections.swap(scans, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);

        return true;
    }


    @Override
    public void onItemDismiss(int position) {

        scans.remove(position);
        notifyItemRemoved(position);

    }

    @Override
    public void onSwapped() {
        notifyDataSetChanged();

    }

    public void addItems(ArrayList<Bitmap> bitmapsToAdd){

        for(Bitmap bitmapToAdd:bitmapsToAdd)
        {
            scans.add(bitmapToAdd);
        }

        notifyDataSetChanged();
    }






    public class MyViewHolder extends RecyclerView.ViewHolder {


        @BindView(R.id.page_number)
        TextView pageNumber;

        @BindView(R.id.page_img)
        ImageView pageImg;

        @BindView(R.id.rotate_page)
        ImageButton rotate;


        @BindView(R.id.delete_page)
        ImageButton delete;


        @BindView(R.id.page_item)
        CardView item;

        Unbinder unbinder;

        public MyViewHolder(@NonNull View itemView) {

            super(itemView);

            unbinder = ButterKnife.bind(this, itemView);


        }


    }
}
