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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oxilo.barcode.AppConstants;
import com.oxilo.barcode.BarCodeReader;
import com.oxilo.barcode.Pojo.CustomRequest;
import com.oxilo.barcode.R;

public class LoginActivity  extends AppCompatActivity {


    // UI references.
    private EditText mEmailView, mPasswordView;
    private EditText mConsumerSecret, mConsumerKey, mSecurityToken;
    Button btnLogin;
    private View mProgressView;
    private View mLoginFormView;
    Toolbar toolbar;

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
            doSomething();
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
        try {
            showProgress(true);
            RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext());
            String username = mEmailView.getText().toString().trim();
            String password = mPasswordView.getText().toString().trim();
            String client_secret = mConsumerSecret.getText().toString().trim();
            String client_key = mConsumerKey.getText().toString().trim();
            String uri = String.format("https://login.salesforce.com/services/oauth2/token?grant_type=%1$s&password=%2$s&username=%3$s&client_secret=%4$s&client_id=%5$s",
                    AppConstants.GRANT_TYPE,
                    password,
                    username,
                    client_secret,
                    client_key);

            StringRequest myReq = new StringRequest(Request.Method.POST,
                    uri,
                    createMyReqSuccessListener(),
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

    private Response.Listener<String> createMyReqSuccessListener(){
        return new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                try {
                    Gson gson = new GsonBuilder().create();
                    CustomRequest customRequest = gson.fromJson(response, CustomRequest.class);
                    customRequest.setEmail(mEmailView.getText().toString());
                    customRequest.setPassword(mPasswordView.getText().toString());
                    customRequest.setClientSecret(AppConstants.CLIENT_SECRET);
                    customRequest.setClientId(AppConstants.CLIENT_ID);
                    Intent i = new Intent(LoginActivity.this,BarCodeReader.class);
                    i.putExtra(getResources().getString(R.string.praceable_modal_regsitration), customRequest);
                    startActivity(i);
                    return;
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
                    Toast.makeText(getApplicationContext(), volleyError.getMessage(), Toast.LENGTH_LONG)
                            .show();
                }catch (Exception ex){
                    ex.printStackTrace();
                }

            }
        };
    }
}
