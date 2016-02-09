package com.oxilo.barcode.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.oxilo.barcode.AppConstants;
import com.oxilo.barcode.ApplicationController;
import com.oxilo.barcode.BarCodePrefs;
import com.oxilo.barcode.BarCodeReader;
import com.oxilo.barcode.BarcodeCaptureActivity;
import com.oxilo.barcode.Pojo.CustomRequest;
import com.oxilo.barcode.Pojo.ErrorModal;
import com.oxilo.barcode.R;
import com.oxilo.barcode.volley.VolleyErrorHelper;

import java.util.Map;

public class LoginActivity  extends AppCompatActivity {


    // UI references.
    private EditText mEmailView, mPasswordView;
    private EditText mConsumerSecret, mConsumerKey, mSecurityToken;
    Button btnLogin;
    private View mProgressView;
    private View mLoginFormView;
    Toolbar toolbar;
    CustomRequest customRequest;
    public static final String BARCODERESULT = "barcodeResult";
    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initUiView();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_UP) {
            final View view = getCurrentFocus();

            if(view != null) {
                final boolean consumed = super.dispatchTouchEvent(ev);

                final View viewTmp = getCurrentFocus();
                final View viewNew = viewTmp != null ? viewTmp : view;

                if(viewNew.equals(view)) {
                    final Rect rect = new Rect();
                    final int[] coordinates = new int[2];

                    view.getLocationOnScreen(coordinates);

                    rect.set(coordinates[0], coordinates[1], coordinates[0] + view.getWidth(), coordinates[1] + view.getHeight());

                    final int x = (int) ev.getX();
                    final int y = (int) ev.getY();

                    if(rect.contains(x, y)) {
                        return consumed;
                    }
                }
                else if(viewNew instanceof EditText) {
                    return consumed;
                }

                final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                inputMethodManager.hideSoftInputFromWindow(viewNew.getWindowToken(), 0);

                viewNew.clearFocus();

                return consumed;
            }
        }
        return super.dispatchTouchEvent(ev);
    }



    private void initUiView(){
        try {
            // Set up the ToolBar.
            toolbar = (Toolbar)findViewById(R.id.toolbar);
            toolbar.setTitle("");
            ((AppCompatActivity) LoginActivity.this).setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mLoginFormView = findViewById(R.id.login_form);
            mProgressView = findViewById(R.id.login_progress);
            mEmailView = (EditText) findViewById(R.id.etUsername);
            mPasswordView = (EditText) findViewById(R.id.etpassword);
            mConsumerSecret = (EditText) findViewById(R.id.etConsumerSecret);
            mConsumerKey = (EditText) findViewById(R.id.etConsumerKey);
            mSecurityToken = (EditText) findViewById(R.id.etSecurityToken);
            btnLogin = (Button) findViewById(R.id.btnlogin);
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptLogin();
                }
            });

