package com.example.studyquizz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studyquizz.R;
import com.example.studyquizz.data.QuizRepository;
import com.example.studyquizz.databinding.ActivityJoinQuizBinding;
import com.example.studyquizz.model.Quiz;

public class JoinQuizActivity extends AppCompatActivity {
    private ActivityJoinQuizBinding binding;
    private QuizRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityJoinQuizBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            repository = QuizRepository.getInstance(this);
            setupViews();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupViews() {
        // Back button
        binding.btnBack.setOnClickListener(v -> finish());

        // Next button
        binding.btnNext.setOnClickListener(v -> handleNext());
    }

    private void handleNext() {
        String quizId = binding.inputQuizId.getText().toString().trim();
        String password = binding.inputPassword.getText().toString().trim();

        if (TextUtils.isEmpty(quizId)) {
            Toast.makeText(this, "Nhập Quiz ID", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Nhập Password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find quiz by Quiz ID and Password
        Quiz quiz = repository.findQuizByQuizIdAndPassword(quizId, password);
        if (quiz == null) {
            Toast.makeText(this, "Không tìm thấy quiz hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate to Quiz Found screen
        Intent intent = new Intent(this, QuizFoundActivity.class);
        intent.putExtra(QuizFoundActivity.EXTRA_QUIZ_ID, quiz.getId());
        startActivity(intent);
    }
}

