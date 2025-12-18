package com.example.studyquizz.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class AuthManager {
    private static final String PREF_NAME = "study_quiz_auth";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_DEFAULT_USERS_INITIALIZED = "default_users_initialized";
    private final SharedPreferences prefs;

    public AuthManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        ensureDefaultUsers();
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
        // Check if user exists
        String savedPassword = prefs.getString(KEY_EMAIL + "_" + email, null);
        if (savedPassword == null || !savedPassword.equals(password)) {
            return false;
        }
        // Get full name if available
        String fullName = prefs.getString(KEY_FULL_NAME + "_" + email, "");
        prefs.edit()
                .putString(KEY_EMAIL, email)
                .putString(KEY_PASSWORD, password)
                .putString(KEY_FULL_NAME, fullName)
                .apply();
        return true;
    }

    public boolean signup(String fullName, String email, String password) {
        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            return false;
        }
        // Check if user already exists
        String existingPassword = prefs.getString(KEY_EMAIL + "_" + email, null);
        if (existingPassword != null) {
            return false; // User already exists
        }
        // Save new user
        prefs.edit()
                .putString(KEY_EMAIL + "_" + email, password)
                .putString(KEY_FULL_NAME + "_" + email, fullName)
                .apply();
        // Auto login after signup
        prefs.edit()
                .putString(KEY_EMAIL, email)
                .putString(KEY_PASSWORD, password)
                .putString(KEY_FULL_NAME, fullName)
                .apply();
        return true;
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, "");
    }

    /**
     * Khởi tạo sẵn 2 tài khoản người dùng mặc định cho app ở lần chạy đầu tiên.
     * Tài khoản 1:
     *  - Tên: Người dùng A
     *  - Email: user1@example.com
     *  - Mật khẩu: 123456
     * Tài khoản 2:
     *  - Tên: Người dùng B
     *  - Email: user2@example.com
     *  - Mật khẩu: 123456
     */
    private void ensureDefaultUsers() {
        boolean initialized = prefs.getBoolean(KEY_DEFAULT_USERS_INITIALIZED, false);
        if (initialized) return;

        SharedPreferences.Editor editor = prefs.edit();

        // User 1
        String email1 = "user1@example.com";
        String password1 = "123456";
        String fullName1 = "Người dùng A";
        editor.putString(KEY_EMAIL + "_" + email1, password1);
        editor.putString(KEY_FULL_NAME + "_" + email1, fullName1);

        // User 2
        String email2 = "user2@example.com";
        String password2 = "123456";
        String fullName2 = "Người dùng B";
        editor.putString(KEY_EMAIL + "_" + email2, password2);
        editor.putString(KEY_FULL_NAME + "_" + email2, fullName2);

        // Đánh dấu đã khởi tạo để không tạo lại nhiều lần
        editor.putBoolean(KEY_DEFAULT_USERS_INITIALIZED, true);
        editor.apply();
    }
}




