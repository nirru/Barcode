package com.oxilo.barcode;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;

/**
 * Created by ericbasendra on 15/11/15.
 */
public class ApplicationController extends Application {

    public static final int LONG_DELAY = 3500; // 3.5 seconds
    public static final int SHORT_DELAY = 2000; // 2 seconds

    /**
     * A singleton instance of the application class for easy access in other places
     */
    private static ApplicationController sInstance;

    private BarCodePrefs barCodePrefs;

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize the singleton
        sInstance = this;


        barCodePrefs = BarCodePrefs.getComplexPreferences(getBaseContext(), "barcode", MODE_PRIVATE);
    }

    /**
     * @return ApplicationController singleton instance
     */
    public static synchronized ApplicationController getInstance() {
        return sInstance;
    }


    public BarCodePrefs getMobiKytePrefs() {
        if(barCodePrefs != null) {
            return barCodePrefs;
        }
        return null;
    }

}
