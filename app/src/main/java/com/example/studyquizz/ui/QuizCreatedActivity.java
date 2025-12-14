package com.example.studyquizz.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studyquizz.MainActivity;
import com.example.studyquizz.R;
import com.example.studyquizz.databinding.ActivityQuizCreatedBinding;

public class QuizCreatedActivity extends AppCompatActivity {
    public static final String EXTRA_QUIZ_NAME = "quiz_name";
    public static final String EXTRA_QUIZ_ID = "quiz_id";
    public static final String EXTRA_PASSWORD = "password";

    private ActivityQuizCreatedBinding binding;
    private String quizId;
    private String password;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizCreatedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get data from intent
        String quizName = getIntent().getStringExtra(EXTRA_QUIZ_NAME);
        quizId = getIntent().getStringExtra(EXTRA_QUIZ_ID);
        password = getIntent().getStringExtra(EXTRA_PASSWORD);

        setupViews(quizName);
    }

    private void setupViews(String quizName) {
        // Set quiz information
        binding.txtQuizName.setText(quizName != null ? quizName : "Quiz");
        binding.txtQuizId.setText(quizId != null ? quizId : "");
        binding.txtPassword.setText(password != null ? password : "");

        // Initially hide password
        binding.txtPassword.setTransformationMethod(new android.text.method.PasswordTransformationMethod());

        // Password toggle
        binding.imgPasswordToggle.setOnClickListener(v -> togglePasswordVisibility());

        // Copy Quiz ID button
        binding.btnCopyQuizId.setOnClickListener(v -> copyQuizId());

        // Home button
        binding.btnHome.setOnClickListener(v -> goToHome());
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            binding.txtPassword.setTransformationMethod(null);
        } else {
            binding.txtPassword.setTransformationMethod(new android.text.method.PasswordTransformationMethod());
        }
    }

    private void copyQuizId() {
        if (quizId != null && !quizId.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Quiz ID", quizId);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Đã sao chép Quiz ID", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

