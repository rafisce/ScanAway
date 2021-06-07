

package com.scanaway;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.marcinmoskala.arcseekbar.ArcSeekBar;
import com.marcinmoskala.arcseekbar.ProgressListener;
import org.opencv.android.OpenCVLoader;
import java.io.IOException;
import java.util.ArrayList;

public class FilterActivity extends AppCompatActivity {

    private static String TAG = "FilterActivity";

    static {

        if (OpenCVLoader.initDebug()) {

            Log.i(TAG, "opencv true");
        } else {
            Log.i(TAG, "opencv false");
        }
    }

    ProgressDialog dialog;
    TextView brightnessPercentage,contrastPercentage;
    int checkFilter = 0;
    Bitmap original;
    Bitmap testPrev;
    ImageView mainPrev;
    ArcSeekBar contrast, brightness;
    ArrayList<Bitmap> filters = new ArrayList<>();
    ArrayList<RoundedImageView> prev = new ArrayList<>();

    static ArrayList<Bitmap> cropped;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        dialog = new ProgressDialog(this);
        new MyTaskFilter().execute();
        contrast = findViewById(R.id.contrast);
        brightness = findViewById(R.id.brightness);
        contrastPercentage = findViewById(R.id.percentage_c);
        brightnessPercentage = findViewById(R.id.percentage_b);



        int[] colorArray = getResources().getIntArray(R.array.purple_gradient);
        contrast.setProgressBackgroundGradient(colorArray);
        brightness.setProgressBackgroundGradient(colorArray);
        contrast.bringToFront();
        brightness.bringToFront();


        contrast.setOnProgressChangedListener(new ProgressListener() {
            @Override
            public void invoke(int i) {
                if(i!=100) {
                    contrastPercentage.setText(String.valueOf(i / 10) + "%");
                    contrastPercentage.setVisibility(View.VISIBLE);
                }
                float contrastValue = ScanAwayUtils.contrastConversion(i);
                mainPrev.setImageBitmap(ScanAwayUtils.changeBitmapContrastBrightness(testPrev, contrastValue, 0));
                (new Handler()).postDelayed(this::percentageVisibilityOff, 2000);
            }

            private void percentageVisibilityOff() {
                contrastPercentage.setVisibility(View.INVISIBLE);
            }
        });

        brightness.setOnProgressChangedListener(new ProgressListener() {
            @Override
            public void invoke(int i) {
                if(i!=500) {
                    brightnessPercentage.setText(String.valueOf(i / 10) + "%");
                    brightnessPercentage.setVisibility(View.VISIBLE);
                }
                float brightnessValue = ScanAwayUtils.brightnessConversion(i);
                mainPrev.setImageBitmap(ScanAwayUtils.changeBitmapContrastBrightness(testPrev, 1, brightnessValue));
                (new Handler()).postDelayed(this::percentageVisibilityOff, 2000);
            }

            private void percentageVisibilityOff() {
                brightnessPercentage.setVisibility(View.INVISIBLE);
            }
        });



        mainPrev = findViewById(R.id.main_prev);
        prev.add(findViewById(R.id.filter1));
        prev.add(findViewById(R.id.filter2));
        prev.add(findViewById(R.id.filter3));
        prev.add(findViewById(R.id.filter4));
        prev.add(findViewById(R.id.filter5));
        prev.add(findViewById(R.id.filter6));



        original = cropped.get(0);

        for (int i = 0; i < prev.size(); i++) {
            final int current = i;
            prev.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    contrast.setProgress(100);
                    brightness.setProgress(500);
                    selectPrev(current, filters);
                    checkFilter = current;
                    mainPrev.setImageBitmap(filters.get(current));
                    testPrev = filters.get(current);

                }
            });
        }



        testPrev = original;
        mainPrev.setImageBitmap(testPrev);

        for (int pre = 0; pre < 6; pre++) {
            filters.add(ScanAwayUtils.filter(original.copy(original.getConfig(),true), "filter" + String.valueOf(pre)));
            if (pre == 0) {
                prev.get(pre).setBorderColor(getResources().getColor(R.color.soft_orange));
            }
            prev.get(pre).setImageBitmap(filters.get(pre));
        }


    }


    public void selectPrev(int selected, ArrayList<Bitmap> bitmap) {
        for (int p = 0; p < 6; p++) {
            if (p == selected) {
                prev.get(p).setBorderColor(getResources().getColor(R.color.soft_orange));
            } else {
                prev.get(p).setBorderColor(getResources().getColor(R.color.transparent));
            }
            prev.get(p).setImageBitmap(bitmap.get(p));

        }
    }

    public void saveScan() throws IOException {


        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        for(Bitmap bitmap: cropped)
        {
            bitmaps.add(ScanAwayUtils.filteredResult(bitmap,checkFilter,contrast.getProgress(),brightness.getProgress()));

        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("שמירת סריקה");
        builder.setMessage("הכנס שם לקובץ הסריקה");
        builder.setCancelable(true);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT );
        builder.setView(input);
        builder.setPositiveButton("אישור", null);
        builder.setNegativeButton("ביטול", null);
        AlertDialog dialog = builder.show();
        Button pos = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button neg = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        pos.setOnClickListener(view1 -> {
            String fileName = input.getText().toString().trim();
            if(TextUtils.isEmpty(fileName)){
                input.setError("אנא הכנס שם לקובץ הסריקה");
            } else {
                ScanAwayUtils.savePfd(this,bitmaps,fileName);
                dialog.dismiss();
            }
        });

        neg.setOnClickListener(view1 -> {
                dialog.cancel();
        });
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.action_save) {
            try {
                saveScan();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class MyTaskFilter extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            while(cropped.contains(null))
            {

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
        }
    }

}
