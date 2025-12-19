package com.example.studyquizz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.studyquizz.MainActivity;
import com.example.studyquizz.data.AuthManager;
import com.example.studyquizz.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Handle system navigation bar overlap
        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollView, (v, insets) -> {
            int bottomPadding = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), 
                    Math.max(100, bottomPadding + 40));
            return insets;
        });
    }

    private void goToSignUp() {
        startActivity(new Intent(this, SignUpActivity.class));
        finish();
    }

    private void handleLogin() {
        String email = binding.inputEmail.getText().toString().trim();
        String password = binding.inputPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }
        if (authManager.login(email, password)) {
            goHome();
        } else {
            Toast.makeText(this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
        }
    }

    private void goHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}




