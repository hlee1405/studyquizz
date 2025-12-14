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
import com.example.studyquizz.R;
import com.example.studyquizz.data.AuthManager;
import com.example.studyquizz.databinding.ActivitySignupBinding;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignupBinding binding;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = new AuthManager(this);
        if (authManager.isLoggedIn()) {
            goHome();
            return;
        }

        binding.btnSignUp.setOnClickListener(v -> handleSignUp());
        binding.txtSignIn.setOnClickListener(v -> goToLogin());
        
        // Handle system navigation bar overlap
        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollView, (v, insets) -> {
            int bottomPadding = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), 
                    Math.max(100, bottomPadding + 40));
            return insets;
        });
    }

    private void handleSignUp() {
        String fullName = binding.inputFullName.getText().toString().trim();
        String email = binding.inputEmail.getText().toString().trim();
        String password = binding.inputPassword.getText().toString().trim();
        String confirmPassword = binding.inputConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        if (authManager.signup(fullName, email, password)) {
            Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
            goHome();
        } else {
            Toast.makeText(this, "Email đã được sử dụng", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void goHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}

