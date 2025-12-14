package com.example.studyquizz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studyquizz.R;
import com.example.studyquizz.data.QuizRepository;
import com.example.studyquizz.databinding.ActivityQuizFoundBinding;
import com.example.studyquizz.model.Quiz;

public class QuizFoundActivity extends AppCompatActivity {
    public static final String EXTRA_QUIZ_ID = "quiz_id";
    
    private ActivityQuizFoundBinding binding;
    private QuizRepository repository;
    private Quiz quiz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizFoundBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = QuizRepository.getInstance(this);
        String quizId = getIntent().getStringExtra(EXTRA_QUIZ_ID);
        
        if (quizId == null) {
            Toast.makeText(this, "Không tìm thấy quiz", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        quiz = repository.getQuizById(quizId);
        if (quiz == null) {
            Toast.makeText(this, "Không tìm thấy quiz", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupViews();
    }

    private void setupViews() {
        // Back button
        binding.btnBack.setOnClickListener(v -> finish());

        // Set quiz information
        binding.txtQuizName.setText(quiz.getTitle());
        
        // Determine quiz type
        boolean isMultipleChoice = quiz.getQuestions().size() > 0 && 
            quiz.getQuestions().get(0).getType().toString().equals("MULTIPLE_CHOICE");
        binding.txtQuizType.setText(isMultipleChoice ? "MCQ" : "True/False");
        
        // Number of questions
        binding.txtNumberOfQuestion.setText(String.valueOf(quiz.getQuestions().size()));
        
        // Parse duration from description
        String duration = "15 Minutes"; // Default
        if (quiz.getDescription() != null && quiz.getDescription().contains("Thời gian:")) {
            try {
                String[] parts = quiz.getDescription().split("Thời gian:");
                if (parts.length > 1) {
                    String timePart = parts[1].split(" phút")[0].trim();
                    duration = timePart + " Minutes";
                }
            } catch (Exception e) {
                // Use default
            }
        }
        binding.txtQuizDuration.setText(duration);

        // Start Quiz button
        binding.btnStartQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuizPlayActivity.class);
            intent.putExtra(QuizPlayActivity.EXTRA_QUIZ_ID, quiz.getId());
            intent.putExtra(QuizPlayActivity.EXTRA_MODE, QuizPlayActivity.MODE_EXAM);
            startActivity(intent);
            finish();
        });
    }
}


