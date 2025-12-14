package com.example.studyquizz.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class AuthManager {
    private static final String PREF_NAME = "study_quiz_auth";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private final SharedPreferences prefs;

    public AuthManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return !TextUtils.isEmpty(prefs.getString(KEY_EMAIL, null));
    }

    public void logout() {
        prefs.edit().clear().apply();
    }

    public boolean login(String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            return false;
        }
        prefs.edit().putString(KEY_EMAIL, email).putString(KEY_PASSWORD, password).apply();
        return true;
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }
}




