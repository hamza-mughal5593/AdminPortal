package com.admin.portal.Utils.ScanUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.admin.portal.Utils.UtilsJava;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;

public class BarcodeScannerProcessor extends VisionProcessorBase<List<Barcode>> {

    private static final String TAG = "BarcodeProcessor";

    private final BarcodeScanner barcodeScanner;
    //    Activity activity;
    Context context;
    Activity activity;
    String scantype;



    public BarcodeScannerProcessor(Context context, Activity activity, String scantype) {
        super(context);
        this.context = context;
        this.activity = activity;
        this.scantype = scantype;
//        this.activity = activity;
        // Note that if you know which format of barcode your app is dealing with, detection will be
        // faster to specify the supported barcode formats one by one, e.g.
        // new BarcodeScannerOptions.Builder()
        //     .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        //     .build();
        barcodeScanner = BarcodeScanning.getClient();
//        loadInterFirst();

    }



    @Override
    public void stop() {
        super.stop();
        barcodeScanner.close();
    }

    @Override
    protected Task<List<Barcode>> detectInImage(InputImage image) {
        return barcodeScanner.process(image);
    }

    @Override
    protected void onSuccess(@NonNull List<Barcode> barcodes, @NonNull GraphicOverlay graphicOverlay, boolean scan) {
        Log.e("1212212121", "onSuccess: " +barcodes.size());
        if (barcodes.isEmpty()) {
            if (scan){
                Toast.makeText(activity, "No QR/Barcode detected", Toast.LENGTH_SHORT).show();
            }
        } else if (barcodes.size() == 1) {
            Barcode barcode = barcodes.get(0);
            graphicOverlay.add(new BarcodeGraphic(graphicOverlay, barcode));
            logExtrasForTesting(barcode);

        }
    }

    private void logExtrasForTesting(Barcode barcode) {
        if (barcode != null) {
            Log.v(
                    MANUAL_TESTING_LOG,
                    String.format(
                            "Detected barcode's bounding box: %s", barcode.getBoundingBox().flattenToString()));
            Log.v(
                    MANUAL_TESTING_LOG,
                    String.format(
                            "Expected corner point size is 4, get %d", barcode.getCornerPoints().length));
            for (Point point : barcode.getCornerPoints()) {
                Log.v(
                        MANUAL_TESTING_LOG,
                        String.format("Corner point is located at: x = %d, y = %d", point.x, point.y));
            }
            Log.v(MANUAL_TESTING_LOG, "barcode display value: " + barcode.getDisplayValue());
            Log.v(MANUAL_TESTING_LOG, "barcode raw value: " + barcode.getRawValue());

            String string = barcode.getDisplayValue();


            String data = "";
            String type = "Text";
            String format = "";

            if (barcode.getFormat() == Barcode.FORMAT_QR_CODE) {
                format = "QR Code";
            } else {
                format = "BarCode";
            }


            if (string != null) {
                type = "Text";
                data = string;
            }



//            Paper.init(context);
//            Paper.book().write("type", type);
//            Paper.book().write("data", data);
//            Paper.book().write("barcode", barcode);
//            Paper.book().write("format", format);

            UtilsJava.PlayVibrate500(activity,1000);
            UtilsJava.Playsound(activity, "beep.mp3");

            if (scantype.equals("checkin")){

                Toast.makeText(context, "Check-In Successfully", Toast.LENGTH_LONG).show();
                SimpleDateFormat sdf =new SimpleDateFormat("hh:mm a", Locale.getDefault());
                String currentDateandTime = sdf.format(new Date());

                Paper.book().write("check_in",currentDateandTime);
            }else {
                Toast.makeText(context, "Check-Out Successfully", Toast.LENGTH_LONG).show();
            }
            activity.finish();

//            Intent intent = new Intent(context, ResultActivity.class);
//                context.startActivity(intent);


        }
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Barcode detection failed " + e);
    }
}

