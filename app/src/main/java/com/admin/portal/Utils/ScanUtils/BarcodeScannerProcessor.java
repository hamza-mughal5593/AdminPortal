package com.admin.portal.Utils.ScanUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.admin.portal.Utils.SingletonVolley;
import com.admin.portal.Utils.UtilsJava;
import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import io.paperdb.Paper;

public class BarcodeScannerProcessor extends VisionProcessorBase<List<Barcode>> {

    private static final String TAG = "BarcodeProcessor";

    private final BarcodeScanner barcodeScanner;
    //    Activity activity;
    Context context;
    Activity activity;
    String scantype;
    int scancount = 0;


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
        Log.e("1212212121", "onSuccess: " + barcodes.size());
        if (barcodes.isEmpty()) {
            if (scan) {
                Toast.makeText(activity, "No QR/Barcode detected", Toast.LENGTH_SHORT).show();
            }
        } else if (barcodes.size() == 1) {
            Barcode barcode = barcodes.get(0);
            graphicOverlay.add(new BarcodeGraphic(graphicOverlay, barcode));
            scancount++;
            if (scancount == 1)
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

            UtilsJava.PlayVibrate500(activity, 1000);
            UtilsJava.Playsound(activity, "beep.mp3");

            if (scantype.equals("checkin")) {


                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
                String currentDateandTime2 = sdf2.format(new Date());

                check_in(currentDateandTime2);


            } else {
                checkout();
            }


//            Intent intent = new Intent(context, ResultActivity.class);
//                context.startActivity(intent);


        }
    }

    private void check_in(String currentDateandTime2) {


        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://ayadimarble.com/checkin/", new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.i("responseImage", response);

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    int success = jsonObject.getInt("success");
                    if (success == 1) {


                        Paper.book().write("check_in", currentDateandTime2);

                        Toast.makeText(context, "Check-In Successfully", Toast.LENGTH_LONG).show();
                        activity.finish();
                    } else {
                        String msg = jsonObject.getString("message");
                        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();

                    }


                } catch (JSONException e) {
                    Log.e("error_responce", "onResponse: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                try {
                    if (error instanceof NoConnectionError) {

                        Toast.makeText(activity, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    } else {

                        System.out.println("nothing");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("do", "add_employee_data");
                params.put("apikey", "dwamsoft12345");
                params.put("check_in", currentDateandTime2);
                params.put("employ_id", Paper.book().read("id", ""));
                return params;
            }
        };
        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 30000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 30000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
            }
        });
        SingletonVolley.getInstance(activity).addToRequestQueue(stringRequest);
    }

    private void checkout() {


        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://ayadimarble.com/checkin/", new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.i("responseImage", response);

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    int success = jsonObject.getInt("success");
                    if (success == 1) {

                        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
                        String currentDateandTime2 = sdf2.format(new Date());
                        Paper.book().write("check_out", currentDateandTime2);

                        Toast.makeText(context, "Check-Out Successfully", Toast.LENGTH_LONG).show();
                        activity.finish();
                    } else {
                        String msg = jsonObject.getString("message");
                        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();

                    }


                } catch (JSONException e) {
                    Log.e("error_responce", "onResponse: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                try {
                    if (error instanceof NoConnectionError) {

                        Toast.makeText(activity, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    } else {

                        System.out.println("nothing");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("do", "employee_checkout");
                params.put("apikey", "dwamsoft12345");
                params.put("employ_id", Paper.book().read("id", ""));
                return params;
            }
        };
        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 30000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 30000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
            }
        });
        SingletonVolley.getInstance(activity).addToRequestQueue(stringRequest);
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Barcode detection failed " + e);
    }
}

