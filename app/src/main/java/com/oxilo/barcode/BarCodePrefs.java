package com.oxilo.barcode;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * Created by ericbasendra on 29/11/15.
 */
public class BarCodePrefs {

    private static BarCodePrefs mobiKytePrefs;
    private final Context context;
    private final SharedPreferences preferences;
    private final SharedPreferences.Editor  editor;
    private static Gson GSON            = new Gson();
    Type typeOfObject    = new TypeToken<Object>(){}.getType();

    private BarCodePrefs(Context context, String namePreferences, int mode) {
        this.context = context;
        if (namePreferences == null || namePreferences.equals("")) {
            namePreferences = "barcode";
        }
        preferences = context.getSharedPreferences(namePreferences, mode);
        editor = preferences.edit();
    }

    public static BarCodePrefs getComplexPreferences(Context context,
                                                      String namePreferences, int mode) {
        if (mobiKytePrefs == null) {
            mobiKytePrefs = new BarCodePrefs(context,
                    namePreferences, mode);
        }
        return mobiKytePrefs;
    }

    public void putObject(String key, Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Object is null");
        }
        if (key.equals("") || key == null) {
            throw new IllegalArgumentException("Key is empty or null");
        }
        editor.putString(key, GSON.toJson(object));
    }

    public void commit() {
        editor.commit();
    }

    public void clear(){
        editor.clear();
    }

    public <T> T getObject(String key, Class<T> a) {
        String gson = preferences.getString(key, null);
        if (gson == null) {
            return null;
        }
        else {
            try {
                return GSON.fromJson(gson, a);
            }
            catch (Exception e) {
                throw new IllegalArgumentException("Object stored with key "
                        + key + " is instance of other class");
            }
        }
    }
}