//            mEmailView.setText(AppConstants.USERNAME);
//            mPasswordView.setText(AppConstants.PASSWORD);
//            mConsumerSecret.setText(AppConstants.CONSUMER_SECRET);
//            mConsumerKey.setText(AppConstants.CONSUMER_KEY);
//            mSecurityToken.setText(AppConstants.SECURITY_TOKEN);

            BarCodePrefs barCodePrefs = ApplicationController.getInstance().getMobiKytePrefs();
            if(barCodePrefs != null) {
               if (!barCodePrefs.getObject("user",CustomRequest.class).getEmail().equals("")){
                   doSomething(barCodePrefs.getObject("user",CustomRequest.class).getEmail(),barCodePrefs.getObject("user",CustomRequest.class).getPassword()
                   ,barCodePrefs.getObject("user",CustomRequest.class).getClientSecret(),
                           barCodePrefs.getObject("user",CustomRequest.class).getClientId(),barCodePrefs.getObject("user",CustomRequest.class).getSecurityToken());
               }
                else{
                    mEmailView.setText(barCodePrefs.getObject("user",CustomRequest.class).getEmail());
                    mPasswordView.setText(barCodePrefs.getObject("user",CustomRequest.class).getPassword());
                    mConsumerSecret.setText(barCodePrefs.getObject("user",CustomRequest.class).getClientSecret());
                    mConsumerKey.setText(barCodePrefs.getObject("user",CustomRequest.class).getClientId());
                    mSecurityToken.setText(barCodePrefs.getObject("user",CustomRequest.class).getSecurityToken());
                }
            } else {
                customRequest = new CustomRequest();
                customRequest.setCameraOpenFirstTime(false);
                android.util.Log.e("BarCode PREFS==", "Preference is null");
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false;
        } else {
            return true;
        }
    }
    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mConsumerSecret.setError(null);
        mConsumerKey.setError(null);
        mSecurityToken.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String consumerSecret = mConsumerSecret.getText().toString();
        String consumerKey = mConsumerKey.getText().toString();
        String securityToken = mSecurityToken.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
         if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }
        else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a empty consumerSecret, .
        else if (TextUtils.isEmpty(consumerSecret)) {
             mConsumerSecret.setError(getString(R.string.error_field_required));
            focusView = mConsumerSecret;
            cancel = true;
        }
        // Check for a empty consumerKey, if the user entered one.
        else if (TextUtils.isEmpty(consumerKey)) {
             mConsumerKey.setError(getString(R.string.error_field_required));
            focusView = mConsumerKey;
            cancel = true;
        }
         else if (TextUtils.isEmpty(securityToken)) {
             mSecurityToken.setError(getString(R.string.error_field_required));
             focusView = mSecurityToken;
             cancel = true;
         }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            // Authicate user
            doSomething(email,password,consumerSecret,consumerKey,securityToken);
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


    private void doSomething(String username, String password,String consumer_secret,String consumer_key,String security_token) {
        try {
            showProgress(true);
            RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext());
            String uri = String.format("https://login.salesforce.com/services/oauth2/token?grant_type=%1$s&password=%2$s&username=%3$s&client_secret=%4$s&client_id=%5$s",
                    AppConstants.GRANT_TYPE,
                    password,
                    username,
                    consumer_secret,
                    consumer_key);

            Log.e("URL","" + uri);

            StringRequest myReq = new StringRequest(Request.Method.POST,
                    uri,
                    createMyReqSuccessListener(username, password,consumer_secret,consumer_key,security_token),
                    createMyReqErrorListener());

            myReq.setRetryPolicy(new DefaultRetryPolicy(
                    AppConstants.MY_SOCKET_TIMEOUT_MS,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            mRequestQueue.add(myReq);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private Response.Listener<String> createMyReqSuccessListener(final String username, final String password, final String client_secret, final String client_key,final String security_token){
        return new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                try {
                    Gson gson = new GsonBuilder().create();
                    customRequest = gson.fromJson(response, CustomRequest.class);
                    customRequest.setEmail(username);
                    customRequest.setPassword(password);
                    customRequest.setClientSecret(client_secret);
                    customRequest.setClientId(client_key);
                    customRequest.setSecurityToken(security_token);

                    BarCodePrefs mobiKytePrefs = ApplicationController.getInstance().getMobiKytePrefs();
                    if(mobiKytePrefs != null) {
                        mobiKytePrefs.putObject("user", customRequest);
                        mobiKytePrefs.commit();

                    } else {
                        android.util.Log.e("MOBIKYTE PREFS==", "Preference is null");
                    }

                    barCodeReaderLauncher();
                }catch (Exception ex){
                    ex.printStackTrace();
                }

            }
        };
    }

    private Response.ErrorListener createMyReqErrorListener(){
        return new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                try {
                    showProgress(false);
                    if (volleyError.networkResponse != null){
                        if(volleyError.networkResponse.statusCode==400) {
                            Gson gson = new GsonBuilder().create();
                           ErrorModal errorModal = gson.fromJson(new String(volleyError.networkResponse.data), ErrorModal.class);
                            Toast.makeText(getApplicationContext(), errorModal.getError(), Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(getApplicationContext(), VolleyErrorHelper.getMessage(volleyError,LoginActivity.this), Toast.LENGTH_LONG).show();
                        }
                    }

                }catch (Exception ex){
                    ex.printStackTrace();
                }

            }
        };
    }

    private void barCodeReaderLauncher(){
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
        intent.putExtra(BarcodeCaptureActivity.UseFlash, false);

        startActivityForResult(intent, RC_BARCODE_CAPTURE);
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
                    Intent i = new Intent(LoginActivity.this,BarCodeReader.class);
                    i.putExtra(BARCODERESULT,barcode.displayValue);
                    BarCodePrefs barCodePrefs = ApplicationController.getInstance().getMobiKytePrefs();
                    if(barCodePrefs != null) {
                      customRequest = barCodePrefs.getObject("user", CustomRequest.class);
                    }
                    Log.e("cdfdfc",customRequest.getClientId());
                    i.putExtra(getResources().getString(R.string.praceable_modal_regsitration), customRequest);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    return;
                } else {
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
               Toast.makeText(LoginActivity.this,getString(R.string.barcode_error),Toast.LENGTH_SHORT).show();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
