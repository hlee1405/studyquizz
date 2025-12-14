package com.example.studyquizz.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studyquizz.MainActivity;
import com.example.studyquizz.R;
import com.example.studyquizz.databinding.ActivityQuizResultBinding;

public class QuizResultActivity extends AppCompatActivity {
    public static final String EXTRA_SCORE = "score"; // Score on scale of 10 (float)
    public static final String EXTRA_TOTAL = "total";
    public static final String EXTRA_CORRECT = "correct";
    public static final String EXTRA_INCORRECT = "incorrect";

    private ActivityQuizResultBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get data from intent
        float score = getIntent().getFloatExtra(EXTRA_SCORE, 0f);
        int total = getIntent().getIntExtra(EXTRA_TOTAL, 0);
        int correct = getIntent().getIntExtra(EXTRA_CORRECT, 0);
        int incorrect = getIntent().getIntExtra(EXTRA_INCORRECT, 0);

        setupViews(score, total, correct, incorrect);
    }

    private void setupViews(float score, int total, int correct, int incorrect) {
        // Set score on scale of 10 (display with 1 decimal place)
        String scoreText;
        if (score == (int) score) {
            scoreText = String.valueOf((int) score);
        } else {
            scoreText = String.format("%.1f", score);
        }
        binding.txtScore.setText(scoreText);

        // Set statistics
        binding.txtTotalQuestions.setText(String.valueOf(total));
        binding.txtCorrect.setText(String.valueOf(correct));
        binding.txtIncorrect.setText(String.format("%02d", incorrect));

        // Home button
        binding.btnHome.setOnClickListener(v -> goToHome());
    }

    private void goToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

