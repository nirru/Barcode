package com.oxilo.barcode;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oxilo.barcode.Pojo.CustomRequest;
import com.oxilo.barcode.Pojo.ErrorModal;
import com.oxilo.barcode.activity.LoginActivity;
import com.oxilo.barcode.volley.VolleyErrorHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BarCodeReader extends AppCompatActivity {

    private static final double LATITUDE=0.0000000;
    private static final double LONGITUDE=0.000000;
    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";
    TextView text;
    AppCompatButton scan_another_btn,save_btn1,logout_btn;
    TextView barcodeResult;
    String mBarcodevalue="121112";
    boolean isCameraOpen = false;

    CustomRequest customRequest;
    private View mProgressView;
    private View mLoginFormView;

    private GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_code_reader);

        gps = new GPSTracker(BarCodeReader.this);
        init();

//        if (!isCameraOpen)
//        barCodeReaderLauncher();
    }

    private void init(){
        if (getIntent() != null){
            customRequest = getIntent().getParcelableExtra(getResources().getString(R.string.praceable_modal_regsitration));
            mBarcodevalue = getIntent().getStringExtra(LoginActivity.BARCODERESULT);
            if (gps.canGetLocation()){
                double lat = gps.getLatitude();
                double lng = gps.getLongitude();
                customRequest.setLatitude(lat);
                customRequest.setLongitude(lng);
            }

        }
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        scan_another_btn=(AppCompatButton)findViewById(R.id.button1);
        save_btn1=(AppCompatButton)findViewById(R.id.button_save);
        text = (TextView)findViewById(R.id.barcoderesult);
        logout_btn = (AppCompatButton) findViewById(R.id.logout);

        try {
            text.setText(mBarcodevalue + "");
        }catch (Exception ex){
            ex.printStackTrace();
        }

        scan_another_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save_btn1.setEnabled(true);
                barCodeReaderLauncher();
                return;
            }
        });

        save_btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!text.getText().toString().equals(null) && text.getText().toString() != null )
                doSomething();
                else
                    Toast.makeText(BarCodeReader.this,"Not a valid barcode",Toast.LENGTH_LONG).show();
            }
        });

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    resetPrefs();
                    final Intent mintent = new Intent(BarCodeReader.this, LoginActivity.class);
                    mintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mintent);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
    }

    private void barCodeReaderLauncher(){
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
        intent.putExtra(BarcodeCaptureActivity.UseFlash, false);

        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }
    public  void resetPrefs(){
        BarCodePrefs barCodePrefs = ApplicationController.getInstance().getMobiKytePrefs();
        if(barCodePrefs != null) {
            barCodePrefs.clear();
            CustomRequest myCustomRequest = new CustomRequest();
            myCustomRequest.setEmail("");
            myCustomRequest.setPassword("");
            myCustomRequest.setClientId(customRequest.getClientId());
            myCustomRequest.setClientSecret(customRequest.getClientSecret());
            myCustomRequest.setSecurityToken(customRequest.getSecurityToken());
            barCodePrefs.putObject("user", myCustomRequest);
            barCodePrefs.commit();
        } else {
            android.util.Log.e("MOBIKYTE PREFS==", "Preference is null");
        }
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void doSomething() {
        showProgress(true);
        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        String uri = "";
        try {
            if (customRequest != null){
                uri = String.format("https://login.salesforce.com/services/oauth2/token?grant_type=%1$s&password=%2$s&username=%3$s&client_secret=%4$s&client_id=%5$s",
                        AppConstants.GRANT_TYPE,
                        customRequest.getPassword()+customRequest.getSecurityToken(),
                        customRequest.getEmail(),
                        customRequest.getClientSecret(),
                        customRequest.getClientId());
                StringRequest myReq = new StringRequest(Request.Method.POST,
                        uri,
                        createLoginReqSuccessListener(),
                        createLoginReqErrorListener());


                myReq.setRetryPolicy(new DefaultRetryPolicy(
                        AppConstants.MY_SOCKET_TIMEOUT_MS,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                mRequestQueue.add(myReq);
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    private Response.Listener<String> createLoginReqSuccessListener(){
        return new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                Gson gson = new GsonBuilder().create();
                CustomRequest customRequest = gson.fromJson(response, CustomRequest.class);
                saveBarCodeEntry();
            }
        };
    }

    private Response.ErrorListener createLoginReqErrorListener(){
        return new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                try {
                    showProgress(false);
                    if (volleyError.networkResponse != null){
                        if(volleyError.networkResponse.statusCode==400) {
                            Gson gson = new GsonBuilder().create();
                            ErrorModal errorModal = gson.fromJson(new String(volleyError.networkResponse.data), ErrorModal.class);
                            if (errorModal.getError().toString().equals("invalid_grant")){
                                Toast.makeText(getApplicationContext(), R.string.invalid_grant, Toast.LENGTH_LONG).show();
                            }else if (errorModal.getError().toString().equals("invalid_client")){
                                Toast.makeText(getApplicationContext(), R.string.invalid_client, Toast.LENGTH_LONG).show();
                            }
                            else if (errorModal.getError().toString().equals("invalid_client_id")){
                                Toast.makeText(getApplicationContext(), R.string.invalid_client_id, Toast.LENGTH_LONG).show();
                            }
                        }else{
                            Toast.makeText(getApplicationContext(), VolleyErrorHelper.getMessage(volleyError,BarCodeReader.this), Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "Network problem", Toast.LENGTH_LONG).show();
                    }

                }catch (Exception ex){
                    ex.printStackTrace();
                }

            }
        };
    }
    private void saveBarCodeEntry(){
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("CIRCEBarcode__Barcode__c", text.getText().toString().trim());
            if (customRequest.getLatitude() != LATITUDE && customRequest.getLongitude() != LONGITUDE){
                jsonBody.put("CIRCEBarcode__Geolocation__Latitude__s", customRequest.getLatitude());
                jsonBody.put("CIRCEBarcode__Geolocation__Longitude__s", customRequest.getLongitude());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, "https://ap2.salesforce.com/services/data/v20.0/sobjects/CIRCEBarcode__Barcode__c", jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        save_btn1.setEnabled(false);
                        showProgress(false);
                        Toast.makeText(BarCodeReader.this,"Barcode saved successfully",Toast.LENGTH_LONG).show();

                        //...
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            showProgress(false);
                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG)
                                    .show();
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }

                        //...
                    }
                })
        {
            @Override
            protected Map<String,String> getParams() {
                // something to do here ??
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                // something to do here ??
                Map<String, String> params = new HashMap<String, String>();
                params.put(
                        "Authorization",
                        "Bearer " + customRequest.getAccessToken());
                params.put("Content-Type","application/json");
                return params;
            }
        };


        request.setRetryPolicy(new DefaultRetryPolicy(
                AppConstants.MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);
    }


    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * <p/>
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.
     * <p/>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    text.setText(barcode.displayValue);
                } else {
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                Toast.makeText(BarCodeReader.this,getString(R.string.barcode_error),Toast.LENGTH_SHORT).show();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
