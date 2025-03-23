// utils/SharedPrefsHelper.java

package com.example.licenta_v2.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsHelper {

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SharedPrefsHelper(Context context) {
        prefs = context.getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveCredentials(String email, String password) {
        editor.putBoolean("rememberMe", true);
        editor.putString("email", email);
        editor.putString("password", password);
        editor.apply();
    }

    public boolean isRemembered() {
        return prefs.getBoolean("rememberMe", false);
    }

    public void clear() {
        editor.clear().apply();
    }
}
